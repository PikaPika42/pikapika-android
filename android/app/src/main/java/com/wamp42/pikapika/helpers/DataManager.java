package com.wamp42.pikapika.helpers;


import android.content.Context;
import android.provider.Settings;

import com.google.gson.Gson;
import com.wamp42.pikapika.models.GoogleAuthTokenJson;
import com.wamp42.pikapika.models.LoginData;
import com.wamp42.pikapika.network.RestClient;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class DataManager {

    public static String AUTH_URL = "https://android.clients.google.com/auth";
    public static final String SECRET = "NCjF1TLi2CcY6t5mt0ZveuL7";
    public static final String CLIENT_ID = "848232511240-73ri3t7plvk96pj4f85uj8otdat2alem.apps.googleusercontent.com";
    public static final String OAUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/device/code";
    public static final String OAUTH_TOKEN_ENDPOINT = "https://www.googleapis.com/oauth2/v4/token";

    private static DataManager dataManagerInstance;
    private RestClient restClient = new RestClient();

    public static DataManager getDataManager(){
        if(dataManagerInstance == null)
            dataManagerInstance = new DataManager();
        return dataManagerInstance;
    }

    //should be just one in all the flow
    private Callback mGoogleLoginCallback;
    private Context mContext;
    private boolean mShouldLoginAPE = false;
    private String mProvider = "google";

    public void login(Context context,Callback callback){
        String androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        //convert object to json
        String jsonInString = new Gson().toJson(new LoginData(androidId,"google"));
        //do the request
        restClient.postJson(jsonInString,"trainers/login",callback);
    }

    public void heartbeat(String token,String lat, String lng, Callback callback){
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", token);
        restClient.get("pokemons/"+lat+"/"+lng+"/heartbeat", params, callback);
    }

    public void quickHeartbeat(String lat, String lng, int radius, Callback callback){
        HashMap<String, String> params = new HashMap<>();
        params.put("radius", radius+"");
        restClient.get("pokemons/"+lat+"/"+lng+"", params, callback);
    }

    public void autoGoogleLoader(Context context,String code, Callback callback){
        mContext = context;
        mGoogleLoginCallback = callback;
        mShouldLoginAPE = !(code == null || code.isEmpty());
        GoogleAuthTokenJson googleAuthTokenJson = PokemonHelper.getGoogleTokenJson(context);
        FormBody.Builder body = new FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("client_secret", SECRET);



        if(googleAuthTokenJson.getId_token() == null){
            body.add("code", code)
                    .add("grant_type", "authorization_code")
                    .add("redirect_uri", "http://127.0.0.1:9004");
        } else {
            String refreshToken = PokemonHelper.getGoogleRefreshToken(context);
            body.add("refresh_token", refreshToken)
                    .add("grant_type", "refresh_token");

        }
        RequestBody requestBody = body.build();
        Request request = new Request.Builder()
                .url(OAUTH_TOKEN_ENDPOINT)
                .method("POST", requestBody)
                .build();
        restClient.getClient().newCall(request).enqueue(googleOAuthCallback);
    }

    /******* Callbacks *******/
    final Callback googleOAuthCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            if(mGoogleLoginCallback != null)
                mGoogleLoginCallback.onFailure(call,e);
            mGoogleLoginCallback = null;
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            //check first if the request was ok
            if (response.code() == 200){
                String jsonStr = response.body().string();
                if(!jsonStr.isEmpty()) {
                    try {
                        GoogleAuthTokenJson googleAuthToken = new Gson().fromJson(jsonStr, GoogleAuthTokenJson.class);
                        googleAuthToken.setInit_time(System.currentTimeMillis()/1000);
                        //save the refresh token if it exists
                        if(googleAuthToken.getRefresh_token() != null)
                            PokemonHelper.saveGoogleRefreshToken(googleAuthToken.getRefresh_token(),mContext);
                        //save the token json with the init time
                        PokemonHelper.saveGoogleTokenJson(mContext, new Gson().toJson(googleAuthToken));
                        if(mShouldLoginAPE) {
                            mShouldLoginAPE = false;
                            login(mContext, mGoogleLoginCallback);
                        } else {
                            if(mGoogleLoginCallback != null)
                                mGoogleLoginCallback.onResponse(call,response);
                            mGoogleLoginCallback = null;
                        }
                        return;
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
                response.body().close();
            }
            if(mGoogleLoginCallback != null)
                mGoogleLoginCallback.onResponse(call,response);
            mGoogleLoginCallback = null;
        }
    };
}
