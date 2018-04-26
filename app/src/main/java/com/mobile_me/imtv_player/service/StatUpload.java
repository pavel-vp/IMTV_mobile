package com.mobile_me.imtv_player.service;

import com.mobile_me.imtv_player.dao.Dao;
import com.mobile_me.imtv_player.model.*;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by pasha on 04.03.17.
 */
public class StatUpload implements IMTCallbackEvent  {

    Dao dao;
    MTOwnCloudHelper helper;
    SimpleDateFormat sdf;
    private Long lastIDExported = null;
    private File lastFile = null;

    private static StatUpload instance;


    public static StatUpload getInstance(Dao dao) {
        if (instance == null) {
            instance = new StatUpload(dao);
        }
        return instance;
    }

    private StatUpload(Dao dao) {
        this.dao = dao;
        helper = new MTOwnCloudHelper(null, dao.getContext(), this);
        sdf = new SimpleDateFormat("yyMMdd-HHmmss");
    }

    public void startUploadStat() {
        try {
            // Процесс фоновой загрузки статистики проигрывания на сервер
            CustomExceptionHandler.log("try startUpload stat");
            lastIDExported = null;
            // запишем данные статистики из лок.базы в файл
            List<MTStatRec> list = dao.getmStatisticDBHelper().getNotExportedStatList();
            if (list.size() > 0) {
                lastIDExported = list.get(list.size() - 1).getIdx();
                CustomExceptionHandler.log("start send");
                // отошлем файл
                helper.sendStat(list);
            }
        } catch (Exception e) {
            CustomExceptionHandler.logException("ошибка при отправке статистики", e);
            reLaunchUploadStat();
        }
    }


    @Override
    public void onPlayListLoaded(MTPlayList playList, MTOwnCloudHelper ownCloudHelper) {

    }

    @Override
    public void onVideoFileLoaded(MTPlayListRec file, MTOwnCloudHelper ownCloudHelper) {

    }

    @Override
    public void onUpdateFileLoaded(MTOwnCloudHelper ownCloudHelper) {

    }

    @Override
    public void onError(int mode, MTOwnCloudHelper ownCloudHelper, Throwable t) {
        CustomExceptionHandler.log("stat file upload error");
        reLaunchUploadStat();
    }

    @Override
    public void onUploadLog(String uploadedLocalFile) {
        // после успеха - сделать снова запуск через определенное число минут
        CustomExceptionHandler.log("stat file upload success");
        // проставим флаг экспорта
        if (lastIDExported != null) {
            dao.getmStatisticDBHelper().clearExportedStatList(lastIDExported);
        }
        reLaunchUploadStat();
    }

    @Override
    public void onGlobalSetupLoaded(MTOwnCloudHelper ownCloudHelper, MTGlobalSetupRec setupRec) {

    }

    @Override
    public void onFileInfoLoaded(MTFileApkInfo fileInfo) {

    }

    public void reLaunchUploadStat() {
        if (!dao.getTerminated()) {
            CustomExceptionHandler.log("Relaunch upload stat");
            dao.getExecutor().schedule(new Runnable() {
                @Override
                public void run() {
                    StatUpload.this.startUploadStat();
                }
            }, (dao.getSetupRec() == null || dao.getSetupRec().getStats_send_time() == null) ? 30 : dao.getSetupRec().getStats_send_time(), TimeUnit.MINUTES);
        }
    }

}
