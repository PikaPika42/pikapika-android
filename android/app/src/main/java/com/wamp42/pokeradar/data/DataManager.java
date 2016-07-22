package com.wamp42.pokeradar.data;


import android.location.Location;

import com.google.gson.Gson;
import com.wamp42.pokeradar.models.Coords;
import com.wamp42.pokeradar.models.LoginData;
import com.wamp42.pokeradar.models.PokemonLocationData;
import com.wamp42.pokeradar.network.RestClient;

import okhttp3.Callback;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class DataManager {

    private static DataManager dataManagerInstance;
    private RestClient restClient = new RestClient();

    public static DataManager getDataManager(){
        if(dataManagerInstance == null)
            dataManagerInstance = new DataManager();
        return dataManagerInstance;
    }

    public void login(String user, String pass,Location location, Callback callback){
        Coords coords;
        if(location != null){
            coords = new Coords(location.getLatitude(),location.getLongitude());
        } else {
            coords = new Coords(20.670573,-103.368709);
        }
        PokemonLocationData pokemonLocation = new PokemonLocationData(coords);
        LoginData loginData = new LoginData(user,pass,pokemonLocation);
        String jsonInString = new Gson().toJson(loginData);
        restClient.postJson(jsonInString,"login",callback);
    }
}
