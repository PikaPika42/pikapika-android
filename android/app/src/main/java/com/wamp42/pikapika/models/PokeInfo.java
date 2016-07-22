package com.wamp42.pikapika.models;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class PokeInfo {
    private String id;
    private String PokemonName;
    private String PokemonId;

    public PokeInfo () {}

    public String getPokemonName() {
        return PokemonName;
    }

    public String getPokemonId() {
        return PokemonId;
    }
}
