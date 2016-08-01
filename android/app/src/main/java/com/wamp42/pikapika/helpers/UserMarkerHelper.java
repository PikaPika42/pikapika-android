package com.wamp42.pikapika.helpers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Location;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    final public double MAX_DISTANCE_NEW_POSITION = 0.44;

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
       if(!isOnRange(latLng))
           return;
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
            showInstruction();
            if(userMarkerButtonClicked) {
                removeMarker();
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
        cancelMarkerButton.setBackground(ResourcesCompat.getDrawable(mMapsActivity.getResources(), R.drawable.ic_location_on_black_36dp, null));
    }

    public LatLng getLocation(){
        if(userMarker != null)
            return userMarker.getPosition();
        else
            return null;
    }

    public boolean isRecommendedDistance(LatLng oldLocation){
        if(userMarker != null){
            double distanceMiles = Utils.locationDistance(oldLocation.latitude,
                    oldLocation.longitude,
                    userMarker.getPosition().latitude,
                    userMarker.getPosition().longitude);
            return distanceMiles < 50;
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
        LatLng latLng = marker.getPosition();
        isOnRange(latLng);

    }

    private boolean isOnRange(LatLng newLocation){

        LatLng lastValidLocation = PokemonRequestHelper.lastLocationRequested;
        if(lastValidLocation == null) {
            Location location = PokemonHelper.lastLocation;
            if(location != null)
                lastValidLocation = new LatLng(location.getLatitude(),location.getLongitude());
        }

        if(lastValidLocation == null) {
            PokemonHelper.showAlert(mMapsActivity,mMapsActivity.getString(R.string.gps_error_title),mMapsActivity.getString(R.string.gps_error_body));
            return false;
        }

        if(Utils.locationDistance(newLocation.latitude,newLocation.longitude,lastValidLocation.latitude,lastValidLocation.longitude) > MAX_DISTANCE_NEW_POSITION ) {
            removeMarker();
            userMarkerButtonClicked = false;
            PokemonHelper.showAlert(mMapsActivity,mMapsActivity.getString(R.string.warning_title),
                    mMapsActivity.getString(R.string.warning_banning_location));
            return false;
        }

        return true;
    }

    public void showInstruction(){
        if(PokemonHelper.getPositionInstructionShown(mMapsActivity))
            return;
        PokemonHelper.savePositionInstructionShown(true,mMapsActivity);
        Dialog dialog = new Dialog(mMapsActivity);
        dialog.setContentView(R.layout.pop_up_splash_view);
        TextView mainTexView = (TextView)dialog.findViewById(R.id.text_view_pop_up);
        TextView emptyTexView = (TextView)dialog.findViewById(R.id.textView);
        emptyTexView.setText("");
        mainTexView.setText(mMapsActivity.getString(R.string.change_position_instruction));
        dialog.show();
    }
}
