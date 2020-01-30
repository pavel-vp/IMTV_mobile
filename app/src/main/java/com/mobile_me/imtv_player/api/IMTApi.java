package com.mobile_me.imtv_player.api;

import com.mobile_me.imtv_player.model.*;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

/**
 * Created by pasha on 05.03.18.
 */
public interface IMTApi {

    // Загрузить с сервера глобальные настройки
    @GET("screen/getScreenSettings.php")
    Call<MTGlobalSetupRec> getGlobalSetup(@Query("code") String code);

    // регистрация нового девайса
    @POST("screen/addNewScreen.php")
    Call<ResponseBody> registerNewDevice(@Query("code") String iddevice);

    // Загрузить с севрера видео по имени файла http://crm.darilkin-shop.ru/api/video/getVideoFile.php?fn=/6/6604_7967.mp4
    @GET("video/getVideoFile.php")
    Call<ResponseBody> getVideoFile(@Query("fn") String filepath);

    // выгрузить на севрер статистику по девайсу
    //http://crm.darilkin-shop.ru/api/stats/setStats.php?code=001f5459d641&date=1523863157394
    @POST("stats/setStats.php")
    Call<ResponseBody> uploadStat(@Query("code") String iddevice, @Query("date") String date, @Body List<MTStatRec> data);

    // Выгрузить на сервер лог по девайсу
    // http://crm.darilkin-shop.ru/api/log/setLog.php?code=001f5459d641&date=1523863157394
    @POST("log/setLog.php")
    Call<ResponseBody> uploadLog(@Query("code") String iddevice, @Query("date") String date, @Body MTLogUploadRec zipBase64);

    // загрузить информацию о файле апк
    @GET("apk/getLastAPK.php")
    Call<MTFileApkInfo> getLastApk();

    // скачать файлапк
    @GET("apk/getAPK.php")
    Call<ResponseBody> getApk();

    // Новое апи - готовый плейлист для проигрывания
    @GET("playlist/getPlaylist.php")
    Call<MTPlayListRec[]> getPlayListFixed(@Query("code") String code);

}
