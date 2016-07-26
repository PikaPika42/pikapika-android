package com.wamp42.pikapika.models;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class PokemonLocation {
    private String type = "coords";
    private String name = "0";
    private Coords coords;

    private double latitude;
    private double longitude;
    private double altitude;

    public PokemonLocation(Coords coords) {
        this.coords = coords;
    }

    public PokemonLocation(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }
}
