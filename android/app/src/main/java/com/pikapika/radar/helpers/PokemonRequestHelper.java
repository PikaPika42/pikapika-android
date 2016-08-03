package com.pikapika.radar.helpers;

import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.pikapika.radar.R;
import com.pikapika.radar.activities.MapsActivity;
import com.pikapika.radar.models.GoogleAuthTokenJson;
import com.pikapika.radar.models.PokemonResult;
import com.pikapika.radar.utils.Debug;
import com.pikapika.radar.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private static final double BASE_SCAN_METERS = 150;

    private static final int RADIUS_QUICK_SCAN = 5000; //5km
    private static final int AUTO_QUICK_SCAN_TIME =15000; //15seconds

    private static final int CIRCLE_1_SCAN_STEPS = 9;
    private static final int CIRCLE_2_SCAN_STEPS = 25;

    private MapsActivity mMapsActivity;
    private Handler autoScanHandler;
    private ProgressBar progressBar;

    public static LatLng lastLocationRequested = null;

    private int scanNumber = 9;
    private int scanCounter = 0;
    private List<Marker> markerDebugList = new ArrayList<>();

    public PokemonRequestHelper(MapsActivity mMapsActivity) {
        this.mMapsActivity = mMapsActivity;
        progressBar = (ProgressBar)mMapsActivity.findViewById(R.id.progressBar);
        autoScanHandler = new Handler();
    }

    public void heartbeat(){
        LatLng latLng = mMapsActivity.getLocation();

        if(latLng == null) {
            PokemonHelper.showAlert(mMapsActivity,mMapsActivity.getString(R.string.gps_error_title),mMapsActivity.getString(R.string.gps_error_body));
        } else {
            lastLocationRequested = latLng;
            mMapsActivity.loadingProgressDialog = PokemonHelper.showLoading(mMapsActivity);
            GoogleAuthTokenJson googleAuthTokenJson = PokemonHelper.getGoogleTokenJson(mMapsActivity);
            DataManager.getDataManager().heartbeat(googleAuthTokenJson.getId_token(), latLng.latitude + "", latLng.longitude + "", heartbeatCallback);
            //mMapsActivity.countDownRequestTimer.start();
        }
    }

    public boolean heartbeat_v2(){
        LatLng latLng = lastLocationRequested;

        if(latLng == null) {
            PokemonHelper.showAlert(mMapsActivity,mMapsActivity.getString(R.string.gps_error_title),mMapsActivity.getString(R.string.gps_error_body));
            return false;
        } else {
            GoogleAuthTokenJson googleAuthTokenJson = PokemonHelper.getGoogleTokenJson(mMapsActivity);
            LatLng newLatLng;
            if(scanCounter == 0){
                newLatLng = latLng;
            }else {
                double angleRadians = (2 * Math.PI/(CIRCLE_1_SCAN_STEPS-1))*(scanCounter-1);
                newLatLng = locationWithBearing(angleRadians, BASE_SCAN_METERS, latLng);
                Debug.Log("new heartbeat: "+scanCounter+ " angle:"+angleRadians);

            }
            createDebugMarker(newLatLng);
            DataManager.getDataManager().heartbeat_v2(googleAuthTokenJson.getId_token(), newLatLng.latitude + "", newLatLng.longitude + "", heartbeatCallback);
            scanCounter ++;
            return true;
        }
    }

    public void createDebugMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ocation_map_pin_blue_36))
                .draggable(true);
        markerDebugList.add(mMapsActivity.mMap.addMarker(markerOptions));
    }

    public void startAutoHeartBeat_v2(){
        LatLng latLng = mMapsActivity.getLocation();

        if(latLng == null) {
            PokemonHelper.showAlert(mMapsActivity,mMapsActivity.getString(R.string.gps_error_title),mMapsActivity.getString(R.string.gps_error_body));
        } else {
            scanCounter = 0;
            lastLocationRequested = latLng;
            countDownAutoScan.start();
            progressBar.setVisibility(View.VISIBLE);
            mMapsActivity.searchButton.setVisibility(View.INVISIBLE);

        }
    }

    public void stopAutoHeartBeat_v2(){
        countDownAutoScan.onFinish();
        mMapsActivity.countDownNewHeartBeat.onFinish();
    }

    final Callback heartbeatCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            if(mMapsActivity.loadingProgressDialog != null)
                mMapsActivity.loadingProgressDialog.dismiss();

            if(!Utils.isNetworkAvailable(mMapsActivity)){
                //internet error message
                PokemonHelper.showAlert(mMapsActivity,mMapsActivity.getString(R.string.error_title)+"!",
                        mMapsActivity.getString(R.string.internet_error_body));
            } else {
                mMapsActivity.refreshToken();
            }
            //mMapsActivity.countDownRequestTimer.onFinish();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if(mMapsActivity.loadingProgressDialog != null)
                mMapsActivity.loadingProgressDialog.dismiss();
            if (response.code() == 200) {
                String jsonStr = response.body().string();
                if (!jsonStr.isEmpty()) {
                    Type listType = new TypeToken<List<PokemonResult>>() {}.getType();
                    try {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(jsonStr).getAsJsonObject();
                        if(jsonObject.has("data")){
                            List<PokemonResult> resultList = new Gson().fromJson(jsonObject.get("data").toString(), listType);
                            if (resultList != null) {
                                mMapsActivity.addDrawPokemonOnMainThread(resultList, true);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                response.body().close();
            } else {
                //strange error from api, try to generate login again
                if (response.code() >= 400) {
                    mMapsActivity.refreshToken();
                    return;
                }
                PokemonHelper.showAlert(mMapsActivity,mMapsActivity.getString(R.string.request_error_title),
                        mMapsActivity.getString(R.string.request_error_body));
            }
            //mMapsActivity.countDownRequestTimer.onFinish();
        }
    };

    public void startQuickScanLoop(){
        autoScanHandler.removeCallbacks(quickScanRunnable);
        autoQuickPokemonScan();
    }

    public void autoQuickPokemonScan(){
        LatLng latLng = mMapsActivity.getLocation();
        if(latLng != null) {
            DataManager.getDataManager().quickHeartbeat(latLng.latitude + "", latLng.longitude + "", RADIUS_QUICK_SCAN, quickScanCallback);
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
                // TODO Auto-generated catch block
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

    public static LatLng locationWithBearing(double bearing, double distanceMeters,  LatLng locationOrigin) {
        double distRadians = distanceMeters / EARTH_RADIUS_METERS;

        double lat1 = locationOrigin.latitude * Math.PI / 180;
        double lon1 = locationOrigin.longitude * Math.PI / 180;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distRadians) + Math.cos(lat1) * Math.sin(distRadians) * Math.cos(bearing));
        double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(distRadians) * Math.cos(lat1), Math.cos(distRadians) - Math.sin(lat1) * Math.sin(lat2));

        return new LatLng( lat2 * 180 /  Math.PI,  lon2 * 180 / Math.PI);
    }

    public static LatLng locationForAngle(double angle ,  LatLng location, double distance){
        //angle must be in radians
        double longitude = distance * Math.cos(angle) - location.longitude;
        double latitude = distance * Math.sin(angle) - location.latitude;
        return new LatLng(latitude,longitude);
    }

    final CountDownTimer countDownAutoScan = new CountDownTimer(50000, 5000) {

        public void onTick(long millisUntilFinished) {
            heartbeat_v2();
            int progress = 100/CIRCLE_1_SCAN_STEPS*scanCounter;
                if (progress > 97)
                    progress = 100;
            progressBar.setProgress(progress);
        }

        public void onFinish() {
            scanCounter = 0;
            mMapsActivity.setActiveSearchButton(false);
            mMapsActivity.searchButton.setVisibility(View.VISIBLE);
            mMapsActivity.timerTextView.setVisibility(View.VISIBLE);
            mMapsActivity.countDownNewHeartBeat.start();
            progressBar.setVisibility(View.INVISIBLE);
            for(Marker marker:markerDebugList)
                marker.remove();
        }
    };
}
