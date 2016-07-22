package com.wamp42.pikapika.models;

/**
 * Created by flavioreyes on 7/22/16.
 */
public class PokemonToken {
    private String accessToken = "";
    private String expire_time = "";
    private String initTime = "";

    public PokemonToken(){}

    public PokemonToken(String accessToken, String expire_time, String initTime) {
        this.accessToken = accessToken;
        this.expire_time = expire_time;
        this.initTime = initTime;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getExpire_time() {
        return expire_time;
    }


    public String getInitTime() {
        return initTime;
    }

    public void setInitTime(String initTime) {
        this.initTime = initTime;
    }
}
