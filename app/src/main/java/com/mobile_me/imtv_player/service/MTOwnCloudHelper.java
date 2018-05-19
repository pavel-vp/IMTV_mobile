package com.mobile_me.imtv_player.service;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import com.mobile_me.imtv_player.R;
import com.mobile_me.imtv_player.dao.Dao;
import com.mobile_me.imtv_player.model.*;
import com.mobile_me.imtv_player.service.rest.IMTRestCallbackPlaylist;
import com.mobile_me.imtv_player.service.rest.MTRestHelper;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by pasha on 7/21/16.
 */
public class MTOwnCloudHelper  {
    public static final int TYPEFILE_SIMPLE = 1;
    public static final int TYPEFILE_PLAYLIST = 2;


    Context ctx;
    private IMTCallbackEvent cb;

    Dao dao;
    private int typeFile = TYPEFILE_PLAYLIST;
    private MTRestHelper restHelper;

    public MTOwnCloudHelper(String playListRemotePath, Context ctx, IMTCallbackEvent cb) {
        this(playListRemotePath, ctx, cb, TYPEFILE_PLAYLIST);
    }

    public MTOwnCloudHelper(String playListRemotePath, Context ctx, IMTCallbackEvent cb, int typeFile) {
        this.ctx = ctx;
        this.cb = cb;
        this.typeFile = typeFile;
        dao = Dao.getInstance(ctx);
        restHelper = MTRestHelper.getInstance(this.ctx.getResources().getString(R.string.rest_server_base_url));
    }

    public void loadPlayListFromServer() {
            CustomExceptionHandler.log("loadPlayListFromServer started" );
            restHelper.getPlaylist(dao.getDeviceId(), new IMTRestCallbackPlaylist() {
                @Override
                public void onPlaylistLoaded(MTPlayList playList) {
                    cb.onPlayListLoaded(playList, MTOwnCloudHelper.this);
                }

                @Override
                public void onError(Throwable t) {
                    //Log.e(LOG_TAG, t.getMessage());
                    CustomExceptionHandler.logException("loadPlayListFromServer error", t);
                    cb.onError(0, MTOwnCloudHelper.this, t);
                }
            });
    }

    public void loadGlobalSettings() {
            CustomExceptionHandler.log("loadGlobalSettings FromServer started");
            restHelper.getGlobalSetupRec(new Callback<MTGlobalSetupRec>() {
                @Override
                public void onResponse(Call<MTGlobalSetupRec> call, Response<MTGlobalSetupRec> response) {
                    // загрузили файл
                    // событие обработчик
                    cb.onGlobalSetupLoaded(MTOwnCloudHelper.this, response.body());
                }

                @Override
                public void onFailure(Call<MTGlobalSetupRec> call, Throwable t) {
                    //  Log.e(LOG_TAG, t.getMessage());
                    CustomExceptionHandler.logException("loadGlobalSettings  error", t);
                    cb.onError(0, MTOwnCloudHelper.this, t);
                }

            }, dao.getDeviceId());
    }

    public void registerNewDevice() {
            CustomExceptionHandler.log("registerNewDevice on server started");
            restHelper.postNewDevice(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    cb.onUpdateFileLoaded(MTOwnCloudHelper.this);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    cb.onError(0, MTOwnCloudHelper.this, t);
                }
            }, dao.getDeviceId());
    }

    public void sendStat(List<MTStatRec> list) {
            CustomExceptionHandler.log("uploadStat on server started");
            restHelper.postStat(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    cb.onUploadLog(null);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    cb.onError(0, MTOwnCloudHelper.this, t);
                }
            }, dao.getDeviceId(), list);
    }

    public void sendLog(String zipBase64, final String fileName) {
        CustomExceptionHandler.log("uploadLog on server started");
        restHelper.postLog(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                cb.onUploadLog(fileName);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                cb.onError(0, MTOwnCloudHelper.this, t);
            }
        }, dao.getDeviceId(), zipBase64);
    }


    public void loadVideoFileFromPlayList(final MTPlayListRec fileToLoad) {
            CustomExceptionHandler.log("loadVideoFileFromPlayList fileToLoad=" + fileToLoad.getFilename());
            restHelper.getVideoFile(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    CustomExceptionHandler.log("loadVideoFileFromPlayList file loaded");
                    // загрузили файл из плейлиста
                    String s = null;
                    try {
                        s = response.body().string().replaceAll("\"", "");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte[] d = Base64.decode(s.getBytes(), 0);

                    File file = new File(dao.getVideoPath(), fileToLoad.getFilename());

                    File parentDir = new File(file.getParent());
                    parentDir.mkdir();
                    try {
                        FileOutputStream fos = null;
                        fos = new FileOutputStream(file);
                        fos.write(d);
                        fos.flush();
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    CustomExceptionHandler.log("loadVideoFileFromPlayList file saved to disk");
                    cb.onVideoFileLoaded(fileToLoad, MTOwnCloudHelper.this);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    cb.onError(0, MTOwnCloudHelper.this, t);
                }
            }, fileToLoad.getFilename());

            CustomExceptionHandler.log("loadVideoFileFromPlayList downloadOperation.executed");
    }

    public void loadFileInfoFromServer() {
        CustomExceptionHandler.log("loadFileInfoFromServer FromServer started");
        restHelper.getLastApk(new Callback<MTFileApkInfo>() {
            @Override
            public void onResponse(Call<MTFileApkInfo> call, final Response<MTFileApkInfo> response) {
                // загрузили файл
                // событие обработчик
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        cb.onFileInfoLoaded(response.body());
                    }
                }).start();
            }

            @Override
            public void onFailure(Call<MTFileApkInfo> call, Throwable t) {
                //  Log.e(LOG_TAG, t.getMessage());
                CustomExceptionHandler.logException("loadFileInfoFromServer  error", t);
                cb.onError(0, MTOwnCloudHelper.this, t);
            }
        });
    }


    public void loadUpdateFromServer() {

        CustomExceptionHandler.log("loadUpdateFromServer started");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] d=restHelper.getApkSync();
                    CustomExceptionHandler.log("loadUpdateFromServer success");

                    File file = new File(Dao.getInstance(ctx).getUpdateApkPath(), ctx.getResources().getString(R.string.updateapk_filename));

                    try {
                        FileOutputStream fos = null;
                        fos = new FileOutputStream(file);
                        fos.write(d);
                        fos.flush();
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cb.onUpdateFileLoaded(MTOwnCloudHelper.this);

                } catch (IOException e) {
                    e.printStackTrace();
                    CustomExceptionHandler.logException("on download apk file error", e);
                }
            }
        }).start();

/*        restHelper.getApk(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> resp) {
                // загрузили файл
                String s = null;
                try {
                    s = resp.body().string().replaceAll("\"", "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] d = Base64.decode(s.getBytes(), 0);

                final String path = Environment.getExternalStorageDirectory() + "/imtv";
                File file = new File(path, fileToLoad.getFilename());
                file.mkdir();
                try {
                    FileOutputStream fos = null;
                    fos = new FileOutputStream(file);
                    fos.write(d);
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cb.onUpdateFileLoaded(MTOwnCloudHelper.this);
            }

            @Override
            public void onFailure(Throwable t) {
                cb.onError(0, MTOwnCloudHelper.this, t);

            }
        });
*/
        CustomExceptionHandler.log("loadUpdateFromServer downloadOperation.executed");
    }

}
