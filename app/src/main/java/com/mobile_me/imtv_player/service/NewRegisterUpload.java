package com.mobile_me.imtv_player.service;

import com.mobile_me.imtv_player.dao.Dao;
import com.mobile_me.imtv_player.model.MTFileApkInfo;
import com.mobile_me.imtv_player.model.MTGlobalSetupRec;
import com.mobile_me.imtv_player.model.MTPlayList;
import com.mobile_me.imtv_player.model.MTPlayListRec;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;
import java.text.SimpleDateFormat;

/**
 * Created by pasha on 03.11.16.
 */
public class NewRegisterUpload implements IMTCallbackEvent {

    Dao dao;
    MTOwnCloudHelper helper;
    SimpleDateFormat sdf;
    private boolean success = false;

    private static NewRegisterUpload instance;

    public static NewRegisterUpload getInstance(Dao dao) {
        if (instance == null) {
            instance = new NewRegisterUpload(dao);
        }
        return instance;
    }

    public NewRegisterUpload(Dao dao) {
        this.dao = dao;
        helper = new MTOwnCloudHelper(null, dao.getContext(), this);
        sdf = new SimpleDateFormat("yyMMdd-HHmmss");
    }

    public void startRegisterAndUpload() {
        try {
            // Процесс фоновой загрузки логов на сервер
            CustomExceptionHandler.log("try startRegisterAndUpload");

            // Отправить
            helper.registerNewDevice();
        } catch (Exception e) {
            CustomExceptionHandler.logException("ошибка при отправке файла", e);
        }
    }

    @Override
    public void onPlayListFixedLoaded(MTPlayList playList, MTOwnCloudHelper ownCloudHelper) {

    }

    @Override
    public void onVideoFileLoaded(MTPlayListRec file, MTOwnCloudHelper ownCloudHelper) {

    }

    @Override
    public void onUpdateFileLoaded(MTOwnCloudHelper ownCloudHelper) {
        CustomExceptionHandler.log("newregisterUpload file uploaded");
        success = true;
    }

    @Override
    public void onError(int mode, MTOwnCloudHelper ownCloudHelper, Throwable t) {
        CustomExceptionHandler.log("newregisterUpload file upload error, no restarting ");
        //startRegisterAndUpload();
    }

    @Override
    public void onUploadLog(String uploadedLocalFile) {

    }

    @Override
    public void onGlobalSetupLoaded(MTOwnCloudHelper ownCloudHelper, MTGlobalSetupRec setupRec) {

    }

    @Override
    public void onFileInfoLoaded(MTFileApkInfo fileInfo) {

    }


    public boolean isSuccess() {
        return success;
    }

}
