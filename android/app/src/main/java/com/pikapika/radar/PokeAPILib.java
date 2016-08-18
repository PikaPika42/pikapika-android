package com.pikapika.radar;

import android.os.AsyncTask;
import android.os.Build;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pikapika.radar.activities.MapsActivity;
import com.pikapika.radar.models.PokemonPosition;
import com.pikapika.radar.models.PokemonResult;
import com.pikapika.radar.utils.Debug;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.device.DeviceInfo;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.exceptions.AsyncPokemonGoException;
import com.pokegoapi.exceptions.AsyncRemoteServerException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.main.AsyncServerRequest;
import com.pokegoapi.util.AsyncHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import POGOProtos.Map.Fort.FortDataOuterClass;
import POGOProtos.Map.Fort.FortTypeOuterClass;
import POGOProtos.Map.MapCellOuterClass;
import POGOProtos.Map.Pokemon.MapPokemonOuterClass;
import POGOProtos.Map.Pokemon.WildPokemonOuterClass;
import POGOProtos.Networking.Requests.Messages.GetMapObjectsMessageOuterClass;
import POGOProtos.Networking.Requests.RequestTypeOuterClass;
import POGOProtos.Networking.Responses.GetMapObjectsResponseOuterClass;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by flavioreyes on 8/14/16.
 */
public class PokeAPILib {
    OkHttpClient httpClient;
    PokemonGo mGo;
    boolean initializedGO =false;
    private MapsActivity mMapsActivity;

    public PokeAPILib(MapsActivity mapsActivity){
        mMapsActivity = mapsActivity;
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    public void initGO(String refreshToken){
        new InitAsyncGO().execute(refreshToken);
    }

    public boolean isInitializedGO(){
        return initializedGO;
    }

    public void getCatchablePokemon(double lat, double log, double alt){
        //mapObjectAsyncTask.execute(1.0);
        //pokemonAsyncTask.execute(lat, log, alt);
        //new AsyncCatchablePokemon().execute(lat, log, alt);
        try {
            mGo.setLocation(lat, log, alt);
            List<CatchablePokemon> list = mGo.getMap().getCatchablePokemon();
            List<PokemonResult> resultList = new ArrayList<>();
            for(CatchablePokemon pokemon : list){
                PokemonResult pokemonResult = new PokemonResult();
                pokemonResult.setId(pokemon.getEncounterId()+"");
                pokemonResult.setNumber(pokemon.getPokemonId().getNumber()+"");
                pokemonResult.setName(pokemon.getPokemonId().name());
                pokemonResult.setExpirationTimestampMs(pokemon.getExpirationTimestampMs());
                pokemonResult.setPosition(new PokemonPosition(pokemon.getLatitude(),pokemon.getLongitude()));

                resultList.add(pokemonResult);
                Debug.d( "lib id: " + pokemonResult.getName()+" expiration_time: "+pokemonResult.getExpirationTimestampMs());
            }
            mMapsActivity.addDrawPokemonOnMainThread(resultList, false);
        } catch (LoginFailedException e) {
            e.printStackTrace();
        } catch (RemoteServerException e) {
            e.printStackTrace();
        }
    }

    private class InitAsyncGO extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {

                mGo = new PokemonGo(new GoogleUserCredentialProvider(httpClient, strings[0]), httpClient);
                //mGo.getMap().setDefaultWidth(1);
                initializedGO = true;

            } catch (LoginFailedException e) {
                e.printStackTrace();
            } catch (RemoteServerException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class AsyncCatchablePokemon extends AsyncTask<Double, Integer, List<CatchablePokemon>> {
        @Override
        protected  List<CatchablePokemon> doInBackground(Double... locations) {
            try{
                //String token = PokemonHelper.getGoogleRefreshToken(mMapsActivity);
                //mGo = new PokemonGo(new GoogleUserCredentialProvider(httpClient, token), httpClient);

                //mGo.setDeviceInfo(getDeviceInfo());

                mGo.setLocation(locations[0],locations[1],locations[2]);
                //MapObjects mapObjects =  getMapObjects();

                //Debug.Log("lib pokestops size:"+mapObjects.getPokestops().size());
                //return new ArrayList<>(mapObjects.getCatchablePokemons());
                return mGo.getMap().getCatchablePokemon();
            } catch (LoginFailedException e) {
                e.printStackTrace();
            } catch (RemoteServerException e) {
                e.printStackTrace();
            }catch (AsyncPokemonGoException e){
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(List<CatchablePokemon> list) {
            if(list == null)
                return;
            Debug.Log("lib result size:"+list.size());
            List<PokemonResult> resultList = new ArrayList<>();
            /*for (MapPokemonOuterClass.MapPokemon cp : list) {

                PokemonResult pokemonResult = new PokemonResult();
                pokemonResult.setId(cp.getEncounterId()+"");
                pokemonResult.setNumber(cp.getPokemonId().getNumber()+"");
                pokemonResult.setName(cp.getPokemonId().name());
                pokemonResult.setExpirationTimestampMs(cp.getExpirationTimestampMs());
                pokemonResult.setPosition(new PokemonPosition(cp.getLatitude(),cp.getLongitude()));

                resultList.add(pokemonResult);
                //Debug.d( "lib id: " + cp.getPokemonId().toString()+" expiration_time: "+cp.getExpirationTimestampMs());
            } */


            for(CatchablePokemon pokemon : list){
                PokemonResult pokemonResult = new PokemonResult();
                pokemonResult.setId(pokemon.getEncounterId()+"");
                pokemonResult.setNumber(pokemon.getPokemonId().getNumber()+"");
                pokemonResult.setName(pokemon.getPokemonId().name());
                pokemonResult.setExpirationTimestampMs(pokemon.getExpirationTimestampMs());
                pokemonResult.setPosition(new PokemonPosition(pokemon.getLatitude(),pokemon.getLongitude()));

                resultList.add(pokemonResult);
                Debug.d( "lib id: " + pokemonResult.getName()+" expiration_time: "+pokemonResult.getExpirationTimestampMs());
            }
            mMapsActivity.addDrawPokemonOnMainThread(resultList, false);
        }
    }

    public DeviceInfo getDeviceInfo(){
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setAndroidBoardName(Build.BOARD);
        deviceInfo.setHardwareManufacturer(Build.MANUFACTURER);
        deviceInfo.setDeviceId(UUID.randomUUID().toString());
        deviceInfo.setHardwareModel(Build.MODEL);
        deviceInfo.setAndroidBootloader(Build.BOOTLOADER);
        deviceInfo.setDeviceBrand(Build.BRAND);
        deviceInfo.setDeviceModelBoot("qcom");
        deviceInfo.setDeviceModelIdentifier(Build.DEVICE);
        deviceInfo.setDeviceModel(Build.MODEL);
        deviceInfo.setFirmwareBrand(Build.BRAND);
        deviceInfo.setFirmwareFingerprint(Build.FINGERPRINT);
        deviceInfo.setFirmwareTags(Build.TAGS);
        deviceInfo.setFirmwareType(Build.TYPE);
        return deviceInfo;
    }

    public MapObjects getMapObjects() throws LoginFailedException, RemoteServerException {
        return AsyncHelper.toBlocking(getMapObjectsAsync());
    }

    public Observable<MapObjects> getMapObjectsAsync() {
        return getMapObjectsAsync(mGo.getMap().getCellIds(mGo.getLatitude(), mGo.getLongitude(), 1));
    }

    public Observable<MapObjects> getMapObjectsAsync(List<Long> cellIds) {

        GetMapObjectsMessageOuterClass.GetMapObjectsMessage.Builder builder = GetMapObjectsMessageOuterClass.GetMapObjectsMessage.newBuilder()
                .setLatitude(mGo.getLatitude())
                .setLongitude(mGo.getLongitude());

        for (Long cellId : cellIds) {
            builder.addCellId(cellId);
            builder.addSinceTimestampMs(0);
        }

        final AsyncServerRequest asyncServerRequest = new AsyncServerRequest(
                RequestTypeOuterClass.RequestType.GET_MAP_OBJECTS, builder.build());
        return mGo.getRequestHandler()
                .sendAsyncServerRequests(asyncServerRequest).map(new Func1<ByteString, MapObjects>() {
                    @Override
                    public MapObjects call(ByteString byteString) {
                        GetMapObjectsResponseOuterClass.GetMapObjectsResponse response;
                        try {
                            response = GetMapObjectsResponseOuterClass.GetMapObjectsResponse.parseFrom(byteString);
                        } catch (InvalidProtocolBufferException e) {
                            throw new AsyncRemoteServerException(e);
                        }

                        MapObjects result = new MapObjects(mGo);
                        for (MapCellOuterClass.MapCell mapCell : response.getMapCellsList()) {
                            result.addNearbyPokemons(mapCell.getNearbyPokemonsList());
                            result.addCatchablePokemons(mapCell.getCatchablePokemonsList());
                            result.addWildPokemons(mapCell.getWildPokemonsList());
                            result.addDecimatedSpawnPoints(mapCell.getDecimatedSpawnPointsList());
                            result.addSpawnPoints(mapCell.getSpawnPointsList());

                            java.util.Map<FortTypeOuterClass.FortType, List<FortDataOuterClass.FortData>> groupedForts = Stream.of(mapCell.getFortsList())
                                    .collect(Collectors.groupingBy(new Function<FortDataOuterClass.FortData, FortTypeOuterClass.FortType>() {
                                        @Override
                                        public FortTypeOuterClass.FortType apply(FortDataOuterClass.FortData fortData) {
                                            return fortData.getType();
                                        }
                                    }));
                            result.addGyms(groupedForts.get(FortTypeOuterClass.FortType.GYM));
                            result.addPokestops(groupedForts.get(FortTypeOuterClass.FortType.CHECKPOINT));
                        }

                        return result;
                    }
                });
    }

    final AsyncTask<Double,Integer, Observable<MapObjects>> mapObjectAsyncTask = new AsyncTask<Double, Integer, Observable<MapObjects>>() {
        @Override
        protected  Observable<MapObjects> doInBackground(Double... locations) {
            return getMapObject();
        }

        protected void onPostExecute(Observable<MapObjects> list) {
            try {
                MapObjects mapObjects = AsyncHelper.toBlocking(list);
                for (MapPokemonOuterClass.MapPokemon mapPokemon : mapObjects.getCatchablePokemons()) {
                    Debug.Log("catchable : "+mapPokemon.getPokemonId()+" timeleft:"+mapPokemon.getExpirationTimestampMs());
                }

                for (WildPokemonOuterClass.WildPokemon wildPokemon : mapObjects.getWildPokemons()) {
                    Debug.Log("wildPokemon : "+wildPokemon.getEncounterId()+" timeleft:"+wildPokemon.getTimeTillHiddenMs());
                }

                for (Pokestop pokestop : mapObjects.getPokestops()) {
                    Debug.Log("pokestop : "+pokestop.getId()+" distance:"+pokestop.getDistance());
                }
            } catch (LoginFailedException e) {
                e.printStackTrace();
            } catch (RemoteServerException e) {
                e.printStackTrace();
            }
            /*
            Observable<List<CatchablePokemon>> pokemons = list.map(new Func1<MapObjects, List<CatchablePokemon>>() {
                @Override
                public List<CatchablePokemon> call(MapObjects mapObjects) {
                    List<CatchablePokemon> catchablePokemons = new ArrayList<>();
                    for (MapPokemonOuterClass.MapPokemon mapPokemon : mapObjects.getCatchablePokemons()) {
                        catchablePokemons.add(new CatchablePokemon(mGo, mapPokemon));
                        Debug.Log("catchable : "+mapPokemon.getPokemonId()+" timeleft:"+mapPokemon.getExpirationTimestampMs());
                    }

                    for (WildPokemonOuterClass.WildPokemon wildPokemon : mapObjects.getWildPokemons()) {
                        catchablePokemons.add(new CatchablePokemon(mGo, wildPokemon));
                        Debug.Log("wildPokemon : "+wildPokemon.getEncounterId()+" timeleft:"+wildPokemon.getTimeTillHiddenMs());
                    }

                    for (Pokestop pokestop : mapObjects.getPokestops()) {
                        if (pokestop.inRangeForLuredPokemon() && pokestop.getFortData().hasLureInfo()) {
                            catchablePokemons.add(new CatchablePokemon(mGo, pokestop.getFortData()));
                        }
                        Debug.Log("pokestop : "+pokestop.getId()+" distance:"+pokestop.getDistance());
                    }
                    return catchablePokemons;
                }
            });
            List<CatchablePokemon> listPokemons;
            try {
                listPokemons = AsyncHelper.toBlocking(pokemons);
                Debug.Log("pokemos size:"+listPokemons.size());
            } catch (LoginFailedException e) {
                e.printStackTrace();
            } catch (RemoteServerException e) {
                e.printStackTrace();
            }
            */
        }
    };

    public Observable<MapObjects> getMapObject(){
        GetMapObjectsMessageOuterClass.GetMapObjectsMessage.Builder builder = GetMapObjectsMessageOuterClass.GetMapObjectsMessage.newBuilder()
                .setLatitude(mGo.getLatitude())
                .setLongitude(mGo.getLongitude());
        List<Long> cellIds = mGo.getMap().getCellIds(mGo.getLatitude(),mGo.getLongitude(),1);
        for (Long cellId : cellIds) {
            builder.addCellId(cellId);
            builder.addSinceTimestampMs(0);
        }

        final AsyncServerRequest asyncServerRequest = new AsyncServerRequest(
                RequestTypeOuterClass.RequestType.GET_MAP_OBJECTS, builder.build());
        return mGo.getRequestHandler()
                .sendAsyncServerRequests(asyncServerRequest).map(new Func1<ByteString, MapObjects>() {
            @Override
            public MapObjects call(ByteString byteString) {
                GetMapObjectsResponseOuterClass.GetMapObjectsResponse response;
                try {
                    response = GetMapObjectsResponseOuterClass.GetMapObjectsResponse.parseFrom(byteString);
                } catch (InvalidProtocolBufferException e) {
                    throw new AsyncRemoteServerException(e);
                }

                MapObjects result = new MapObjects(mGo);
                for (MapCellOuterClass.MapCell mapCell : response.getMapCellsList()) {
                    result.addNearbyPokemons(mapCell.getNearbyPokemonsList());
                    result.addCatchablePokemons(mapCell.getCatchablePokemonsList());
                    result.addWildPokemons(mapCell.getWildPokemonsList());
                    result.addDecimatedSpawnPoints(mapCell.getDecimatedSpawnPointsList());
                    result.addSpawnPoints(mapCell.getSpawnPointsList());

                    java.util.Map<FortTypeOuterClass.FortType, List<FortDataOuterClass.FortData>> groupedForts = Stream.of(mapCell.getFortsList())
                            .collect(Collectors.groupingBy(new Function<FortDataOuterClass.FortData, FortTypeOuterClass.FortType>() {
                                @Override
                                public FortTypeOuterClass.FortType apply(FortDataOuterClass.FortData fortData) {
                                    return fortData.getType();
                                }
                            }));
                    result.addGyms(groupedForts.get(FortTypeOuterClass.FortType.GYM));
                    result.addPokestops(groupedForts.get(FortTypeOuterClass.FortType.CHECKPOINT));
                }
                return result;
            }
        });
    }
}
