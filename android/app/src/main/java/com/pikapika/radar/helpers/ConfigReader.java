package com.pikapika.radar.helpers;

import android.content.Context;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pikapika.radar.BuildConfig;
import com.pikapika.radar.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by flavioreyes on 8/7/16.
 */
public class ConfigReader {

    private Context m_context;
    private JsonObject m_jsonConfig;
    private int defaultClicksForAds = 10;

    public ConfigReader(Context context){
        this.m_context = context;
        readLocalConfig();
    }


    void readLocalConfig(){
        //read json from raw folder
        InputStream inputStream = m_context.getResources().openRawResource(R.raw.config);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //get the json as string
        String jsonStr = byteArrayOutputStream.toString();
        JsonParser parser = new JsonParser();
        m_jsonConfig = parser.parse(jsonStr).getAsJsonObject();
        //read data
        if(m_jsonConfig.has("data")){
            JsonObject jsonObject = m_jsonConfig.getAsJsonObject("data");
            JsonObject jsonAds = jsonObject.getAsJsonObject("ads");
            defaultClicksForAds = jsonAds.getAsJsonPrimitive("reload_number").getAsInt();
        }
    }

    public int getDefaultAdsClick(){
        return defaultClicksForAds;
    }

    public String getLastVersion(){
        String lastVersion = BuildConfig.VERSION_NAME;
        if(m_jsonConfig.has("data")){
            JsonObject jsonObject = m_jsonConfig.getAsJsonObject("data").getAsJsonObject("last_version").getAsJsonObject("android");
            lastVersion = jsonObject.toString();
        }
        return lastVersion;
    }
}
