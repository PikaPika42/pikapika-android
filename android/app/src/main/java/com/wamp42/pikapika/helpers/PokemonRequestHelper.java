package com.wamp42.pikapika.helpers;

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
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by flavioreyes on 7/31/16.
 */
public class PokemonRequestHelper {
    private static final int RADIUS_QUICK_SCAN = 3000;
    public MapsActivity mMapsActivity;

    public PokemonRequestHelper(MapsActivity mMapsActivity) {
        this.mMapsActivity = mMapsActivity;
    }

    public void heartbeat(){
        LatLng latLng = mMapsActivity.getLocation();

        if(latLng == null) {
            PokemonHelper.showAlert(mMapsActivity,mMapsActivity.getString(R.string.gps_error_title),mMapsActivity.getString(R.string.gps_error_body));
        } else {
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
        for(PokemonResult pokemonResult:list)
            pokemonResult.setFromQuickScan(true);
    }
}
