package com.wamp42.pikapika.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
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
import com.wamp42.pikapika.R;
import com.wamp42.pikapika.data.DataManager;
import com.wamp42.pikapika.data.PokemonHelper;
import com.wamp42.pikapika.models.LoginData;
import com.wamp42.pikapika.models.PokemonResult;
import com.wamp42.pikapika.models.PokemonToken;
import com.wamp42.pikapika.utils.Debug;

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
    private static final int REQUEST_TIME_OUT = 15000; //15 seg

    //map stuff
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    //view stuff
    DrawerLayout menuDrawerLayout;
    private ProgressDialog loadingProgressDialog;
    private Button searchButton;

    private boolean shouldRequestLogin = false;

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
        checkButtonText();
        MenuItem item = navigationView.getMenu().getItem(0);
        checkAudioSettings(item);
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

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            centerMapCamera(location);
        } else {
            //TODO: request position with other method and update map
            requestSingleLocationUpdate();
        }
    }

    private void centerMapCamera(Location location){
        if (location != null) {
            PokemonHelper.lastLocation = location;
            LatLng currentPos = new LatLng(PokemonHelper.lastLocation.getLatitude(), PokemonHelper.lastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 17));
        }
    }

    public void onMainActionClick(View view) {
        PokemonToken pokemonToken = PokemonHelper.getTokenFromData(this);
        if(pokemonToken.getAccessToken().isEmpty()){
            if(shouldRequestLogin){
                shouldRequestLogin = false;
                //request new token using the current credentials
                loginAgain();
            }else {
                //open login menu
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivityForResult(loginIntent, LOGIN_ACTIVITY_RESULT);
            }
        } else {
            Location location = null;
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED){
                location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                //makes sure we have a valid location
                if(location == null)
                    location = PokemonHelper.lastLocation;
            }

            if(location == null) {
                PokemonHelper.showAlert(this,getString(R.string.gps_error_title),getString(R.string.gps_error_body));
            } else {
                loadingProgressDialog = PokemonHelper.showLoading(this);
                if(PokemonHelper.pokemonResultList != null)
                    PokemonHelper.pokemonResultList.clear();
                DataManager.getDataManager().heartbeat(pokemonToken.getAccessToken(), location.getLatitude() + "", location.getLongitude() + "", heartbeatCallback);

                //dismiss the loading progress after a time out
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (loadingProgressDialog != null && loadingProgressDialog.isShowing()) {
                            loadingProgressDialog.dismiss();
                            PokemonHelper.showAlert(MapsActivity.this, getString(R.string.request_error_title) + "!",
                                    getString(R.string.request_error_body));
                        }
                    }
                }, REQUEST_TIME_OUT);
            }
        }
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
        PokemonToken pokemonToken = PokemonHelper.getTokenFromData(this);
        if(pokemonToken.getAccessToken().isEmpty()){
            //TODO:set text for navigation menu button
            searchButton.setText(getString(R.string.login));
        } else {
            searchButton.setText(getString(R.string.search_action));
        }
    }

    final Callback heartbeatCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
            //PokemonHelper.saveTokenData(MapsActivity.this,null);
            PokemonHelper.showAlert(MapsActivity.this,getString(R.string.request_error_title)+"!!",
                    getString(R.string.request_error_body));
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
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
                                PokemonHelper.pokemonResultList = resultList;
                                drawPokemonOnMainThread(PokemonHelper.pokemonResultList);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                response.body().close();
            } else {
                //clean credentials if the token was invalid
                if (response.code() >= 400) {
                    PokemonHelper.saveTokenData(MapsActivity.this, null);
                    shouldRequestLogin = true;
                }

                PokemonHelper.showAlert(MapsActivity.this,getString(R.string.request_error_title),
                        getString(R.string.request_error_body));
            }
        }
    };

    public void loginAgain() {
        //get the saved credentials
        LoginData loginData = PokemonHelper.getDataLogin(this);
        if(loginData != null) {
            //try to get the current location
            MapsActivity.getMapsActivity().requestLocation();
            //show a progress dialog
            loadingProgressDialog = PokemonHelper.showLoading(this);
            //request the pokemon data / login
            DataManager.getDataManager().login(this, loginData.getUsername(), loginData.getPassword(), PokemonHelper.lastLocation, loginData.getProvider(), loginCallback);
        }
    }

    /**** Login Callback ***/
    final Callback loginCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
            //clean data
            PokemonHelper.saveTokenData(MapsActivity.this,null);
            PokemonHelper.showAlert(MapsActivity.this,getString(R.string.request_error_title)+"!!",
                    getString(R.string.request_error_body));
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
            //check first if the request was ok
            if (response.code() == 200){
                String jsonStr = response.body().string();
                if(!jsonStr.isEmpty()) {
                    Debug.d(jsonStr);
                    try {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(jsonStr).getAsJsonObject();
                        if(jsonObject.has("data")) {
                            Type listType = new TypeToken<PokemonToken>() {}.getType();
                            PokemonToken pokemonToken = new Gson().fromJson(jsonObject.get("data").toString(), listType);
                            if(!pokemonToken.getAccessToken().isEmpty()) {
                                //save token
                                PokemonHelper.saveTokenData(MapsActivity.this,pokemonToken);
                                //call function of activity result to request pokemon
                                onActivityResult(LOGIN_ACTIVITY_RESULT,RESULT_OK,null);
                                return;
                            }
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
                response.body().close();
            } else {
                //clean the credentials saved
                PokemonHelper.saveTokenData(MapsActivity.this,null);
            }
            PokemonHelper.showAlert(MapsActivity.this,getString(R.string.request_error_title),
                    getString(R.string.request_error_body));
        }
    };

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(PokemonHelper.getAudioSetting(this) && PokemonHelper.markersMap.containsKey(marker.getId())) {
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
            //request pokemon
            onMainActionClick(new View(this));
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_logout:
                processLogoutNavButton(item);
                break;
            case R.id.menu_sound_option:
                PokemonHelper.saveAudioSetting(!PokemonHelper.getAudioSetting(this),this);
                checkAudioSettings(item);
                item.setChecked(false);
                break;
        }
        return true;
    }

    public void processLogoutNavButton(MenuItem item){
        PokemonToken pokemonToken = PokemonHelper.getTokenFromData(this);
        if(!pokemonToken.getAccessToken().isEmpty()){
            checkButtonText();
            menuDrawerLayout.closeDrawers();
            item.setTitle(getString(R.string.logout));
        } else {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent,LOGIN_ACTIVITY_RESULT);
        }
    }

    public void requestLocation() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED){
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(location != null)
                PokemonHelper.lastLocation = location;
        }
    }

    public void checkAudioSettings(MenuItem item){
        boolean audioActive = PokemonHelper.getAudioSetting(this);
        if(audioActive)
            item.setIcon(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_volume_up_black_24dp, null));
        else
            item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_volume_off_black_24dp, null));
    }

    private void requestSingleLocationUpdate() {
        Criteria criteria = new Criteria ();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        try {
            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestSingleUpdate(criteria, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Debug.Log(" LocationManager: requestSingleUpdate onLocationChanged = "+location.getLatitude()+ "," +location.getLongitude());
                    //center the camera to the new location
                    centerMapCamera(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            }, Looper.myLooper());
        } catch (SecurityException e){
            Debug.Log("Location security exception");
        }
    }
}
