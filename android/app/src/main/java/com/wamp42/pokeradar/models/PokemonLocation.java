package com.wamp42.pokeradar.models;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class PokemonLocation {
    private String type = "coords";
    private String name = "0";
    private Coords coords;

    public PokemonLocation(Coords coords) {
        this.coords = coords;
    }
}
