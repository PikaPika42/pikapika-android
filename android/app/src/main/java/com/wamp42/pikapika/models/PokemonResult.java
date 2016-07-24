package com.wamp42.pikapika.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wamp42.pikapika.R;
import com.wamp42.pikapika.data.PokemonHelper;

import java.util.Locale;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class PokemonResult {
    private double Latitude;
    private double Longitude;
    private int TimeTillHiddenMs;
    private PokeInfo pokemon;
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
        return pokemon;
    }

    public void drawMark(GoogleMap map, Context context){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(Latitude, Longitude));

        markerOptions.title(pokemon.getPokemonName());

        int [] secondsArray = splitToComponentTimes(TimeTillHiddenMs);
        String timeLeftStr = context.getString(R.string.time_left);
        String timeStr = String.format(Locale.ENGLISH,"%dm %ds",secondsArray[0],secondsArray[1]);
        markerOptions.snippet(timeLeftStr+": " + timeStr);

        //set the marker-icon
        String idStr = getStrId();
        int iconId = context.getResources().getIdentifier("pokemon_"+idStr+"", "drawable", context.getPackageName());
        if(iconId > 0) {
            Bitmap bitmapIcon = BitmapFactory.decodeResource(context.getResources(),iconId);
            float wt_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics());
            int iconSize = (int)wt_px;
            Bitmap resizedIcon = Bitmap.createScaledBitmap(bitmapIcon, iconSize, iconSize, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizedIcon));
        }
        Marker marker = map.addMarker(markerOptions);
        //maps the marker and the pokemon id
        PokemonHelper.markersMap.put(marker.getId(),idStr);
    }

    public void createInfoWindowAdapter(){
        new GoogleMap.InfoWindowAdapter () {
            @Override
            public View getInfoContents(Marker marker) {
                return null;

            }

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }
        };
    }

    public String getStrId() {
        String strId = String.valueOf(pokemon.getPokemonId());
        int length = strId.length();
        if(length == 1)
            strId = "00"+strId;
        else if (length == 2)
            strId = "0"+strId;
        return strId;
    }

    public static int[] splitToComponentTimes(long longVal)
    {
        int seconds = (int)longVal/1000;
        int minutes = seconds / 60;
        int remainder = seconds - minutes * 60;
        int secs = remainder;

        int[] ints = { minutes , secs};
        return ints;
    }
}
