package com.wamp42.pokeradar.data;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by flavioreyes on 7/19/16.
 */
public interface PokemonCallback<T> {
    void onFailure(Call call, IOException e);
    void onResponse(Call call, Response response, T object) throws IOException;
}
