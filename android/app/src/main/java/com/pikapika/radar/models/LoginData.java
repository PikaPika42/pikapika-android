package com.pikapika.radar.models;


/**
 * Created by flavioreyes on 7/21/16.
 */
public class LoginData {
    private String username;
    //new fields for new login version
    private String device_unique_id;
    private String provider;

    public LoginData(String device_id, String provider){
        this.device_unique_id = device_id;
        this.provider = provider;
    }

    public LoginData() {
        this.username = device_unique_id = "";
    }
}

