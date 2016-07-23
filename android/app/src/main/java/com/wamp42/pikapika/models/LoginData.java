package com.wamp42.pikapika.models;

import com.wamp42.pikapika.data.PokemonHelper;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class LoginData {
    private String username;
    private String password;
    private PokemonLocation location;
    private String provider = PokemonHelper.PTC_PROVIDER;

    public LoginData(String username, String password, String provider,PokemonLocation location) {
        this.username = username;
        this.password = password;
        this.location = location;
        this.provider = provider;
    }

    public LoginData() {
        this.username = "";
        this.password = "";
        this.provider = "";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getProvider() {
        return provider;
    }
}

