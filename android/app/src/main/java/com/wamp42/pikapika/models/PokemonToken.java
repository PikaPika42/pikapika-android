package com.wamp42.pikapika.models;

/**
 * Created by flavioreyes on 7/22/16.
 */
public class PokemonToken {
    private String access_token = "";
    private String expire_time = "";
    private String initTime = "";

    public PokemonToken(){}

    public PokemonToken(String accessToken, String expire_time, String initTime) {
        this.access_token = accessToken;
        this.expire_time = expire_time;
        this.initTime = initTime;
    }

    public String getAccessToken() {
        return access_token;
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
