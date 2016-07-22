package com.wamp42.pokeradar.data;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wamp42.pokeradar.models.Coords;
import com.wamp42.pokeradar.models.LoginData;
import com.wamp42.pokeradar.models.Pokemon;
import com.wamp42.pokeradar.models.PokemonLocation;
import com.wamp42.pokeradar.models.PokemonLocationData;
import com.wamp42.pokeradar.network.RestClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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

    static public List<PokemonLocation> getDummyPokemonsLocation(){
        List<PokemonLocation> list = new ArrayList<>();
        list.add(new PokemonLocation(0,"Flaviomon",100,20.6707593,-103.3686944));
        list.add(new PokemonLocation(12,"Pierremon",160,20.6710723,-103.3685152));
        list.add(new PokemonLocation(33,"Cesarmon",21,20.6717847,-103.3684512));
        return list;
    }

    public void requestPokemonsOnBackground(String url, final PokemonCallback<List<PokemonLocation>> pokemonCallback){
       Callback tempCallback = new Callback() {
           @Override
           public void onFailure(Call call, IOException e) {
               pokemonCallback.onFailure(call,e);
           }

           @Override
           public void onResponse(Call call, Response response) throws IOException {
                String jsonStr = response.body().string();
                Type listType = new TypeToken<List<PokemonLocation>>(){}.getType();
                List<PokemonLocation> pokemonLocationList = new Gson().fromJson(jsonStr, listType);
                pokemonCallback.onResponse(call,response,pokemonLocationList);
           }
       };
        restClient.get(url,tempCallback);
    }

    public void getPokemons(int lat, int lng, PokemonCallback<List<PokemonLocation>> pokemonCallback){
        requestPokemonsOnBackground("pokemon_data.json",pokemonCallback);
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
