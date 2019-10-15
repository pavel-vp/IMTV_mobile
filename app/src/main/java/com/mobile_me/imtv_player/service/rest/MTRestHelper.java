package com.mobile_me.imtv_player.service.rest;

import android.util.Log;
import com.mobile_me.imtv_player.api.IMTApi;
import com.mobile_me.imtv_player.model.*;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.apache.commons.codec.binary.Base64;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by pasha on 06.03.18.
 */
public class MTRestHelper {

    private static MTRestHelper restHelper = null;
    private static IMTApi service = null;

    public static MTRestHelper getInstance(String baseUrl) {
        if (restHelper == null) {
            restHelper = new MTRestHelper(baseUrl);
        }
        return restHelper;
    }

    public MTRestHelper(String baseUrl) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
//                .addConverterFactory(SimpleXmlConverterFactory.create())
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                //.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        service = retrofit.create(IMTApi.class);
    }


    private void getPlayList(Callback<MTPlayListRec[]> cb, String deviceid) {
        Call<MTPlayListRec[]> callRes = service.getPlayList(deviceid);
        callRes.enqueue(cb);
    }

    public MTPlayListRec[] getPlayListSync(String deviceid) throws IOException {
        Call<MTPlayListRec[]> callRes = service.getPlayList(deviceid);
        return callRes.execute().body();
    }

    private void getPlayListFixed(Callback<MTPlayListRec[]> cb, String deviceid) {
        Call<MTPlayListRec[]> callRes = service.getPlayListFixed(deviceid);
        callRes.enqueue(cb);
    }

    public MTPlayListRec[] getPlayListFixedSync(String deviceid) throws IOException {
        Call<MTPlayListRec[]> callRes = service.getPlayListFixed(deviceid);
        return callRes.execute().body();
    }

    public MTGlobalSetupRec getGlobalSetupRecSync(String deviceid) throws IOException {
        Call<MTGlobalSetupRec> callRes = service.getGlobalSetup(deviceid);
        return callRes.execute().body();
    }

    public void getGlobalSetupRec(Callback<MTGlobalSetupRec> cb, String deviceid)  {
        Call<MTGlobalSetupRec> callRes = service.getGlobalSetup(deviceid);
        callRes.enqueue(cb);
    }

    public void postNewDeviceSync(String deviceid) throws IOException {
        Call<ResponseBody> callRes = service.registerNewDevice(deviceid);
        Response<ResponseBody> resp = callRes.execute();
    }

    public void postNewDevice(Callback<ResponseBody> cb, String deviceid)  {
        Call<ResponseBody> callRes = service.registerNewDevice(deviceid);
        callRes.enqueue(cb);
    }

    public byte[] getVideoFileSync(String fileName) throws IOException {
        Call<ResponseBody> callRes = service.getVideoFile(fileName);
        Response<ResponseBody> resp = callRes.execute();
        String s = resp.body().string().replaceAll("\"", "");
        byte[] d = Base64.decodeBase64(s.getBytes());
        return d;
    }

    public void getVideoFile(Callback<ResponseBody> cb, String fileName)  {
        Call<ResponseBody> callRes = service.getVideoFile(fileName);
        callRes.enqueue(cb);
    }

    public void getPlaylist(final String deviceid, final IMTRestCallbackPlaylist cb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getPlayList(new Callback<MTPlayListRec[]>() {
                    @Override
                    public void onResponse(Call<MTPlayListRec[]> call, Response<MTPlayListRec[]> response) {
                        Log.v("MT", "sent ok response=" + response.body());
                        if (response.body() != null) {
                            MTPlayList playlist = new MTPlayList();
                            playlist.getPlaylist().clear();
                            List<MTPlayListRec> list = Arrays.asList(response.body());
                            playlist.getPlaylist().addAll(list);
                            cb.onPlaylistLoaded(playlist);
                        } else {
                            cb.onError(new Exception("error in  restaip - null playlist"));
                        }
                    }

                    @Override
                    public void onFailure(Call<MTPlayListRec[]> call, Throwable t) {
                        cb.onError(t);
                    }
                }, deviceid);
            }
        }).start();

    }

    public void getPlaylistFixed(final String deviceid, final IMTRestCallbackPlaylist cb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getPlayListFixed(new Callback<MTPlayListRec[]>() {
                    @Override
                    public void onResponse(Call<MTPlayListRec[]> call, Response<MTPlayListRec[]> response) {
                        Log.v("MT", "sent ok response=" + response.body());
                        if (response.body() != null) {
                            MTPlayList playlist = new MTPlayList();
                            playlist.getPlaylist().clear();
                            List<MTPlayListRec> list = Arrays.asList(response.body());
                            playlist.getPlaylist().addAll(list);
                            cb.onPlaylistLoaded(playlist);
                        } else {
                            cb.onError(new Exception("error in  restaip - null playlist"));
                        }
                    }

                    @Override
                    public void onFailure(Call<MTPlayListRec[]> call, Throwable t) {
                        cb.onError(t);
                    }
                }, deviceid);
            }
        }).start();

    }

    public ResponseBody postStatSync(String deviceid, List<MTStatRec> data) throws IOException {
        Calendar calendar = Calendar.getInstance();
        Call<ResponseBody> callRes = service.uploadStat(deviceid, String.valueOf(calendar.getTimeInMillis()), data);
        Response<ResponseBody> resp = callRes.execute();
        return resp.body();
    }

    public void postStat(Callback<ResponseBody> cb, String deviceid, List<MTStatRec> data)  {
        Calendar calendar = Calendar.getInstance();
        Call<ResponseBody> callRes = service.uploadStat(deviceid, String.valueOf(calendar.getTimeInMillis()), data);
        callRes.enqueue(cb);
    }

    public ResponseBody postLogSync(String deviceid, String zipBase64) throws IOException {
        Calendar calendar = Calendar.getInstance();
        MTLogUploadRec r = new MTLogUploadRec();
        r.setData(zipBase64);
        Call<ResponseBody> callRes = service.uploadLog(deviceid, String.valueOf(calendar.getTimeInMillis()), r);
        Response<ResponseBody> resp = callRes.execute();
        return resp.body();
    }

    public void postLog(Callback<ResponseBody> cb, String deviceid, String zipBase64)  {
        Calendar calendar = Calendar.getInstance();
        MTLogUploadRec r = new MTLogUploadRec();
        r.setData(zipBase64);
        Call<ResponseBody> callRes = service.uploadLog(deviceid, String.valueOf(calendar.getTimeInMillis()), r);
        callRes.enqueue(cb);
    }

    // загрузить информацию о файле апк
    public MTFileApkInfo getLastApkSync() throws IOException {
        Call<MTFileApkInfo> callRes = service.getLastApk();
        return callRes.execute().body();
    }

    public void getLastApk(Callback<MTFileApkInfo> cb)  {
        Call<MTFileApkInfo> callRes = service.getLastApk();
        callRes.enqueue(cb);
    }

    // скачать файлапк
    public byte[] getApkSync() throws IOException {
        Call<ResponseBody> callRes = service.getApk();
        Response<ResponseBody> resp = callRes.execute();
        String s = resp.body().string().replaceAll("\"", "");
        byte[] d = Base64.decodeBase64(s.getBytes());
        return d;
    }

    public void getApk(Callback<ResponseBody> cb)  {
        Call<ResponseBody> callRes = service.getApk();
        callRes.enqueue(cb);
    }


}
