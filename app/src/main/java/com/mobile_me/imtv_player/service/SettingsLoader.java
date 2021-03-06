package com.mobile_me.imtv_player.service;

import com.mobile_me.imtv_player.R;
import com.mobile_me.imtv_player.dao.Dao;
import com.mobile_me.imtv_player.model.MTFileApkInfo;
import com.mobile_me.imtv_player.model.MTGlobalSetupRec;
import com.mobile_me.imtv_player.model.MTPlayList;
import com.mobile_me.imtv_player.model.MTPlayListRec;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Класс для работы с регистрацией устройства на сервере и выкачивания глобальных настроек
 * - необходимо  проверять наличие файла настроек в Settings/FromCRM/  при старте приложения
 * - при удачном скачивании - сохранять локально дату последнего обновления настроек
 *
 * Created by pasha on 31.10.16.
 */
public class SettingsLoader implements IMTCallbackEvent {

    Dao dao;
    MTOwnCloudHelper helper;
    SimpleDateFormat sdf;

    private static SettingsLoader instance;

    public static SettingsLoader getInstace(Dao dao) {
        if (instance == null) {
            instance = new SettingsLoader(dao);
        }
        return instance;
    }

    public SettingsLoader(Dao dao) {
        this.dao = dao;
        helper = new MTOwnCloudHelper(null, dao.getContext(), this, MTOwnCloudHelper.TYPEFILE_SIMPLE);
        sdf = new SimpleDateFormat("yyMMdd-HHmmss");
    }

    public void tryLoadSettings() {
        helper.loadGlobalSettings();
    }

    @Override
    public void onPlayListFixedLoaded(MTPlayList playList, MTOwnCloudHelper ownCloudHelper) {

    }

    @Override
    public void onVideoFileLoaded(MTPlayListRec file, MTOwnCloudHelper ownCloudHelper) {

    }

    @Override
    public void onUpdateFileLoaded(MTOwnCloudHelper ownCloudHelper) {

    }

    @Override
    public void onError(int mode, MTOwnCloudHelper ownCloudHelper, Throwable t) {
        // просто заново перезапустим, не обновляя время
        dao.setLastTimeSettings(null);
        // запустим регистрцию
        CustomExceptionHandler.log("start NewRegisterUpload because it need to");
        NewRegisterUpload.getInstance(dao).startRegisterAndUpload();
        reLaunchSettingLoader();
    }

    @Override
    public void onUploadLog(String uploadedLocalFile) {

    }

    @Override
    public void onGlobalSetupLoaded(MTOwnCloudHelper ownCloudHelper, MTGlobalSetupRec setupRec) {
        // загрузили файл настроек
            // отметим время
            dao.setLastTimeSettings(Calendar.getInstance().getTime().getTime());
            dao.setSetupRec(setupRec);
        reLaunchSettingLoader();
    }

    @Override
    public void onFileInfoLoaded(MTFileApkInfo fileInfo) {

    }

    private void reLaunchSettingLoader() {
        if (!dao.getTerminated()) {
            CustomExceptionHandler.log("reLaunchSettingLoader next try after " + dao.getContext().getResources().getString(R.string.checkid_interval_minutes) + " mins");
            dao.getExecutor().schedule(new Runnable() {
                @Override
                public void run() {
                    Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler.getLog());
                    SettingsLoader.this.tryLoadSettings();
                }
            }, Integer.parseInt(dao.getContext().getResources().getString(R.string.checkid_interval_minutes)), TimeUnit.MINUTES);
        }
    }

}
