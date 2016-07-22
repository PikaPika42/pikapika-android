package com.wamp42.pokeradar.data;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.maps.GoogleMap;
import com.wamp42.pokeradar.R;
import com.wamp42.pokeradar.models.PokemonResult;

import java.util.HashMap;
import java.util.List;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class PokemonManager {

    static public HashMap<String,String> markersMap = new HashMap<>();

    static public void drawPokemonResult(Context context, GoogleMap map, List<PokemonResult> locationList){
        for(PokemonResult location:locationList){
            //draw each pokemon mark
            location.drawMark(map, context);
        }
    }

    public static ProgressDialog showLoading(Context context) {
        return ProgressDialog.show(context, context.getString(R.string.please_wait), context.getString(R.string.loading_data), true);
    }

    public static void showAlert(final Activity activity, final String title, final String body) {
        activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(body)
                            .setTitle(title);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
    }

    public static void saveUserData(Context context,String user, String pass){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("user", user);
        editor.putString("pass", pass);
        editor.apply();
    }
}
