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
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.wamp42.pokeradar.R;
import com.wamp42.pokeradar.data.DataManager;
import com.wamp42.pokeradar.data.PokemonHelper;
import com.wamp42.pokeradar.models.PokemonResult;
import com.wamp42.pokeradar.utils.Debug;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener,NavigationView.OnNavigationItemSelectedListener {

    private static final int MY_LOCATION_REQUEST_CODE = 1001;
    private static final int LOGIN_ACTIVITY_RESULT = 101;

    //map stuff
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location lastLocation;

    //view stuff
    DrawerLayout menuDrawerLayout;
    private ProgressDialog loadingProgressDialog;
    private Button searchButton;

    //keep the pokemon result objects in memory
    private static List<PokemonResult> pokemonResultList;

    //static instance in order to set the pokemon result data from other activities
    public static MapsActivity staticMapActivity;
    public static MapsActivity getMapsActivity(){
        return staticMapActivity;
    }
    public GoogleApiClient getGoogleAPIClient(){
        return mGoogleApiClient;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        staticMapActivity = this;
        setContentView(R.layout.activity_maps);
        searchButton = (Button)findViewById(R.id.main_action_button);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
            searchButton.setText(getString(R.string.login));
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

    public static void setPokemonList(List<PokemonResult> resultList){
        pokemonResultList = resultList;
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

            initMapResources();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                //clean user data
                PokemonHelper.saveUserData(this,"","","");

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
        menuDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            String user = sharedPref.getString(PokemonHelper.USER_PARAMETER,"");
            String pass = sharedPref.getString(PokemonHelper.PASS_PARAMETER,"");
            String provider = sharedPref.getString(PokemonHelper.PROVIDER_PARAMETER,PokemonHelper.PTC_PROVIDER);
            Location location = null;
            if((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED)
                    && MapsActivity.getMapsActivity() != null){
                location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
            loadingProgressDialog = PokemonHelper.showLoading(this);
            if(pokemonResultList != null)
                pokemonResultList.clear();
            DataManager.getDataManager().login(user, pass, location,provider,loginCallback);
        }
    }

    public boolean isLogged(){
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String user = sharedPref.getString(PokemonHelper.USER_PARAMETER,"");
        return !user.isEmpty();
    }

    /**
     * Try to call drawPokemonLocations in the main thread. This is because we are doing async request and
     * the map drawing must be don in the main thread.
     * @param pokemonList
     */
    public void drawPokemonOnMainThread(final List<PokemonResult> pokemonList){
        if(mMap != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PokemonHelper.drawPokemonResult(MapsActivity.this, mMap, pokemonList);
                }
            });
        }
    }

    private void checkButtonText(){
        //check if the user is logged to change the button text
        if(!isLogged()){
            //TODO:set text for navigation menu button
            searchButton.setText(getString(R.string.login));
        } else {
            searchButton.setText(getString(R.string.search_action));
        }
    }

    final Callback loginCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
            PokemonHelper.showAlert(MapsActivity.this,getString(R.string.request_error_title)+"!!",
                    getString(R.string.request_error_body));
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

            if (response.code() == 200) {
                String jsonStr = response.body().string();
                if (!jsonStr.isEmpty()) {
                    Debug.Log("login response: "+jsonStr);
                    Type listType = new TypeToken<List<PokemonResult>>() {
                    }.getType();
                    try {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(jsonStr).getAsJsonObject();
                        if(jsonObject.has("data")){
                            List<PokemonResult> resultList = new Gson().fromJson(jsonObject.get("data").toString(), listType);
                            if (resultList != null) {
                                pokemonResultList = resultList;
                                drawPokemonOnMainThread(pokemonResultList);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                PokemonHelper.showAlert(MapsActivity.this,getString(R.string.request_error_title),
                        getString(R.string.request_error_body));
            }
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
        }
    };

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(PokemonHelper.markersMap.containsKey(marker.getId())) {
            //try to play the pokemon sound
            String audioName = "raw/pokemon_" + PokemonHelper.markersMap.get(marker.getId());
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
            checkButtonText();
            //try to paint the markers
            if(pokemonResultList != null){
                drawPokemonOnMainThread(pokemonResultList);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_logout:
                processLoginNavButton();
                break;
            case R.id.nav_settings:
                //TODO:open settings activity
                break;
        }
        return true;
    }

    public void processLoginNavButton(){
        if(isLogged()) {
            //clean user data
            PokemonHelper.saveUserData(this, "", "", "");
            checkButtonText();
            menuDrawerLayout.closeDrawers();
        } else {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent,LOGIN_ACTIVITY_RESULT);
        }
    }
}
