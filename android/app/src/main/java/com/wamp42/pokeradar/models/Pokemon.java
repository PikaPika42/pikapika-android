package com.wamp42.pokeradar.models;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class Pokemon {
    private String id;
    private String name;

    public Pokemon(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
