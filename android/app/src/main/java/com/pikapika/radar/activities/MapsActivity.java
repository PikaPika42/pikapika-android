package com.pikapika.radar.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
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
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import com.pikapika.radar.BuildConfig;
import com.pikapika.radar.R;
import com.pikapika.radar.helpers.AdsHelper;
import com.pikapika.radar.helpers.ConfigReader;
import com.pikapika.radar.helpers.DataManager;
import com.pikapika.radar.helpers.PokemonHelper;
import com.pikapika.radar.helpers.PokemonRequestHelper;
import com.pikapika.radar.helpers.SettingsSaving;
import com.pikapika.radar.helpers.UserMarkerHelper;
import com.pikapika.radar.models.GoogleAuthTokenJson;
import com.pikapika.radar.models.PokemonResult;
import com.pikapika.radar.utils.Debug;
import com.pikapika.radar.utils.Utils;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener,NavigationView.OnNavigationItemSelectedListener,
        GoogleMap.OnMapClickListener{

    private static final int MY_LOCATION_REQUEST_CODE = 1001;
    private static final int LOGIN_ACTIVITY_RESULT = 101;
    private static final int GOOGLE_WEB_VIEW_ACTIVITY_RESULT = 1005;

    private static final int REQUEST_LIMIT_TIME = 3000; //3 seg
    private static final int DELAY_LOGIN_HEARTBEAT = 1000; //1 seg

    private static final int CAMERA_MAP_ZOOM = 16;

    private static final String MOPUD_INTERSTITIAL_UNIT_ID = "2493c0695a364c929b598164f4b9fd68";
    private static final String MOPUD_BANNER_UNIT_ID = "eb21a36f0eb14d59b660660d33585893";

    //map stuff
    public GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    //view stuff
    private DrawerLayout menuDrawerLayout;
    public  ProgressDialog loadingProgressDialog;
    public Button searchButton;
    public TextView timerTextView;
    private AlertDialog alertDialog;

    private Marker currentMarker;
    private Handler markerHandler;

    //helpers
    private UserMarkerHelper userMarkerHelper;
    private PokemonRequestHelper pokemonRequestHelper;
    private ConfigReader configReader;
    public SettingsSaving settingsSaving;

    //Ads
    private MoPubInterstitial mInterstitial;
    private MoPubView moPubView;;
    private AdsHelper mAdsHelper;

    //Firebase
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.setDebugLogActive(BuildConfig.DEBUG);
        setContentView(R.layout.activity_maps);
        searchButton = (Button)findViewById(R.id.main_action_button);
        timerTextView = (TextView) findViewById(R.id.timer_text_view);
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

        checkSearchButtonText();
        MenuItem item = navigationView.getMenu().getItem(0);
        setAudioIcon(item, PokemonHelper.getAudioSetting(this));
        //show google login popup
        GoogleAuthTokenJson googleAuthToken = PokemonHelper.getGoogleTokenJson(MapsActivity.this);
        if (googleAuthToken.getId_token() == null)
            showPopUpLogin();
        //show a popup with warning information
        if(PokemonHelper.isFirstLaunch(this)) {
            showPopUpSplash();
            PokemonHelper.saveFirstLaunch(false,this);
        }

        //ADS
        mInterstitial = new MoPubInterstitial(this, MOPUD_INTERSTITIAL_UNIT_ID);
        mAdsHelper = new AdsHelper();
        mInterstitial.setInterstitialAdListener(mAdsHelper);
        mInterstitial.load();
        moPubView = (MoPubView) findViewById(R.id.adview);
        moPubView.setAdUnitId(MOPUD_BANNER_UNIT_ID); //Ad Unit ID from www.mopub.com
        moPubView.loadAd();

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        markerHandler = new Handler();
        pokemonRequestHelper = new PokemonRequestHelper(this);
        configReader = new ConfigReader(this);
        settingsSaving = new SettingsSaving(this);
    }

    public void showPopUpSplash(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(R.layout.pop_up_splash_view);
        alert.setPositiveButton(getText(R.string.ok),null);
        alert.show();
    }

    public void showPopUpLogin(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(R.layout.pop_up_google_login);
        alertDialog =alert.show();
    }

    public void showPopUpScanAreaSetting(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_scan_area);
        RadioGroup radioGroup = (RadioGroup)dialog.findViewById(R.id.scan_radio_group);
        radioGroup.check(settingsSaving.getScanZoneSetting() == 0 ? R.id.radio_button_scan_1 : R.id.radio_button_scan_2);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int level = (i == R.id.radio_button_scan_1 ? 0 : 1);
                settingsSaving.saveScanZoneSetting(level);
            }
        });
        dialog.show();
    }

    public void showPopUpFAQ(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_splash_view);
        TextView mainTexView = (TextView)dialog.findViewById(R.id.text_view_pop_up);
        TextView emptyTexView = (TextView)dialog.findViewById(R.id.textView);
        emptyTexView.setText("");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mainTexView.setText(Html.fromHtml(getString(R.string.fag_text_html),Html.FROM_HTML_MODE_LEGACY));
        } else {
            mainTexView.setText(Html.fromHtml(getString(R.string.fag_text_html)));
        }
        mainTexView. setMovementMethod(LinkMovementMethod.getInstance());
        dialog.show();
    }

    public void onGoogleButtonClick(View view) {
        if(alertDialog != null)
            alertDialog.dismiss();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Intent loginIntent = new Intent(MapsActivity.this, GoogleWebActivity.class);
            startActivityForResult(loginIntent, GOOGLE_WEB_VIEW_ACTIVITY_RESULT);
        } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_LOCATION_REQUEST_CODE);
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        pokemonRequestHelper.startQuickScanLoop();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mInterstitial.destroy();
        moPubView.destroy();
        super.onDestroy();
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
        userMarkerHelper = new UserMarkerHelper(MapsActivity.this, googleMap);

        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startQuickSearchWithDelay();
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
                startQuickSearchWithDelay();
            } else {
                PokemonHelper.showAlert(this,getString(R.string.warning_title),getString(R.string.permissions_location_body));
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
            PokemonHelper.lastLocation = location;
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
            //create the user marker

        }
    }

    public void onMainActionClick(View view) {
        GoogleAuthTokenJson googleAuthTokenJson = PokemonHelper.getGoogleTokenJson(this);

        //check if the token is still valid
        boolean tokenIsEmpty = googleAuthTokenJson.getId_token()  == null;
        if(!tokenIsEmpty) {
            long expireTime = googleAuthTokenJson.getExpires_in(); //in case the expired time is not valid check if the value is 0
            boolean tokenExpired = expireTime == 0 || googleAuthTokenJson.getInit_time() + expireTime < System.currentTimeMillis()/1000;
            if (tokenExpired) {
                //request new token using the current credentials
                refreshToken();
                return;
            }
        }

        //if there is token, means we can do the heartbeat request, in other case we go to login activity
        if(tokenIsEmpty){
            showPopUpLogin();
        } else {
            //ADS
            int clickCounter = PokemonHelper.addClickCounter(this);
            if(clickCounter % configReader.getDefaultAdsClick() == 0) {
                if (mInterstitial.isReady())
                    mInterstitial.show();
            }
            //Heartbeat
            pokemonRequestHelper.startAutoHeartBeat_v2();
        }
    }

    public void onCancelScanClick(View view) {
        pokemonRequestHelper.stopAutoHeartBeat_v2();
    }

    public LatLng getLocation(){
        LatLng latLng = null;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED){
            //check if the user marker is activated
            if(userMarkerHelper != null && userMarkerHelper.getLocation() != null){
                latLng = userMarkerHelper.getLocation();
            } else {
                //get the last location
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                //makes sure we have a valid location
                if (location == null)
                    location = PokemonHelper.lastLocation;
                else
                    PokemonHelper.lastLocation = location;

                if (location != null)
                    latLng = new LatLng(location.getLatitude(),location.getLongitude());
            }
        }
        return  latLng;
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
                    /*if(withAlert) {
                        setActiveSearchButton(false);
                        timerTextView.setVisibility(View.VISIBLE);
                        countDownNewHeartBeat.start();

                        //message to try login again
                        //if(pokemonList.size() == 0)
                            //PokemonHelper.showAlert(MapsActivity.this,getString(R.string.warning_title),getString(R.string.pokemon_not_found));
                    }*/
                }
            });
        }
    }

    private void checkSearchButtonText(){
        //check if the user is logged to change the button text
        GoogleAuthTokenJson googleAuthTokenJson = PokemonHelper.getGoogleTokenJson(this);
        if(googleAuthTokenJson.getId_token() == null){
            //TODO:set text for navigation menu button
           //searchButton.setCompoundDrawablesRelative(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_menu_add, null),null,null,null);
            searchButton.setText(getString(R.string.login));
        } else {
            //searchButton.setCompoundDrawables(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_menu_add, null),null,null,null);
            searchButton.setText(getString(R.string.scan_action));
        }
    }

    public void refreshToken() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //show a progress dialog
                loadingProgressDialog = PokemonHelper.showLoading(MapsActivity.this, getString(R.string.refreshing_title), "");
                DataManager.getDataManager().autoGoogleLoader(MapsActivity.this, "", tokenRequestCallback);
            }
        });
    }

    /**** Login Callback ***/
    final Callback tokenRequestCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
            if(!Utils.isNetworkAvailable(MapsActivity.this)){
                //internet error message
                PokemonHelper.showAlert(MapsActivity.this,getString(R.string.error_title)+"!",
                        getString(R.string.internet_error_body));
            } else {
                PokemonHelper.showAlert(MapsActivity.this,getString(R.string.server_error_title),
                        getString(R.string.login_error_body));
                pokemonRequestHelper.stopAutoHeartBeat_v2();
            }

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();

            //check first if the request was ok
            if (response.code() == 200){
                runHeartbeatWithDelay();
                response.body().close();
                return;
            }
            //String jsonStr = response.body().string();
            PokemonHelper.showAlert(MapsActivity.this,getString(R.string.request_error_title),
                    getString(R.string.login_error_body)+"(response code: "+response.code()+")");
        }
    };

    private void runHeartbeatWithDelay(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //check button state
                checkSearchButtonText();
                loadingProgressDialog = PokemonHelper.showLoading(MapsActivity.this);
                //we do a delay before heartbeat because we need the new token can be updated at server
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadingProgressDialog.dismiss();
                        //request pokemon
                        pokemonRequestHelper.stopAutoHeartBeat_v2();
                        pokemonRequestHelper.startAutoHeartBeat_v2();
                    }
                },DELAY_LOGIN_HEARTBEAT);
            }
        });
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

            if(PokemonHelper.getAudioSetting(this)) {
                //try to play the pokemon sound

                String audioName = "raw/pokemon_" + pokemonResult.getStrNumber();
                int soundId = getResources().getIdentifier(audioName , null, getPackageName());
                if (soundId > 0) {
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), soundId);
                    mp.start();
                }
            }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GOOGLE_WEB_VIEW_ACTIVITY_RESULT && resultCode == RESULT_OK) {
            final String code = data.getStringExtra(GoogleWebActivity.EXTRA_CODE);
            if(code != null) {
                //show a progress dialog
                loadingProgressDialog = PokemonHelper.showLoading(MapsActivity.this, getString(R.string.login_title), "");
                //we request the login but we wait one second the new token has been updated in server
                DataManager.getDataManager().autoGoogleLoader(this, code, tokenRequestCallback);
            } else {
                PokemonHelper.showAlert(this,getString(R.string.error_title),getString(R.string.permissions_error_body));
            }
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
                boolean audioEnabled = PokemonHelper.getAudioSetting(this);
                PokemonHelper.saveAudioSetting(!audioEnabled,this);
                setAudioIcon(item,!audioEnabled);
                break;
            case R.id.menu_scan_area:
                showPopUpScanAreaSetting();
                break;
            case R.id.menu_faq:
                showPopUpFAQ();
                break;
        }
        return true;
    }

    public void processLogoutNavButton(MenuItem item){
        GoogleAuthTokenJson googleAuthTokenJson = PokemonHelper.getGoogleTokenJson(this);
        if(googleAuthTokenJson.getId_token()  != null){
            //clean user data as well
            PokemonHelper.saveGoogleTokenJson(this,null);
            checkSearchButtonText();
            menuDrawerLayout.closeDrawers();
            item.setTitle(getString(R.string.logout));
            //clear pokemon markers
            mMap.clear();
            //clear cookies for google login
            Utils.cleanCookies();
        } else {
            showPopUpLogin();
        }
    }

    public void requestLocation() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED){
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(location != null)
                PokemonHelper.lastLocation = location;
        }
    }

    public void setAudioIcon(MenuItem item,boolean audioEnabled){
        if(audioEnabled)
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

    final public CountDownTimer countDownNewHeartBeat = new CountDownTimer(REQUEST_LIMIT_TIME, 1000) {

        public void onTick(long millisUntilFinished) {
            String labelTime = getString(R.string.time_remaining);
            String textTimer = labelTime+" "+String.valueOf(millisUntilFinished / 1000)+"s";
            timerTextView.setText(textTimer);
        }

        public void onFinish() {
            timerTextView.setVisibility(View.INVISIBLE);
            setActiveSearchButton(true);
        }
    };

    /*final public CountDownTimer countDownRequestTimer = new CountDownTimer(REQUEST_LIMIT_TIME, 1000) {

        public void onTick(long millisUntilFinished) {
            String textTimer = String.valueOf(millisUntilFinished / 1000);
            if(loadingProgressDialog != null)
                loadingProgressDialog.setTitle(getString(R.string.please_wait)+"    "+textTimer);
        }

        public void onFinish() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (loadingProgressDialog != null)
                        loadingProgressDialog.setTitle(getString(R.string.please_wait)+"  "+getString(R.string.arrive_message));
                }
            });
        }
    };*/

    public void setActiveSearchButton(boolean active){
        if(!active){
            searchButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.round_shape_button_gray, null));
            searchButton.setEnabled(active);
        } else {
            searchButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.round_shape_button, null));
            searchButton.setEnabled(active);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        markerHandler.removeCallbacks(markerRunnable);
    }
}
