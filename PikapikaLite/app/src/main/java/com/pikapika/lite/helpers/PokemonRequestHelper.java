package com.pikapika.lite.helpers;

import android.os.Handler;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.pikapika.lite.activities.MapsActivity;
import com.pikapika.lite.models.PokemonResult;
import com.pikapika.lite.utils.Debug;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by flavioreyes on 7/31/16.
 */
public class PokemonRequestHelper {
    private static final double EARTH_RADIUS_METERS = 6372797.6; // earth radius in meters

    private static final int RADIUS_QUICK_SCAN_LIMIT = 30000; //30km
    private static final int AUTO_QUICK_SCAN_TIME =20000; //20 seconds

    private MapsActivity mMapsActivity;
    private Handler autoScanHandler;

    public PokemonRequestHelper(MapsActivity mMapsActivity) {
        this.mMapsActivity = mMapsActivity;
        autoScanHandler = new Handler();
    }

    public void startQuickScanLoop(){
        if(!mMapsActivity.isMapReady)
            return;
        autoScanHandler.removeCallbacks(quickScanRunnable);
        autoQuickPokemonScan();
    }

    public void stopQuickScanLoop(){
        autoScanHandler.removeCallbacks(quickScanRunnable);
    }

    public void autoQuickPokemonScan(){
        //we use this loop to clean the pokemones with expired time
        PokemonHelper.cleanPokemon();
        LatLng latLng = mMapsActivity.getLocation();
        if(latLng != null) {
            int mapRadio = mMapsActivity.getRadios();
            if(mapRadio > RADIUS_QUICK_SCAN_LIMIT)
                mapRadio = RADIUS_QUICK_SCAN_LIMIT;
            Debug.Log("quick scan with radio: "+mapRadio);
            DataManager.getDataManager().quickHeartbeat(latLng.latitude + "", latLng.longitude + "", mapRadio, quickScanCallback);
        }
        autoScanHandler.postDelayed(quickScanRunnable,AUTO_QUICK_SCAN_TIME);
    }

    final Runnable quickScanRunnable = new Runnable() {
        @Override
        public void run() {
            autoQuickPokemonScan();
        }
    };

    final Callback quickScanCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            //TODO:handle failure
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.code() == 200) {
                String jsonStr = response.body().string();
                if (!jsonStr.isEmpty()) {
                    Debug.Log("DB data: "+jsonStr);
                    Type listType = new TypeToken<List<PokemonResult>>() {}.getType();
                    try {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(jsonStr).getAsJsonObject();
                        if(jsonObject.has("data")){
                            List<PokemonResult> resultList = new Gson().fromJson(jsonObject.get("data").toString(), listType);
                            if (resultList != null) {
                                setQuickScanFlag(resultList);
                                mMapsActivity.addDrawPokemonOnMainThread(resultList,false);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                response.body().close();
            }
        }
    };

    public static void setQuickScanFlag(List<PokemonResult> list){
        //TimeZone timeZone = TimeZone.getTimeZone("GMT");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        long currentMillis = cal.getTimeInMillis();
        for(PokemonResult pokemonResult:list) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            try {
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date convertedDate = dateFormat.parse(pokemonResult.getExpireAt());
                long expireMillis = convertedDate.getTime();
                int timeLeft = (int) (expireMillis - currentMillis);
                pokemonResult.setTimeleft(timeLeft);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            pokemonResult.setFromQuickScan(true);
        }
        Iterator it = list.iterator();
        while(it.hasNext()){
            PokemonResult pokemon = (PokemonResult) it.next();
            if(pokemon.getTimeleft() <= 0)
                it.remove();
        }
    }

    public static LatLng locationWithBearing(double bearing, double distanceMeters, LatLng locationOrigin) {
        double distRadians = distanceMeters / EARTH_RADIUS_METERS;

        double lat1 = locationOrigin.latitude * Math.PI / 180;
        double lon1 = locationOrigin.longitude * Math.PI / 180;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distRadians) + Math.cos(lat1) * Math.sin(distRadians) * Math.cos(bearing));
        double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(distRadians) * Math.cos(lat1), Math.cos(distRadians) - Math.sin(lat1) * Math.sin(lat2));

        return new LatLng( lat2 * 180 /  Math.PI,  lon2 * 180 / Math.PI);
    }

    public static LatLng locationForAngle(double angle , LatLng location, double distance){
        //angle must be in radians
        double longitude = distance * Math.cos(angle) - location.longitude;
        double latitude = distance * Math.sin(angle) - location.latitude;
        return new LatLng(latitude,longitude);
    }

    //return the distance between two locations
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1609.34; //convert to meters
        return dist;
    }
}
