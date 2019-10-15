package com.mobile_me.imtv_player.service;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.mobile_me.imtv_player.BuildConfig;
import com.mobile_me.imtv_player.R;
import com.mobile_me.imtv_player.dao.Dao;
import com.mobile_me.imtv_player.model.MTFileApkInfo;
import com.mobile_me.imtv_player.model.MTGlobalSetupRec;
import com.mobile_me.imtv_player.model.MTPlayList;
import com.mobile_me.imtv_player.model.MTPlayListRec;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;

import java.io.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by pasha on 7/26/16.
 */
public class Updater implements IMTCallbackEvent {

    private Context ctx;
    private MTOwnCloudHelper helper;
    public Updater(Context ctx) {
        this.ctx = ctx;
        helper = new MTOwnCloudHelper(null, ctx, this);
        CustomExceptionHandler.log("Updater created");
    }

    public void startGetInfoUpdate() {
        CustomExceptionHandler.log("Updater start get info");
        helper.loadFileInfoFromServer();
    }

    private void startDownLoadUpdate() {
        CustomExceptionHandler.log("Updater start download");
        helper.loadUpdateFromServer();
    }

    public static void checkVersionAndInstall(File newFile, Context ctx) throws Exception {
        final PackageManager pm = ctx.getPackageManager();
        PackageInfo newInfo = pm.getPackageArchiveInfo(newFile.getAbsolutePath(), PackageManager.GET_META_DATA);
        CustomExceptionHandler.log("Updater newFile version= " + newInfo.versionCode + ", current version=" + BuildConfig.VERSION_CODE);
        if (newInfo.versionCode > BuildConfig.VERSION_CODE) {
            CustomExceptionHandler.log("Updater start to update app");
//                installApk2(newFile.getAbsolutePath());
            Updater.installAPKonRooted(newFile.getAbsolutePath(), ctx);
        }
    }

    static class ReadStream implements Runnable {
        String name;
        InputStream is;
        Thread thread;
        public ReadStream(String name, InputStream is) {
            this.name = name;
            this.is = is;
        }
        public void start () {
            thread = new Thread (this);
            thread.start ();
        }
        public void run () {
            try {
                InputStreamReader isr = new InputStreamReader (is);
                BufferedReader br = new BufferedReader (isr);
                while (true) {
                    String s = br.readLine ();
                    if (s == null) break;
                    CustomExceptionHandler.log( "Updater [" + name + "] " + s);
                }
                is.close ();
            } catch (Exception ex) {
                CustomExceptionHandler.log( "Updater Problem reading stream " + name + "... :" + ex);
                ex.printStackTrace ();
            }
        }
    }


    public static void installAPKonRooted(String filename, Context ctx) throws Exception {
        File file = new File(filename);
        if (file.exists()) {
            final String libs = "LD_LIBRARY_PATH=/vendor/lib:/system/lib";
            String command;

            command = "pm install -r " + filename;
            Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", command });
            int res = 0;
            res=proc.waitFor();
            CustomExceptionHandler.log("Updater res procWait2="+res);
            if (res == 0) {
                System.exit(0);
            }
        }
    }

    @Override
    public void onPlayListLoaded(MTPlayList playList, MTOwnCloudHelper ownCloudHelper) {

    }

    @Override
    public void onPlayListFixedLoaded(MTPlayList playList, MTOwnCloudHelper ownCloudHelper) {

    }

    @Override
    public void onVideoFileLoaded(MTPlayListRec file, MTOwnCloudHelper ownCloudHelper) {

    }

    @Override
    public void onUpdateFileLoaded(MTOwnCloudHelper ownCloudHelper) {
        // вызвать установку
        try {
            CustomExceptionHandler.log("Updater success downloading");
            File newFile = new File(Dao.getInstance(ctx).getUpdateApkPath(), ctx.getString(R.string.updateapk_filename));
            CustomExceptionHandler.log("Updater newFile= " + newFile + " check is need to install");
            // нужно проверить надо ли установить, т.к.  иначе уйдем в бесконечный цикл
            Updater.checkVersionAndInstall(newFile, ctx);
            CustomExceptionHandler.log("Updater finished");
        } catch (Exception e) {
            CustomExceptionHandler.logException("Error updating", e);
            e.printStackTrace();
        }
        reLaunchUpdater();

    }

    @Override
    public void onError(int mode, MTOwnCloudHelper ownCloudHelper, Throwable t) {
        CustomExceptionHandler.log("Updater onError download occured");
        reLaunchUpdater();
    }

    @Override
    public void onUploadLog(String file) {

    }

    @Override
    public void onGlobalSetupLoaded(MTOwnCloudHelper ownCloudHelper, MTGlobalSetupRec setupRec) {

    }

    @Override
    public void onFileInfoLoaded(MTFileApkInfo newFileInfo) {
        // Проверить, есть ли уже такой скачанный файл (по длине)
        File existedFile = new File(Dao.getInstance(ctx).getUpdateApkPath(), ctx.getString(R.string.updateapk_filename));
        CustomExceptionHandler.log("onFileInfoLoaded check file: exists:"+existedFile.exists()+", fileLength="+existedFile.length()+", newFileLength="+newFileInfo.getSize());
        //if (newFileInfo.getSize() == 0) return; // FIXME: убрать потом чтобы обновлялось нормально
        if (existedFile.exists()  && existedFile.length() == newFileInfo.getSize()) { // TODO: потом сделать по MD5
            // ничего не делаем, т.к. файлы иентичные
            CustomExceptionHandler.log("onFileInfoLoaded files are identical, try to check version number with current");
            try {
                Updater.checkVersionAndInstall(existedFile, ctx);
            } catch (Exception e) {
                CustomExceptionHandler.logException("Error updating", e);
                e.printStackTrace();
            }
        } else {
            // Если нету - запустить скачку
            this.startDownLoadUpdate();
        }
    }

    private void reLaunchUpdater() {
        if (!Dao.getInstance(ctx).getTerminated()) {
            CustomExceptionHandler.log("reLaunchUpdater next try after " + ctx.getResources().getString(R.string.updateapk_interval_minutes) + " mins");
            Dao.getInstance(ctx).getExecutor().schedule(new Runnable() {
                @Override
                public void run() {
                    Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler.getLog());
                    Updater.this.startGetInfoUpdate();
                }
            }, Integer.parseInt(ctx.getResources().getString(R.string.updateapk_interval_minutes)), TimeUnit.MINUTES);
        }
    }
}
