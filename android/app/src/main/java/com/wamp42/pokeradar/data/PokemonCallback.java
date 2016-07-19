package com.wamp42.pokeradar.data;

import com.wamp42.pokeradar.models.PokemonLocation;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by flavioreyes on 7/19/16.
 */
public interface PokemonCallback<T> {
    void onFailure(Call call, IOException e);
    void onResponse(Call call, Response response, T object) throws IOException;
}
