package com.wamp42.pokeradar.models;

import com.wamp42.pokeradar.data.PokemonHelper;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class LoginData {
    private String username;
    private String password;
    private PokemonLocationData location;
    private String provider = PokemonHelper.PTC_PROVIDER;

    public LoginData(String username, String password, String provider,PokemonLocationData location) {
        this.username = username;
        this.password = password;
        this.location = location;
        this.provider = provider;
    }
}

