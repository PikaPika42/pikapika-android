package com.wamp42.pokeradar.models;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class Coords {
    private double latitude;
    private double longitude;
    private double altitude = 0;

    public Coords(double lat, double lgd){
        this.latitude = lat;
        this.longitude = lgd;
    }
}
