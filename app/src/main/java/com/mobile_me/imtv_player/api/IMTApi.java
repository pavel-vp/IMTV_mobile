package com.mobile_me.imtv_player.api;

import com.mobile_me.imtv_player.model.*;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

import java.util.List;

/**
 * Created by pasha on 05.03.18.
 */
public interface IMTApi {
    // Загрузить плейлист по девайсу
    @GET("screen/getPlayList.php")
    Call<MTPlayListRec[]> getPlayList(@Query("code") String code);

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
    // http://crm.darilkin-shop.ru/api/video/setStats.php?code=001f5459d641&date=1523863157394&data=[{"dt":"1523863157394","id":935,"lat":0.0,"lon":0.0},{"dt":"1523863157394","id":935,"lat":0.0,"lon":0.0}]
    @POST("video/setStats.php")
    Call<ResponseBody> uploadStat(@Query("code") String iddevice, @Query("date") String date, @Body List<MTStatRec> data);

    // Выгрузить на сервер лог по девайсу
    // http://crm.darilkin-shop.ru/api/log/setLog.php?code=001f5459d641&date=1523863157394&data=(base64 zip file)
    @POST("log/setLog.php")
    Call<ResponseBody> uploadLog(@Query("code") String iddevice, @Query("date") String date, @Body String zipBase64);

    // загрузить информацию о файле апк
    @GET("apk/getLastAPK.php")
    Call<MTFileApkInfo> getLastApk();

    // скачать файлапк
    @GET("apk/getAPK.php")
    Call<ResponseBody> getApk();

}
