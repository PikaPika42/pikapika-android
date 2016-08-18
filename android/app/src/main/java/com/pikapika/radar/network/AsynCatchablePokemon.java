package com.pikapika.radar.network;

import android.os.AsyncTask;

import com.pikapika.radar.models.PokemonPosition;
import com.pikapika.radar.models.PokemonResult;
import com.pikapika.radar.utils.Debug;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by flavioreyes on 8/14/16.
 */
public class AsynCatchablePokemon extends AsyncTask<Double, Void, List<PokemonResult>> {
    PokemonGo go;

    public AsynCatchablePokemon(PokemonGo go) {
        this.go = go;
    }

    @Override
    protected List<PokemonResult> doInBackground(Double... locations) {
        List<PokemonResult> resultList = new ArrayList<>();
        try{
            List<CatchablePokemon> list = go.getMap().getCatchablePokemon();
            for (CatchablePokemon cp : list) {
                PokemonResult pokemonResult = new PokemonResult();
                pokemonResult.setId(cp.getPokemonId().getNumber()+"");
                pokemonResult.setName(cp.getPokemonId().name());
                pokemonResult.setTimeleft((int)cp.getExpirationTimestampMs());
                pokemonResult.setPosition(new PokemonPosition(cp.getLatitude(),cp.getLongitude()));
                resultList.add(pokemonResult);
                Debug.d( "id: " + cp.getPokemonId()+" expiration_time: "+cp.getExpirationTimestampMs());
            }
        } catch (LoginFailedException e) {
            e.printStackTrace();
        } catch (RemoteServerException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    /*protected void onPostExecute(List<CatchablePokemon> list) {
        Debug.Log("onPostExecute result size:"+list.size());
        for (CatchablePokemon cp : list) {
            PokemonResult pokemonResult = new PokemonResult();
            pokemonResult.setId(cp.getPokemonId().getNumber()+"");
            pokemonResult.setName(cp.getPokemonId().name());
            pokemonResult.setTimeleft((int)cp.getExpirationTimestampMs());
            pokemonResult.setPosition(new PokemonPosition(cp.getLatitude(),cp.getLongitude()));

            Debug.d( "id: " + cp.getPokemonId()+" expiration_time: "+cp.getExpirationTimestampMs());
        }
    }*/
}
