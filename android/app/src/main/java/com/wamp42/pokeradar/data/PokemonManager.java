package com.wamp42.pokeradar.data;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.wamp42.pokeradar.R;
import com.wamp42.pokeradar.models.PokemonLocation;

import java.util.HashMap;
import java.util.List;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class PokemonManager {

    static public HashMap<String,String> markersMap = new HashMap<>();

    static public void drawPokemonLocations(Context context, GoogleMap map, List<PokemonLocation> locationList){
        for(PokemonLocation location:locationList){
            //draw each pokemon mark
            location.drawMark(map, context);
        }
    }

    public static ProgressDialog showLoading(Context context) {
        return ProgressDialog.show(context, context.getString(R.string.please_wait), context.getString(R.string.loading_data), true);
    }
}
