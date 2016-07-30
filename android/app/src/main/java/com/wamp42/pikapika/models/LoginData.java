package com.wamp42.pikapika.models;


import android.util.Log;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class LoginData {
    private String username;
    //private Provider provider;
    private PokemonLocation location;
    //private String provider = PokemonHelper.PTC_PROVIDER;

    //new fields for new login version
    private String device_unique_id;
    private String provider;


    public LoginData(String username, Provider provider, PokemonLocation location) {
        this.username = username;
        //this.provider = provider;
        this.location = location;
    }

    public LoginData(String device_id, String provider){
        this.device_unique_id = device_id;
        this.provider = provider;
    }

    public LoginData() {
        this.username = device_unique_id = "";
    }
}

