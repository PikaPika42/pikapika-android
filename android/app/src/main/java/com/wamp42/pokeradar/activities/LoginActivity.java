package com.wamp42.pokeradar.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.wamp42.pokeradar.R;
import com.wamp42.pokeradar.data.DataManager;
import com.wamp42.pokeradar.data.PokemonHelper;
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

    private EditText userEditText;
    private EditText passEditText;
    private ProgressDialog loadingProgressDialog;
    private RadioButton googleRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userEditText = (EditText)findViewById(R.id.edittext_login_email);
        passEditText = (EditText)findViewById(R.id.edittext_login_password);
        googleRadioButton = (RadioButton)findViewById(R.id.google_radio_button);
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
        //save the user credentials for future requests
        String user = userEditText.getText().toString();
        String pass = passEditText.getText().toString();
        String provider = googleRadioButton.isChecked() ? PokemonHelper.GOOGLE_PROVIDER : PokemonHelper.PTC_PROVIDER;
        PokemonHelper.saveUserData(LoginActivity.this,user, pass,provider);
        //try to get the current location
        Location location = null;
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED)
                && MapsActivity.getMapsActivity() != null){
            location = LocationServices.FusedLocationApi.getLastLocation(MapsActivity.getMapsActivity().getGoogleAPIClient());
        }
        //show a progress dialog
        loadingProgressDialog = PokemonHelper.showLoading(this);
        //request the pokemon data / login
        DataManager.getDataManager().login(user, pass, location,provider,loginCallback);
    }



    final Callback loginCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            if(loadingProgressDialog != null)
                loadingProgressDialog.dismiss();
            PokemonHelper.saveUserData(LoginActivity.this,"","","");
            PokemonHelper.showAlert(LoginActivity.this,getString(R.string.request_error_title)+"!!",
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
                    Type listType = new TypeToken<List<PokemonResult>>() {
                    }.getType();
                    try {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(jsonStr).getAsJsonObject();
                        if(jsonObject.has("data")) {
                            List<PokemonResult> pokemonResultList = new Gson().fromJson(jsonObject.get("data").toString(), listType);
                            if (pokemonResultList != null) {
                                MapsActivity.setPokemonList(pokemonResultList);
                            }
                            //finished activity with OK response
                            setResult(RESULT_OK, new Intent());
                            finish();
                            return;
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            } else {
                //clean the credentials saved
                PokemonHelper.saveUserData(LoginActivity.this,"","","");
            }
            PokemonHelper.showAlert(LoginActivity.this,getString(R.string.request_error_title),
                    getString(R.string.request_error_body));
        }
    };
}
