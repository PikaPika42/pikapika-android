package com.wamp42.pokeradar.models;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class LoginData {
    private String username;
    private String password;
    private PokemonLocationData location;
    private String provider = "ptc";

    public LoginData(String username, String password, PokemonLocationData location) {
        this.username = username;
        this.password = password;
        this.location = location;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public PokemonLocationData getLocation() {
        return location;
    }
}

