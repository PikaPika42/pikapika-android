package com.wamp42.pokeradar.models;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class PokemonLocationData {
    private String type = "coords";
    private String name = "0";
    private Coords coords;

    public PokemonLocationData(Coords coords) {
        this.coords = coords;
    }
}
