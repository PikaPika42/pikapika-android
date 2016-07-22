package com.wamp42.pokeradar.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wamp42.pokeradar.R;
import com.wamp42.pokeradar.data.DataManager;
import com.wamp42.pokeradar.data.PokemonManager;
import com.wamp42.pokeradar.models.PokemonResult;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by flavioreyes on 7/21/16.
 */
public class LoginActivity extends AppCompatActivity {

    EditText userEditText;
    EditText passEditText;
    private ProgressDialog loadingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userEditText = (EditText)findViewById(R.id.edittext_login_email);
        passEditText = (EditText)findViewById(R.id.edittext_login_password);
    }
    //Home back arrow support
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onLoginClick(View view) {
        String user = userEditText.getText().toString();
        String pass = passEditText.getText().toString();

        PokemonManager.saveUserData(LoginActivity.this,user, pass);

        Location location = null;
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED)
                && MapsActivity.getMapsActivity() != null){
            location = LocationServices.FusedLocationApi.getLastLocation(MapsActivity.getMapsActivity().getGoolgeAPIclient());
        }
        loadingProgressDialog = PokemonManager.showLoading(this);
        DataManager.getDataManager().login(user, pass, location,loginCallback);
    }



    final Callback loginCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
            PokemonManager.saveUserData(LoginActivity.this,"","");
            finish();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
            if (response.code() == 200){
                String jsonStr = response.body().string();
                if(!jsonStr.isEmpty()) {
                    Type listType = new TypeToken<List<PokemonResult>>() {
                    }.getType();
                    try {
                        List<PokemonResult> pokemonResultList = new Gson().fromJson(jsonStr, listType);
                        if(pokemonResultList != null){
                            MapsActivity.getMapsActivity().setPokemonList(pokemonResultList);
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            } else {
                PokemonManager.saveUserData(LoginActivity.this,"","");
            }

            setResult(RESULT_OK, new Intent());
            finish();
        }
    };
}
