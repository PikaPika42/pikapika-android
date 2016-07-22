package com.wamp42.pikapika.network;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class RestClient {
    private final String API_URL = "";
    private final String API_URL_TEST ="http://10.0.1.14:3000/";

    private OkHttpClient client = new OkHttpClient();

    public static final MediaType MEDIA_TYPE_JSON
            = MediaType.parse("application/json");

    public String getFullURL(String path){
        return API_URL_TEST + path;
    }

    public void get(String path, HashMap<String,String> parametersMap, Callback callback) {
        String params = "";
        if(parametersMap != null){
            params ="?";
            for(String key:parametersMap.keySet()){
                params += key+"="+parametersMap.get(key)+"&";
            }
        }
        final String fullUrlPath = getFullURL(path+params);
        try {
            Request request = new Request.Builder()
                    .url(fullUrlPath)
                    .build();
            client.newCall(request).enqueue(callback);
        } catch (Exception e){
            callback.onFailure(null, new IOException("okHttp exception on request "+fullUrlPath));
        }
    }

    public void postJson(String json, String path, Callback callback){
        final String fullUrlPath = getFullURL(path);
        try {
        Request request = new Request.Builder()
                .url(fullUrlPath)
                .post(RequestBody.create(MEDIA_TYPE_JSON, json))
                .build();
        client.newCall(request).enqueue(callback);
        } catch (Exception e){
            callback.onFailure(null, new IOException("okHttp exception on request "+fullUrlPath));
        }
    }
}
