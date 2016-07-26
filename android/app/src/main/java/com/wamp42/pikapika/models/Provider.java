package com.wamp42.pikapika.models;

/**
 * Created by flavioreyes on 7/25/16.
 */
public class Provider {
    String name = "google";
    String token = "";
    String expireTime = "";

    public Provider(String name, String token, String expireTime) {
        this.name = name;
        this.token = token;
        this.expireTime = expireTime;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public String getExpireTime() {
        return expireTime;
    }
}
