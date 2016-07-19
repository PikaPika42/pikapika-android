package com.wamp42.pokeradar.data;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.wamp42.pokeradar.models.PokemonLocation;

import java.util.List;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class PokemonManager {
    static public void drawPokemonLocations(Context context, GoogleMap map, List<PokemonLocation> locationList){
        for(PokemonLocation location:locationList){
            //draw each pokemon mark
            location.drawMark(map, context);
        }
    }
}
