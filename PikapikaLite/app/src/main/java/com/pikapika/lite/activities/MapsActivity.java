package com.pikapika.lite.activities;

import android.Manifest;
import android.content.Context;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mopub.mobileads.MoPubView;
import com.pikapika.lite.BuildConfig;
import com.pikapika.lite.R;
import com.pikapika.lite.helpers.PokemonHelper;
import com.pikapika.lite.helpers.PokemonRequestHelper;
import com.pikapika.lite.helpers.SettingsSaving;
import com.pikapika.lite.models.PokemonResult;
import com.pikapika.lite.utils.Debug;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener,GoogleMap.OnMapClickListener,GoogleMap.OnCameraChangeListener {

    private static final int MY_LOCATION_REQUEST_CODE = 1001;
    private static final int CAMERA_MAP_ZOOM = 16;

    private static final String MOPUD_BANNER_UNIT_ID = "eb21a36f0eb14d59b660660d33585893";

    public boolean isMapReady = false;

    //map stuff
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private Marker currentMarker;
    private Handler markerHandler;

    //helpers
    private PokemonRequestHelper pokemonRequestHelper;
    //private ConfigReader configReader;
    public SettingsSaving settingsSaving;

    //Ads
    private MoPubView moPubView;

    //Firebase
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.setDebugLogActive(BuildConfig.DEBUG);

        setContentView(R.layout.activity_maps);
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

        //ADS
        moPubView = (MoPubView) findViewById(R.id.adview);
        moPubView.setAdUnitId(MOPUD_BANNER_UNIT_ID); //Ad Unit ID from www.mopub.com
        moPubView.loadAd();

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //helpers
        markerHandler = new Handler();
        pokemonRequestHelper = new PokemonRequestHelper(this);
        //configReader = new ConfigReader(this);
        settingsSaving = new SettingsSaving(this);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        pokemonRequestHelper.startQuickScanLoop();
        //configReader.requestConfig();
        PokemonHelper.cleanPokemon();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        pokemonRequestHelper.stopQuickScanLoop();
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
        isMapReady = true;
        mMap = googleMap;

        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnCameraChangeListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //startQuickSearchWithDelay();
            initMapResources();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                /* TODO:Show an explanation to the user *asynchronously* -- don't block
                    this thread waiting for the user's response! After the user
                     sees the explanation, try again to request the permission. */
                PokemonHelper.showAlert(this,getString(R.string.warning_title),getString(R.string.permissions_location_body));
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
                PokemonHelper.showAlert(this,getString(R.string.warning_title),getString(R.string.permissions_location_body));
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void initMapResources(){
        if(mMap == null || (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED) )
            return;

        mMap.setMyLocationEnabled(true);

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            //PokemonHelper.lastLocation = location;
            centerMapCamera(location);
        } else {
            //TODO: request position with other method and update map
            requestSingleLocationUpdate();
        }
    }

    public void centerMapCamera(Location location){
        if (location != null) {
            LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, CAMERA_MAP_ZOOM));
        }
    }

    public LatLng getLocation(){
        return  mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
    }

    public int getRadios(){
        LatLng left = mMap.getProjection().getVisibleRegion().farLeft;
        LatLng right = mMap.getProjection().getVisibleRegion().farRight;
        return (int)PokemonRequestHelper.distance(left.latitude,left.longitude,right.latitude,right.longitude);
    }

    public LatLngBounds getMapBounds(){
        return mMap.getProjection().getVisibleRegion().latLngBounds;
    }

    private void startQuickSearchWithDelay(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pokemonRequestHelper.startQuickScanLoop();
            }
        },1000);
    }

    /**
     * Try to call draw Pokemon in the main thread. This is because we are doing async request and
     * the map drawing must be don in the main thread.
     * @param pokemonList
     */
    public synchronized void addDrawPokemonOnMainThread(final List<PokemonResult> pokemonList, final boolean withAlert){
        if(mMap != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PokemonHelper.addToDrawPokemon(MapsActivity.this, mMap, pokemonList);
                }
            });
        }
    }

    final private Runnable markerRunnable = new Runnable() {
        @Override
        public void run() {
            if(currentMarker != null && PokemonHelper.markersPokemonMap.containsKey(currentMarker.getId())) {
                String key = PokemonHelper.markersPokemonMap.get(currentMarker.getId());
                PokemonResult pokemonResult = PokemonHelper.pokemonGlobalMap.get(key);
                if(pokemonResult.getTimeleft() > 0) {
                    long currentMilli = System.currentTimeMillis() - pokemonResult.getInitTime();
                    if(pokemonResult.getTimeleft() - currentMilli > 0 ) {
                        currentMarker.setSnippet(pokemonResult.getTimeleftParsed(MapsActivity.this, currentMilli));
                        currentMarker.showInfoWindow();
                    } else {
                        PokemonHelper.removePokemonMarker(currentMarker);
                        currentMarker = null;
                        markerHandler.removeCallbacks(this);
                        return;
                    }
                }
            }
            markerHandler.postDelayed(markerRunnable,1000);
        }
    };

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if(PokemonHelper.markersPokemonMap.containsKey(marker.getId())) {
            String key = PokemonHelper.markersPokemonMap.get(marker.getId());
            PokemonResult pokemonResult = PokemonHelper.pokemonGlobalMap.get(key);

            //if(PokemonHelper.getAudioSetting(this)) {
                //try to play the pokemon sound

                String audioName = "raw/pokemon_" + pokemonResult.getStrNumber();
                int soundId = getResources().getIdentifier(audioName , null, getPackageName());
                if (soundId > 0) {
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), soundId);
                    mp.start();
                }
            //}
            currentMarker = marker;
            markerHandler.removeCallbacks(markerRunnable);

            if(pokemonResult.getTimeleft() > 0) {
                long t = System.currentTimeMillis() - pokemonResult.getInitTime();
                marker.setSnippet(pokemonResult.getTimeleftParsed(MapsActivity.this, t));
                markerHandler.postDelayed(markerRunnable,1000);
            }
        }
        return false;
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

    @Override
    public void onMapClick(LatLng latLng) {
        markerHandler.removeCallbacks(markerRunnable);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Debug.Log("onCameraChange, request pokemones");
        pokemonRequestHelper.startQuickScanLoop();
    }
}
