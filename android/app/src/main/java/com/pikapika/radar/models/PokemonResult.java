package com.pikapika.radar.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.util.TypedValue;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pikapika.radar.R;
import com.pikapika.radar.activities.MapsActivity;
import com.pikapika.radar.helpers.PokemonHelper;

import java.util.Locale;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class PokemonResult {

    final public static int POKEMON_SIZE = 60;

    //new parameters
    private String id;
    private String number;
    private String name;
    private PokemonPosition position;
    private int timeleft = 0;

    private long initTime = 0;
    private boolean fromQuickScan = false;

    private long expirationTimestampMs;

    private String createdAt;
    private String expireAt;

    public PokemonResult(){}

    public void drawMark(GoogleMap map, Context context){
        if(position == null)
            return;
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(position.getLat(), position.getLng()));

        markerOptions.title(name);
        int[] secondsArray = splitToComponentTimes(timeleft);
        String timeLeftStr = context.getString(R.string.time_left);
        String timeStr = String.format(Locale.ENGLISH, "%dm %ds", secondsArray[0], secondsArray[1]);
        markerOptions.snippet(timeLeftStr + ": " + timeStr);

        //set the marker-icon
        String idStr = getStrNumber();
        int iconId = context.getResources().getIdentifier("pokemon_"+idStr+"", "drawable", context.getPackageName());
        if(iconId > 0) {
            Bitmap bitmapIcon = BitmapFactory.decodeResource(context.getResources(),iconId);
            float wt_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, POKEMON_SIZE, context.getResources().getDisplayMetrics());
            int iconSize = (int)wt_px;
            Bitmap resizedIcon = Bitmap.createScaledBitmap(bitmapIcon, iconSize, iconSize, false);
            if(fromQuickScan)
                resizedIcon = changeBitmapColor(resizedIcon, 0xacb2d8);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizedIcon));
        }
        Marker marker = map.addMarker(markerOptions);
        //maps the marker and the pokemon id
        PokemonHelper.markersPokemonMap.put(marker.getId(),id);
        PokemonHelper.pokemonMarkersMap.put(id,marker);
        initTime = System.currentTimeMillis();
    }

    public String getStrNumber() {
        String pokemonNumber = number;
        String strId = String.valueOf(pokemonNumber);
        int length = strId.length();
        if(length == 1)
            strId = "00"+strId;
        else if (length == 2)
            strId = "0"+strId;
        return strId;
    }


    public void setTimeleft(int timeleft) {
        this.timeleft = timeleft;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public int getTimeleft() {
        return timeleft;
    }

    public long getInitTime() {
        return initTime;
    }

    public String getName() {
        return name;
    }

    public String getUniqueId(){
        return id;
    }

    public String getTimeleftParsed(Context context,long currentMilli) {
        long realTimeLeft;
        if(MapsActivity.USER_JAVA_LIB && timeleft == 0){
            realTimeLeft = expirationTimestampMs - currentMilli;
        } else {
            realTimeLeft = timeleft - currentMilli;
        }
        if(realTimeLeft > 0) {
            int[] secondsArray = splitToComponentTimes(realTimeLeft);
            String timeLeftStr = context.getString(R.string.time_left);
            String timeStr = String.format(Locale.ENGLISH, "%dm %ds", secondsArray[0], secondsArray[1]);
            return timeLeftStr + ": " + timeStr;
        }
        return context.getString(R.string.it_has_gone);
    }

    public void setExpirationTimestampMs(long expirationTimestampMs) {
        this.expirationTimestampMs = expirationTimestampMs;
    }

    public long getExpirationTimestampMs() {
        return expirationTimestampMs;
    }

    public boolean isFromQuickScan() {
        return fromQuickScan;
    }

    public void setFromQuickScan(boolean quickScan) {
        this.fromQuickScan = quickScan;
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

    private Bitmap changeBitmapColor(Bitmap sourceBitmap, int color) {

        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0,
                sourceBitmap.getWidth() - 1, sourceBitmap.getHeight() - 1);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 1);
        p.setColorFilter(filter);

        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, p);
        return resultBitmap;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(PokemonPosition position) {
        this.position = position;
    }
}
