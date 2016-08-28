package com.pikapika.lite.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.pikapika.lite.R;
import com.pikapika.lite.models.PokemonResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class PokemonHelper {

    //map <marker Id, pokemon Id>
    static public HashMap<String,String> markersPokemonMap = new HashMap<>();
    //map <pokemon Id, Marker>
    static public HashMap<String,Marker> pokemonMarkersMap = new HashMap<>();
    //keep the pokemon result objects in memory
    static public HashMap<String,PokemonResult> pokemonGlobalMap = new HashMap<>();
    //share the lastValidLocation;
    static public Location lastLocation;

    static public void addToDrawPokemon(Context context, GoogleMap map, List<PokemonResult> locationList){

        //draw the new pokemon
        for(PokemonResult pokemon:locationList) {
            //if doesn't exist we added to map
            if(!pokemonGlobalMap.containsKey(pokemon.getUniqueId())) {
                pokemonGlobalMap.put(pokemon.getUniqueId(), pokemon);
                pokemon.drawMark(map, context);
            } else if(!pokemon.isFromQuickScan()){
                /**
                 * A pokemon from normal heartbeat has priority form one from quickScan.
                 * So we remove the quickScan pokemon and we save the new one.
                 */
                //remove the old one
                String pokemonKey = pokemon.getUniqueId();
                Marker marker = pokemonMarkersMap.get(pokemonKey);
                String markerId = marker.getId();
                pokemonMarkersMap.remove(pokemonKey);
                markersPokemonMap.remove(markerId);
                marker.remove();
                pokemonGlobalMap.remove(pokemonKey);
                //add the new one
                pokemonGlobalMap.put(pokemon.getUniqueId(), pokemon);
                pokemon.drawMark(map, context);
            }
        }
    }

    static public void cleanPokemon(){
        //check which pokemon has a valid time, in oder case it is removed
        Iterator it = pokemonGlobalMap.entrySet().iterator();
        long currentTimeMillis = System.currentTimeMillis();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            String pokemonKey = (String)pair.getKey();
            PokemonResult pokemon =  pokemonGlobalMap.get(pokemonKey);
            if(pokemon.getTimeleft() > 0 && pokemon.getInitTime() > 0){
                long currentMilli = currentTimeMillis - pokemon.getInitTime();
                if(pokemon.getTimeleft() - currentMilli <= 0 ){
                    removePokemon(it,pokemonKey);
                }
            }
        }
    }

    private static void removePokemon(Iterator it,String pokemonKey){
        //remove pokemon
        if(it != null)
            it.remove();
        //remove marker
        if(pokemonMarkersMap.containsKey(pokemonKey)) {
            Marker marker = pokemonMarkersMap.get(pokemonKey);
            String markerId = marker.getId();
            pokemonMarkersMap.remove(pokemonKey);
            markersPokemonMap.remove(markerId);
            marker.remove();
        }
    }

    public static void removePokemonMarker(Marker marker){
        String markerId = marker.getId();
        String pokemonKey = markersPokemonMap.get(markerId);
        pokemonMarkersMap.remove(pokemonKey);
        markersPokemonMap.remove(markerId);
        marker.remove();
        pokemonGlobalMap.remove(pokemonKey);
    }

    public static ProgressDialog showLoading(Context context) {
        return ProgressDialog.show(context, context.getString(R.string.please_wait), context.getString(R.string.loading_data), true);
    }

    public static ProgressDialog showLoading(Context context, String title, String body) {
        return ProgressDialog.show(context, title, body, true);
    }

    public static void showAlert(final Activity activity, final String title, final String body) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(body)
                        .setTitle(title);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}
