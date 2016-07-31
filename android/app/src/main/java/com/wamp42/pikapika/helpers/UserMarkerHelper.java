package com.wamp42.pikapika.helpers;

import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wamp42.pikapika.R;
import com.wamp42.pikapika.activities.MapsActivity;
import com.wamp42.pikapika.utils.Utils;

/**
 * Created by flavioreyes on 7/30/16.
 */
public class UserMarkerHelper implements  GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener{
    public MapsActivity mMapsActivity;
    public GoogleMap mMap;
    private Marker userMarker;
    private Button cancelMarkerButton;

    private Boolean userMarkerButtonClicked = false;

    public UserMarkerHelper(MapsActivity mapsActivity, GoogleMap mMap) {
        cancelMarkerButton = (Button)mapsActivity.findViewById(R.id.button_location_marker);
        cancelMarkerButton.setOnClickListener(userMarkerListener);
        this.mMapsActivity = mapsActivity;
        this.mMap = mMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(userMarker != null)
            userMarker.remove();
        createMarker(latLng);
        userMarkerButtonClicked = true;
        cancelMarkerButton.setBackground(ResourcesCompat.getDrawable(mMapsActivity.getResources(),R.drawable.ic_location_off_black_36dp, null));
    }

    public void createMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin_yellow_36))
                .draggable(true);
        userMarker = mMap.addMarker(markerOptions);
    }

    private View.OnClickListener userMarkerListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(userMarkerButtonClicked) {
                removeMarker();
                cancelMarkerButton.setBackground(ResourcesCompat.getDrawable(mMapsActivity.getResources(), R.drawable.ic_location_on_black_36dp, null));
            } else {
                mMapsActivity.requestLocation();
                if (PokemonHelper.lastLocation != null) {
                    createMarker(new LatLng(PokemonHelper.lastLocation.getLatitude(), PokemonHelper.lastLocation.getLongitude()));
                    mMapsActivity.centerMapCamera(PokemonHelper.lastLocation);
                    cancelMarkerButton.setBackground(ResourcesCompat.getDrawable(mMapsActivity.getResources(),R.drawable.ic_location_off_black_36dp, null));
                }
            }
            userMarkerButtonClicked = !userMarkerButtonClicked;
        }
    };

    public void removeMarker(){
        if(userMarker != null)
            userMarker.remove();
        userMarker = null;
    }

    public LatLng getLocation(){
        if(userMarker != null)
            return userMarker.getPosition();
        else
            return null;
    }

    public boolean isRecommendeDistance(LatLng oldLocation){
        if(userMarker != null){
            double distanceMiles = Utils.locationDistance(oldLocation.latitude,
                    oldLocation.longitude,
                    userMarker.getPosition().latitude,
                    userMarker.getPosition().longitude);
            return distanceMiles < 100;
        }
        return true;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
}
