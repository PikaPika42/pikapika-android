package com.wamp42.pokeradar.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wamp42.pokeradar.data.PokemonManager;

import java.util.Locale;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class PokemonResult {
    private double Latitude;
    private double Longitude;
    private int TimeTillHiddenMs;
    private PokeInfo pokeinfo;
    public PokemonResult(){}

    public double getLatitude() {
        return Latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public int getTimeTillHiddenMs() {
        return TimeTillHiddenMs;
    }

    public PokeInfo getPokeinfo() {
        return pokeinfo;
    }

    public void drawMark(GoogleMap map, Context context){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(Latitude, Longitude));
        int [] sedondsArray = splitToComponentTimes(TimeTillHiddenMs);
        String timeStr = String.format(Locale.ENGLISH,"%d:%d:%d",sedondsArray[0],sedondsArray[1],sedondsArray[2]);
        markerOptions.title(pokeinfo.getName()+"\\nTime left: " + timeStr);
        //set the marker-icon
        String idStr = getStrId();
        int iconId = context.getResources().getIdentifier("pokemon_"+idStr+"", "drawable", context.getPackageName());
        if(iconId > 0) {
            Bitmap bitmapIcon = BitmapFactory.decodeResource(context.getResources(),iconId);
            Bitmap resizedIcon = Bitmap.createScaledBitmap(bitmapIcon, 150, 150, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizedIcon));
        }
        Marker marker = map.addMarker(markerOptions);
        //maps the marker and the pokemon id
        PokemonManager.markersMap.put(marker.getId(),idStr);
    }

    public String getStrId() {
        String strId = String.valueOf(pokeinfo.getId());
        int length = strId.length();
        if(length == 1)
            strId = "00"+strId;
        else if (length == 2)
            strId = "0"+strId;
        return strId;
    }

    public static int[] splitToComponentTimes(long longVal)
    {
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return ints;
    }
}
