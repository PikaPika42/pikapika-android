package com.wamp42.pikapika.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wamp42.pikapika.R;
import com.wamp42.pikapika.models.GoogleAuthTokenJson;
import com.wamp42.pikapika.models.PokemonResult;
import com.wamp42.pikapika.models.PokemonToken;
import com.wamp42.pikapika.utils.Utils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class PokemonHelper {
    final public static String GOOGLE_PROVIDER = "google";
    final public static String PTC_PROVIDER = "ptc";
    final public static String DATA_LOGIN = "data_login";
    final public static String DATA_GOOGLE_TOKEN = "google_auth_token";
    final public static String DATA_GOOGLE_CODE = "google_auth_code";
    final public static String DATA_GOOGLE_REFRESH_TOKEN = "google_refresh_token";
    final public static String TOKEN_PARAMETER = "accessToken";
    final public static String EXPIRE_TIME_PARAMETER = "expire_time";
    final public static String INIT_TIME_PARAMETER = "init_time";

    final public static String AUDIO_SETTING        = "audio_setting";
    final public static String AUTO_SEARCH_SETTING        = "audio_setting";
    final public static String FIRST_LAUNCH        = "firstLaunch";

    //map <marker Id, pokemon Id>
    static public HashMap<String,String> markersPokemonMap = new HashMap<>();
    //map <pokemon Id, Marker>
    static public HashMap<String,Marker> pokemonMarkersMap = new HashMap<>();
    //keep the pokemon result objects in memory
    static public HashMap<String,PokemonResult> pokemonGlobalMap = new HashMap<>();
    //share the lastValidLocation;
    static public Location lastLocation;

    static public void addToDrawPokemon(Context context, GoogleMap map, List<PokemonResult> locationList){

        cleanPokemon();

        //draw the new pokemon
        for(PokemonResult pokemon:locationList) {
            pokemonGlobalMap.put(pokemon.getUniqueId(),pokemon);
            pokemon.drawMark(map, context);
        }
    }

    static public void cleanPokemon(){
        //check which pokemon has a valid time, in oder case it is removed
        Iterator it = pokemonGlobalMap.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            String pokemonKey = (String)pair.getKey();
            PokemonResult pokemon =  pokemonGlobalMap.get(pokemonKey);
            if(pokemon.getTimeleft() > 0 && pokemon.getInitTime() > 0){
                long currentMilli = System.currentTimeMillis() - pokemon.getInitTime();
                if(pokemon.getTimeleft() - currentMilli <= 0 ){
                    //remove pokemon
                    it.remove();
                    //remove marker
                    if(pokemonMarkersMap.containsKey(pokemonKey)) {
                        Marker marker = pokemonMarkersMap.get(pokemonKey);
                        String markerId = marker.getId();
                        marker.remove();
                        pokemonMarkersMap.remove(pokemonKey);
                        markersPokemonMap.remove(markerId);
                    }
                }
            }
        }
    }

    public static ProgressDialog showLoading(Context context) {
        return ProgressDialog.show(context, context.getString(R.string.please_wait), context.getString(R.string.loading_data), true);
    }

    public static ProgressDialog showLoading(Context context, String title, String body) {
        return ProgressDialog.show(context, title, body, true);
    }

    public static void showAlert(final Activity activity, final String title, final String body) {
        activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(body)
                            .setTitle(title);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
    }

    public static void saveTokenData(Context context, PokemonToken pokemonToken){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(pokemonToken != null) {
            editor.putString(TOKEN_PARAMETER, pokemonToken.getAccessToken());
            editor.putString(EXPIRE_TIME_PARAMETER, pokemonToken.getExpire_time());
            editor.putLong(INIT_TIME_PARAMETER, pokemonToken.getInitTime());
        } else {
            editor.putString(TOKEN_PARAMETER, "");
            editor.putString(EXPIRE_TIME_PARAMETER, "");
            editor.putLong(INIT_TIME_PARAMETER, 0);
        }
        editor.apply();
    }

    public static void saveDataLogin(Context context, String jsonLoginData){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(jsonLoginData != null) {
            //encrypt the user data
            String encryptedData = Utils.encryptIt(jsonLoginData);
            editor.putString(DATA_LOGIN,encryptedData);
        } else {
            editor.putString(DATA_LOGIN, "");
        }
        editor.apply();
    }

    public static void saveAudioSetting(boolean active, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(AUDIO_SETTING, active);
        editor.apply();
    }

    public static boolean getAudioSetting(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(PokemonHelper.AUDIO_SETTING,true);
    }

    public static void saveAutoSearchSetting(boolean active, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(AUTO_SEARCH_SETTING, active);
        editor.apply();
    }

    public static boolean getAutoSearchSetting(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(PokemonHelper.AUTO_SEARCH_SETTING,true);
    }

    public static void saveFirstLaunch(boolean first, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(FIRST_LAUNCH, first);
        editor.apply();
    }

    public static boolean isFirstLaunch(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(PokemonHelper.FIRST_LAUNCH,true);
    }

    public static void saveGoogleTokenJson(Context context, String jsonToken){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(jsonToken != null) {
            //encrypt the user data
            String encryptedData = Utils.encryptIt(jsonToken);
            editor.putString(DATA_GOOGLE_TOKEN,encryptedData);
        } else {
            editor.putString(DATA_GOOGLE_TOKEN, "");
        }
        editor.apply();
    }

    public static GoogleAuthTokenJson getGoogleTokenJson(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String json = sharedPref.getString(DATA_GOOGLE_TOKEN,"");
        if(json.isEmpty())
            return new GoogleAuthTokenJson();
        //decrypt the user data
        String decryptedData = Utils.decryptIt(json);
        try {
            Type listType = new TypeToken<GoogleAuthTokenJson>() {}.getType();
            GoogleAuthTokenJson authTokenJson = new Gson().fromJson(decryptedData, listType);
            return authTokenJson;
        } catch (Exception e){
            e.printStackTrace();
            return new GoogleAuthTokenJson();
        }
    }

    public static void saveGoogleRefreshToken(String code, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(DATA_GOOGLE_REFRESH_TOKEN, code);
        editor.apply();
    }

    public static String getGoogleRefreshToken(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString(PokemonHelper.DATA_GOOGLE_REFRESH_TOKEN,"");
    }
}
