package com.wamp42.pokeradar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.ContactsContract;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.wamp42.pokeradar.data.DataManager;
import com.wamp42.pokeradar.data.PokemonCallback;
import com.wamp42.pokeradar.data.PokemonManager;
import com.wamp42.pokeradar.models.PokemonLocation;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int MY_LOCATION_REQUEST_CODE = 1001;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

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
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

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
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)
                    mMap.setMyLocationEnabled(true);
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

        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (currentLocation != null) {
            LatLng currentPos = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 17));
        }
    }

    public void onMainActionClick(View view) {
        //PokemonManager.drawPokemonLocations(this,mMap, DataManager.getDummyPokemonsLocation());
        DataManager dataManager = DataManager.getDataManager();
        dataManager.getPokemons(0,0,pokemonCallback);
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
                }
            });
        }
    }

    final PokemonCallback<List<PokemonLocation>> pokemonCallback = new PokemonCallback<List<PokemonLocation>>() {
        @Override
        public void onFailure(Call call, IOException e) {
            //TODO: show error message
        }

        @Override
        public void onResponse(Call call, Response response, List<PokemonLocation> pokemonList) throws IOException {
            drawPokemonListOnMainThread(pokemonList);
        }
    };
}
