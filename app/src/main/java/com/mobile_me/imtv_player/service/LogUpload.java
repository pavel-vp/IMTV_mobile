package com.mobile_me.imtv_player.service;

import android.util.Base64;
import com.mobile_me.imtv_player.R;
        import com.mobile_me.imtv_player.dao.Dao;
import com.mobile_me.imtv_player.model.MTFileApkInfo;
import com.mobile_me.imtv_player.model.MTGlobalSetupRec;
import com.mobile_me.imtv_player.model.MTPlayList;
import com.mobile_me.imtv_player.model.MTPlayListRec;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;

import java.io.*;
import java.text.SimpleDateFormat;
        import java.util.Calendar;
        import java.util.zip.ZipEntry;
        import java.util.zip.ZipOutputStream;

public class LogUpload implements IMTCallbackEvent {
    Dao dao;
    MTOwnCloudHelper helper;
    SimpleDateFormat sdf;
    private File lastFile = null;

    private static LogUpload instance;

    public static LogUpload getInstance(Dao dao) {
        if (instance == null) {
            instance = new LogUpload(dao);
        }
        return instance;
    }

    public LogUpload(Dao dao) {
        this.dao = dao;
        helper = new MTOwnCloudHelper(null, dao.getContext(), this);
        sdf = new SimpleDateFormat("yyMMdd-HHmmss");
    }

    public synchronized void startUpload() {
        try {
            // Процесс фоновой загрузки логов на сервер
            CustomExceptionHandler.log("try startUpload");
            lastFile = null;

            String zipPostfix = dao.getDeviceId() + "_" + sdf.format(Calendar.getInstance().getTime());

            String logFileName = CustomExceptionHandler.getLog().getFileName();
            String tmpZipFileName = logFileName + "_" + zipPostfix + ".zip";
            File tmpFile = new File(tmpZipFileName);

            if (tmpFile.exists())
                tmpFile.delete();

            CustomExceptionHandler.log("tmpZipFileName=" + tmpZipFileName);
            // Пожать файл с логом
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmpFile)));
            ZipEntry e = new ZipEntry(zipPostfix + ".log");
            zos.putNextEntry(e);

            CustomExceptionHandler.log("start write");
            CustomExceptionHandler.log("sending new file=" + tmpZipFileName);

            // переключить на временный файл
            String logFileNameTmp = CustomExceptionHandler.getLog().switchToNewFile();
            File logFile = new File(logFileNameTmp);


            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(logFile));
            byte[] br = new byte[1024];
            int bytesRead;
            while ((bytesRead = bis.read(br)) != -1) {
                zos.write(br, 0, bytesRead);
            }

            bis.close();

            zos.flush();
            zos.close();
            // Удалить файл временный
            logFile.delete();

            lastFile = tmpFile;
            trySendFile();
        } catch (Exception e) {
            CustomExceptionHandler.logException("ошибка при отправке лога", e);
        }
    }

    private synchronized void trySendFile() {
        // Отправить пожатый файл с логом
        // отправить первый файлы zip в этой директории
        File[] fm = getAllZipFiles(lastFile.getParent());
        if (fm != null && fm.length > 0) {
            CustomExceptionHandler.log("try upload errorlog="+fm[0].getAbsolutePath());
            String zipBase64 = readFileAsStringAndEncode64(fm[0].getAbsolutePath());
            helper.sendLog(zipBase64, fm[0].getAbsolutePath());
        } else {
            CustomExceptionHandler.log("no more zip files to upload");
        }
    }

    private String readFileAsStringAndEncode64(String absolutePath) {
        try {
            BufferedReader br= new BufferedReader(new FileReader(absolutePath));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String data = Base64.encodeToString(sb.toString().getBytes(), Base64.DEFAULT);
            CustomExceptionHandler.log("log compressed to "+data.length());
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            CustomExceptionHandler.logException("error while compressing file to str", e);
        }
        return "";
    }

    private File[] getAllZipFiles( String dirName){
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename)
            { return filename.endsWith(".zip"); }
        } );

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
        CustomExceptionHandler.log("on sent zip file error");
       // trySendFile();
    }

    @Override
    public void onUploadLog(String localFileToUpload) {
        CustomExceptionHandler.log("sent = "+localFileToUpload);
        File tmpFile = new File(localFileToUpload);
        tmpFile.delete();
        trySendFile();
    }

    @Override
    public void onGlobalSetupLoaded(MTOwnCloudHelper ownCloudHelper, MTGlobalSetupRec setupRec) {

    }

    @Override
    public void onFileInfoLoaded(MTFileApkInfo fileInfo) {

    }

    public void reLaunchUploadLog() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!dao.getTerminated()) {
                    CustomExceptionHandler.log("Relaunch upload log");
                    LogUpload.this.startUpload();
                    try {
                        Thread.sleep(Integer.parseInt(dao.getContext().getResources().getString(R.string.uploadlogs_interval_minutes)) * 1000 * 60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
