package com.wamp42.pikapika.models;

import com.wamp42.pikapika.data.PokemonHelper;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class LoginData {
    private String username;
    private String password;
    private Provider provider;
    private PokemonLocation location;
    //private String provider = PokemonHelper.PTC_PROVIDER;

    public LoginData(String username, Provider provider, PokemonLocation location) {
        this.username = username;
        this.provider = provider;
        this.location = location;
    }

    public LoginData() {
        this.username = "";
        this.password = "";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

