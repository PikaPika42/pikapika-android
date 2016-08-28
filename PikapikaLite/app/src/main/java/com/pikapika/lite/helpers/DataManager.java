package com.pikapika.lite.helpers;

import com.pikapika.lite.network.RestClient;
import java.util.HashMap;
import okhttp3.Callback;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class DataManager {

    private static DataManager dataManagerInstance;
    private RestClient restClient = new RestClient();

    public static DataManager getDataManager(){
        if(dataManagerInstance == null)
            dataManagerInstance = new DataManager();
        return dataManagerInstance;
    }


    public void configuration(Callback callback){
        restClient.get("configuration", new HashMap<String, String>(), callback);
    }

    public void quickHeartbeat(String lat, String lng, int radius, Callback callback){
        HashMap<String, String> params = new HashMap<>();
        params.put("radius", radius+"");
        restClient.get("pokemons/"+lat+"/"+lng+"", params, callback);
    }
}
