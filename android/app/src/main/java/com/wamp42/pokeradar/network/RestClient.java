package com.wamp42.pokeradar.network;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by flavioreyes on 7/19/16.
 */
public class RestClient {
    private final String API_URL = "https://dl.dropboxusercontent.com/u/820149/";
    private OkHttpClient client = new OkHttpClient();

    public void get(String path, Callback callback) {
        final String fullUrlPath = API_URL + path;
        try {
            Request request = new Request.Builder()
                    .url(fullUrlPath)
                    .build();
            client.newCall(request).enqueue(callback);
        } catch (Exception e){
            callback.onFailure(null, new IOException("okHttp exception on request "+fullUrlPath));
        }
    }
}
