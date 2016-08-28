package com.pikapika.lite.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.pikapika.lite.R;

/**
 * Created by flavioreyes on 8/9/16.
 */
public class SettingsSaving {

    final public static String SCAN_ZONE_SETTING        = "scan_zone_setting";
    final public static String AUDIO_SETTING        = "audio_setting";
    final public static String AUTO_SEARCH_SETTING        = "audio_setting";
    final public static String FIRST_LAUNCH        = "firstLaunch";
    final public static String CHANGE_POSITION_INSTRUCTION        = "positionInstructionShown";
    final public static String HAERTBEAT_CLICKS_COUNTER        = "clicks_counter";

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

    public static void saveAudioSetting(boolean active, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(AUDIO_SETTING, active);
        editor.apply();
    }

    public static boolean getAudioSetting(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(AUDIO_SETTING,true);
    }

    public static void saveAutoSearchSetting(boolean active, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(AUTO_SEARCH_SETTING, active);
        editor.apply();
    }

    public static boolean getAutoSearchSetting(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(AUTO_SEARCH_SETTING,true);
    }

    public static void saveFirstLaunch(boolean first, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(FIRST_LAUNCH, first);
        editor.apply();
    }

    public static boolean isFirstLaunch(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(FIRST_LAUNCH,true);
    }


    public static void savePositionInstructionShown(boolean active, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(CHANGE_POSITION_INSTRUCTION, active);
        editor.apply();
    }

    public static boolean getPositionInstructionShown(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(CHANGE_POSITION_INSTRUCTION,false);
    }

    public static int addClickCounter( Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int clicksCounter =  sharedPref.getInt(HAERTBEAT_CLICKS_COUNTER,0) + 1;
        editor.putInt(HAERTBEAT_CLICKS_COUNTER, clicksCounter);
        editor.apply();
        return clicksCounter;
    }

    public static int getCounerClicks(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getInt(HAERTBEAT_CLICKS_COUNTER,0);
    }
}
