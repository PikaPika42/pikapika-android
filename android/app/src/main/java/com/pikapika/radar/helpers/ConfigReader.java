package com.pikapika.radar.helpers;

import android.content.Context;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pikapika.radar.BuildConfig;
import com.pikapika.radar.R;
import com.pikapika.radar.activities.MapsActivity;
import com.pikapika.radar.update.SemVer;
import com.pikapika.radar.utils.PermissionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by flavioreyes on 8/7/16.
 */
public class ConfigReader {

    private MapsActivity m_Activity;
    private JsonObject m_jsonConfig;
    private int defaultClicksForAds = 10;

    public ConfigReader(MapsActivity context){
        this.m_Activity = context;
        readLocalConfig();
    }

    public void requestConfig(){
        DataManager.getDataManager().configuration(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    String jsonStr = response.body().string();
                    if (!jsonStr.isEmpty()) {
                        parseJsonConfig(jsonStr);
                        //check the new apk version
                        checkNewVersion();
                    }
                }
                response.body().close();
            }
        });
    }

    void readLocalConfig(){
        //read json from raw folder
        InputStream inputStream = m_Activity.getResources().openRawResource(R.raw.config);
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
        parseJsonConfig(jsonStr);
    }

    void parseJsonConfig(String jsonStr){
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

    public String getRemoteVersion(){
        String lastVersion = BuildConfig.VERSION_NAME;
        if(m_jsonConfig.has("data")){
            JsonObject jsonObject = m_jsonConfig.getAsJsonObject("data").getAsJsonObject("last_version");
            if(jsonObject != null) {
                lastVersion = jsonObject.get("android").getAsString();
                //lastVersion = jsonObject.toString();
            }
        }
        return lastVersion;
    }

    public String getAPKUri(){
        String url = "";
        if(m_jsonConfig.has("data")){
            JsonObject jsonObject = m_jsonConfig.getAsJsonObject("data").getAsJsonObject("apk_url");
            if(jsonObject != null)
                url = jsonObject.toString();
        }
        return url;
    }

    public void  checkNewVersion(){
        //check if there a valid url to download the apk
        if(getAPKUri().isEmpty())
            return;
        SemVer currentVersion = SemVer.parse(BuildConfig.VERSION_NAME);
        SemVer remoteVersion = SemVer.parse(getRemoteVersion());
        if (currentVersion.compareTo(remoteVersion) < 0) {
            if (PermissionUtils.doWeHaveReadWritePermission(m_Activity)) {
                m_Activity.showAppUpdateDialog();
            } else {

            }
        }
    }
}
