package com.pikapika.radar.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.pikapika.radar.R;

/**
 * Created by flavioreyes on 8/9/16.
 */
public class SettingsSaving {

    final public static String SCAN_ZONE_SETTING        = "scan_zone_setting";

    Context context;
    SharedPreferences sharedPref;

    public SettingsSaving(Context context){
        this.context = context;
        sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    public void saveScanZoneSetting(int scanLevel){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(SCAN_ZONE_SETTING, scanLevel);
        editor.apply();
    }

    public int getScanZoneSetting(){
        return sharedPref.getInt(SCAN_ZONE_SETTING,0);
    }
}
