package com.wamp42.pokeradar.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wamp42.pokeradar.R;
import com.wamp42.pokeradar.data.DataManager;
import com.wamp42.pokeradar.data.PokemonCallback;
import com.wamp42.pokeradar.data.PokemonManager;
import com.wamp42.pokeradar.models.PokemonLocation;
import com.wamp42.pokeradar.models.PokemonResult;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener {

    private static final int MY_LOCATION_REQUEST_CODE = 1001;
    private static final int LOGIN_ACTIVITY_RESULT = 101;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location lastLocation;

    private ProgressDialog loadingProgressDialog;
    private Button searchButon;

    private static List<PokemonResult> pokemonResultList;

    public static MapsActivity staticMapActivity;

    public static MapsActivity getMapsActivity(){
        return staticMapActivity;
    }

    public GoogleApiClient getGoolgeAPIclient(){
        return mGoogleApiClient;
    }

    public void setPokemonList(List<PokemonResult> resultList){
        pokemonResultList = resultList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        staticMapActivity = this;
        setContentView(R.layout.activity_maps);
        searchButon = (Button)findViewById(R.id.main_action_button);

        initMenu();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if(!isLogged()){
            searchButon.setText(getString(R.string.login));
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setOnMarkerClickListener(this);
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Add a marker in a city and move the camera
            //LatLng gdlLocation = new LatLng(20.666, -103.3685);
            //mMap.addMarker(new MarkerOptions().position(gdlLocation).title("Pokemon Team in Gdl"));
            //set the camera in the position and with a zoom value
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gdlLocation,15));

            initMapResources();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                /* TODO:Show an explanation to the user *asynchronously* -- don't block
                    this thread waiting for the user's response! After the user
                     sees the explanation, try again to request the permission. */

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //enable map location and set the current position
                    initMapResources();
            } else {
                // TODO: Permission was denied. Display an error message.
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initMapResources();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void initMenu(){
        //Menu settings
        final DrawerLayout menuDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(menuDrawerLayout != null) {
            Button drawerButton = (Button) findViewById(R.id.button_drawer_menu);
            drawerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!menuDrawerLayout.isDrawerOpen(GravityCompat.START))
                        menuDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
    }

    private void initMapResources(){
        if(mMap == null || (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED) )
            return;

        mMap.setMyLocationEnabled(true);

        lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            LatLng currentPos = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 17));
        } else {
            //request position and update map
        }
    }

    public void onMainActionClick(View view) {
        if(!isLogged()){
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent,LOGIN_ACTIVITY_RESULT);
        } else {
            SharedPreferences sharedPref = getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String user = sharedPref.getString("user","");
            String pass = sharedPref.getString("pass","");
            Location location = null;
            if((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED)
                    && MapsActivity.getMapsActivity() != null){
                location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
            loadingProgressDialog = PokemonManager.showLoading(this);
            DataManager.getDataManager().login(user, pass, location,loginCallback);
        }
    }

    public boolean isLogged(){
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String user = sharedPref.getString("user","");
        return !user.isEmpty();
    }

    /**
     * Try to call drawPokemonLocations in the main thread. This is because we are doing async request and
     * the map drawing must be don in the main thread.
     * @param pokemonList
     */
    public void drawPokemonListOnMainThread(final List<PokemonLocation> pokemonList){
        if(mMap != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PokemonManager.drawPokemonLocations(MapsActivity.this, mMap, pokemonList);
                    //add a circle
                    /*if (lastLocation != null) {
                        mMap.addCircle(new CircleOptions()
                                .center(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                                .radius(500) //radio in meters
                                .strokeColor(ContextCompat.getColor(MapsActivity.this,R.color.colorPrimary))
                                .strokeWidth(5)
                                .fillColor(0x5500b6ff));
                    }*/
                }
            });
        }
    }

    public void drawPokemonOnMainThread(final List<PokemonResult> pokemonList){
        if(mMap != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PokemonManager.drawPokemonResult(MapsActivity.this, mMap, pokemonList);
                }
            });
        }
    }

    final PokemonCallback<List<PokemonLocation>> pokemonCallback = new PokemonCallback<List<PokemonLocation>>() {
        @Override
        public void onFailure(Call call, IOException e) {
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
            //TODO: show error message

        }

        @Override
        public void onResponse(Call call, Response response, List<PokemonLocation> pokemonList) throws IOException {
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
            drawPokemonListOnMainThread(pokemonList);
        }
    };

    final Callback loginCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

            if (response.code() == 200) {
                String jsonStr = response.body().string();
                if (!jsonStr.isEmpty()) {
                    Type listType = new TypeToken<List<PokemonResult>>() {
                    }.getType();
                    try {
                        List<PokemonResult> resultList = new Gson().fromJson(jsonStr, listType);
                        if (resultList != null) {
                            MapsActivity.getMapsActivity().setPokemonList(resultList);
                            drawPokemonOnMainThread(pokemonResultList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
        }
    };

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(PokemonManager.markersMap.containsKey(marker.getId())) {
            //try to play the pokemon sound
            String audioName = "raw/pokemon_" + PokemonManager.markersMap.get(marker.getId());
            int soundId = getResources().getIdentifier(audioName , null, getPackageName());
            if (soundId > 0) {
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), soundId);
                mp.start();
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_ACTIVITY_RESULT && resultCode == RESULT_OK) {
            if(!isLogged()){
                searchButon.setText(getString(R.string.login));
            } else {
                searchButon.setText(getString(R.string.search_action));
            }
            if(pokemonResultList != null){
                drawPokemonOnMainThread(pokemonResultList);
            }
        }
    }
}
