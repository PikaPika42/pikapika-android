package com.wamp42.pokeradar.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.wamp42.pokeradar.data.PokemonManager;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class PokemonLocation {
    private int id;
    private String name;
    private int timeleft;
    public double lat;
    public double lng;

    public PokemonLocation(){}

    public PokemonLocation(int id, String name, int timeleft, double lat, double lng){
        this.id = id;
        this.timeleft = timeleft;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public void drawMark(GoogleMap map, Context context){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, lng));
        markerOptions.title(name+" - Time left: " + timeleft + "s");
        //set the marker-icon
        String idStr = getStrId();
        int iconId = context.getResources().getIdentifier("pokemon_"+idStr+"", "drawable", context.getPackageName());
        if(iconId > 0) {
            Bitmap bitmapIcon = BitmapFactory.decodeResource(context.getResources(),iconId);
            Bitmap resizedIcon = Bitmap.createScaledBitmap(bitmapIcon, 150, 150, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizedIcon));
        }
        Marker marker = map.addMarker(markerOptions);
        PokemonManager.markersMap.put(marker.getId(),idStr);
    }

    public String getStrId() {
        String strId = String.valueOf(id);
        int length = strId.length();
        if(length == 1)
            strId = "00"+strId;
        else if (length == 2)
            strId = "0"+strId;
        return strId;
    }

    public static PokemonLocation fromJson(String s) {
        return new Gson().fromJson(s, PokemonLocation.class);
    }
    public String toString() {
        return new Gson().toJson(this);
    }
}
