package com.wamp42.pikapika.data;


import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.webkit.WebSettings;

import com.google.gson.Gson;
import com.wamp42.pikapika.models.GoogleAuthTokenJson;
import com.wamp42.pikapika.models.LoginData;
import com.wamp42.pikapika.models.PokemonLocation;
import com.wamp42.pikapika.models.PokemonToken;
import com.wamp42.pikapika.models.Provider;
import com.wamp42.pikapika.network.RestClient;
import com.wamp42.pikapika.utils.Debug;
import com.wamp42.pikapika.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

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
    private String mProvider = "google";

    public void loginWithToken(Context context,String token, String timeExpire , Location location, String loginType, Callback callback){
        //using android Id as username
        String androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        PokemonLocation pokemonLocation;
        if(location == null){
            pokemonLocation = new PokemonLocation(0,0, 0);
        } else {
            pokemonLocation = new PokemonLocation(location.getLatitude(),location.getLongitude(), location.getAltitude());
        }
        //hardcoding google provider
        LoginData loginData = new LoginData(androidId, new Provider("google",token,timeExpire), pokemonLocation);
        //convert object to json
        String jsonInString = new Gson().toJson(loginData);
        //do the request
        restClient.postJson(jsonInString,"trainers/login",callback);
        jsonInString = new Gson().toJson(loginData);
        PokemonHelper.saveDataLogin(context,jsonInString);
    }

    public void heartbeat(String token,String lat, String lng, Callback callback){
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", token);
        restClient.get("pokemons/"+lat+"/"+lng+"/heartbeat", params, callback);
    }

    public void autoGoogleLoader(Context context,String code, Callback callback){
        mContext = context;
        mGoogleLoginCallback = callback;
        RequestBody body = new FormBody.Builder()
                .add("code", code)
                .add("client_id", CLIENT_ID)
                .add("client_secret", SECRET)
                .add("redirect_uri", "http://127.0.0.1:9004")
                .add("grant_type", "authorization_code")
                .build();
        Request request = new Request.Builder()
                .url(OAUTH_TOKEN_ENDPOINT)
                .method("POST", body)
                .build();
        restClient.getClient().newCall(request).enqueue(googleOAuthCallback);
    }

    /*public void oauthGoogle(String user, String pass, String provider, Callback callback, Context context){
        mGoogleLoginCallback = callback;
        mContext = context;
        mUser = user;
        mPass = pass;
        mProvider = provider;

        // Google Parts
        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Locale locale = Locale.getDefault();
        String device_country = locale.getCountry();
        String country_code;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            country_code = tm.getSimCountryIso();
        } catch(Exception e) {
            country_code = device_country;
        }
        if (country_code == null)country_code = device_country;

        String language = locale.getLanguage();
        String skdVersion = Build.VERSION.SDK_INT + "";
        String user_agent = WebSettings.getDefaultUserAgent(context);

        RequestBody formBody = new FormBody.Builder()
                .add("accountType", "HOSTED_OR_GOOGLE")
                .add("Email", user)
                .add("has_permission", "1")
                .add("add_account", "1")
                .add("Passwd", pass)
                .add("service", "ac2dm")
                .add("source", "android")
                .add("androidId", android_id)
                .add("device_country", country_code)
                .add("operatorCountry", country_code)
                .add("lang", language)
                .add("sdk_version", skdVersion)
                .build();

        Request request = new Request.Builder()
                .url(AUTH_URL)
                .post(formBody)
                .header("User-Agent",user_agent)
                .build();
        restClient.getClient().newCall(request).enqueue(googleOAuthCallback);
    }

    private void oauthGoogleNiantic(String email,String master_token, Callback callback, Context context){

        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Locale locale = Locale.getDefault();
        String device_country = locale.getCountry();
        String country_code;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            country_code = tm.getSimCountryIso();
        } catch(Exception e) {
            country_code = device_country;
        }
        if (country_code == null)country_code = device_country;

        String language = locale.getLanguage();
        String skdVersion = Build.VERSION.SDK_INT + "";
        String user_agent = WebSettings.getDefaultUserAgent(context);

        String oauth_service = "audience:server:client_id:848232511240-7so421jotr2609rmqakceuu1luuq0ptb.apps.googleusercontent.com";
        String app = "com.nianticlabs.pokemongo";
        String client_sig = "321187995bc7cdc2b5fc91b11a96e2baa8602c62";

        RequestBody formBody = new FormBody.Builder()
                .add("accountType", "HOSTED_OR_GOOGLE")
                .add("Email", email)
                .add("EncryptedPasswd", master_token)
                .add("has_permission", "1")
                .add("service", oauth_service)
                .add("source", "android")
                .add("androidId", android_id)
                .add("app", app)
                .add("client_sig", client_sig)
                .add("device_country", country_code)
                .add("operatorCountry", country_code)
                .add("lang", language)
                .add("sdk_version", skdVersion)
                .build();

        Request request = new Request.Builder()
                .url(AUTH_URL)
                .post(formBody)
                .header("User-Agent",user_agent)
                .build();
        restClient.getClient().newCall(request).enqueue(callback);
    }*/

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
                        PokemonHelper.saveGoogleTokenJson(mContext, new Gson().toJson(googleAuthToken));
                        //request auth with niantic
                        String provider = mProvider;
                        loginWithToken(
                                mContext,
                                googleAuthToken.getId_token(),
                                googleAuthToken.getExpires_in()+"",
                                PokemonHelper.lastLocation,
                                provider,
                                mGoogleLoginCallback);
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


    /*final Callback googleOAuthCallback = new Callback() {
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
                    Debug.d(jsonStr);
                    try {
                        HashMap<String,String> formMap = Utils.getMapFromForm(jsonStr);
                        if(formMap.containsKey("Token")){
                            //request auth with niantic
                            String user = mUser;
                            oauthGoogleNiantic(user,formMap.get("Token"),nianticOAuthCallback,mContext);
                            return;
                        }
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

    final Callback nianticOAuthCallback = new Callback() {
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
                    Debug.d(jsonStr);
                    try {
                        HashMap<String,String> formMap = Utils.getMapFromForm(jsonStr);
                        if(formMap.containsKey("Auth")){
                            String authToken = formMap.get("Auth");
                            String tokenExpiredTime = formMap.get("Expiry");
                            PokemonToken pokemonToken = new PokemonToken(authToken,tokenExpiredTime, System.currentTimeMillis());
                            PokemonHelper.saveTokenData(mContext,pokemonToken);
                            //request auth with niantic
                            String user = mUser;
                            String provider = mProvider;
                            loginWithToken(
                                    mContext,
                                    user,
                                    authToken,
                                    tokenExpiredTime,
                                    PokemonHelper.lastLocation,
                                    provider,
                                    mGoogleLoginCallback);
                            return;
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
                response.body().close();
            }
            if(mGoogleLoginCallback != null)
                mGoogleLoginCallback.onFailure(call,new IOException("Code error "+response.code()));
            mGoogleLoginCallback = null;
        }
    };*/

}
