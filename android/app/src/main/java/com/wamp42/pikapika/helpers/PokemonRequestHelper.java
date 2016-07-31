package com.wamp42.pikapika.helpers;

import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.wamp42.pikapika.R;
import com.wamp42.pikapika.activities.MapsActivity;
import com.wamp42.pikapika.models.GoogleAuthTokenJson;
import com.wamp42.pikapika.models.PokemonResult;
import com.wamp42.pikapika.utils.Utils;

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
    private static final int RADIUS_QUICK_SCAN = 5000; //5km
    private static final int AUTO_QUICK_SCAN_TIME =15000; //15seconds
    public MapsActivity mMapsActivity;

    public static LatLng lastLocationRequested = null;

    public PokemonRequestHelper(MapsActivity mMapsActivity) {
        this.mMapsActivity = mMapsActivity;
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
            mMapsActivity.countDownRequestTimer.start();
        }
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
            mMapsActivity.countDownRequestTimer.onFinish();
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
            mMapsActivity.countDownRequestTimer.onFinish();
        }
    };

    public void doQuickPokemonScan(){
        LatLng latLng = mMapsActivity.getLocation();
        if(latLng != null)
            DataManager.getDataManager().quickHeartbeat(latLng.latitude+"",latLng.longitude+"",RADIUS_QUICK_SCAN,quickScanCallback);
    }

    public void autoScan(){
        mMapsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doQuickPokemonScan();
                    }
                }, AUTO_QUICK_SCAN_TIME);
            }
        });
    }

    final Callback quickScanCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            //TODO:handle failure
            autoScan();
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
                                //scan again
                                autoScan();
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
}
