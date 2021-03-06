package com.mobile_me.imtv_player.dao;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.os.EnvironmentCompat;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.mobile_me.imtv_player.BuildConfig;
import com.mobile_me.imtv_player.R;
import com.mobile_me.imtv_player.model.MTGlobalSetupRec;
import com.mobile_me.imtv_player.model.MTGpsPoint;
import com.mobile_me.imtv_player.model.MTPlayList;
import com.mobile_me.imtv_player.service.LogUpload;
import com.mobile_me.imtv_player.service.MTPlayListManager;
import com.mobile_me.imtv_player.service.SettingsLoader;
import com.mobile_me.imtv_player.service.tasks.GpioCheckTask;
import com.mobile_me.imtv_player.service.tasks.WifiCheckTask;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;
import com.mobile_me.imtv_player.util.RootUtils;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Отвечает за локальные данные
 * Created by pasha on 8/14/16.
 */
public class Dao {

    public final static String PATH_KEY = "base_path_key";
    public final static String DEVICEID_KEY = "deviceid_key";
    public final static String LASTTIMESETTINGS_KEY = "lasttimesettings_key";
    public final static String MIN_COUNT_FREE = "min_count_free";
    public final static String COUNT_DAYS_BEFORE = "count_days_before";
    public final static String STAT_SENT_TIME = "stat_send_time";
    private static final long GPS_COORD_ACTUAL_TIMEOUT = 10 * 1000;


    private static Dao instance;

    private Context ctx;

    private PlayListFixedDBHelper mPlayListFixedDBHelper;
    private StatisticDBHelper mStatisticDBHelper;
    private SharedPreferences mSharedPreferences;

    private File baseFolder;
    private File videoPath;
    private File updateApkPath;

    String deviceId;
    private MTPlayListManager playListManager;
    private MTPlayListManager playListManager2;
    private Boolean isTerminated = false;
    private Long lastTimeSettings = null;
    private MTGlobalSetupRec setupRec = null;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private MTGpsPoint lastGpsCoordinate;
    private long lastGpsTime;
    private WifiCheckTask wifiCheckTask;
    private GpioCheckTask gpioCheckTask;

    public static Dao getInstance(Context ctx) {
        if (instance == null) {
            instance = new Dao(ctx);
        }
        return instance;
    }

    public Dao(Context ctx) {
        this.ctx = ctx;
        this.mPlayListFixedDBHelper = new PlayListFixedDBHelper(this.ctx);
        this.mStatisticDBHelper = new StatisticDBHelper(this.ctx);
        this.mSharedPreferences = ctx.getSharedPreferences("settings", Activity.MODE_PRIVATE);
        MTGlobalSetupRec r = new MTGlobalSetupRec();
        CustomExceptionHandler.log("try load previous setup rec");
        try {
            r.setMin_count_free(this.mSharedPreferences.getLong(MIN_COUNT_FREE, -1));
            r.setCount_days_before(this.mSharedPreferences.getLong(COUNT_DAYS_BEFORE, -1));
            r.setStats_send_time(this.mSharedPreferences.getLong(STAT_SENT_TIME, 30));
            this.setupRec = r;
            CustomExceptionHandler.log("setup rec:"+r);
        } catch (Exception e) {
            e.printStackTrace();
            CustomExceptionHandler.logException("no previous setup rec", e);
//            CustomExceptionHandler.log();
        }


/*        TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = telephonyManager.getDeviceId();
        */
        WifiManager manager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        while (!manager.isWifiEnabled()) {
            manager.setWifiEnabled(true);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        WifiInfo info = manager.getConnectionInfo();
        deviceId = info.getMacAddress().replace(":", "");

//deviceId = "b8b58378e361";  // actual
//deviceId = "f8a62d97e8ca";
        CustomExceptionHandler.log("version:"+BuildConfig.VERSION_CODE);
        CustomExceptionHandler.log("deviceId:"+deviceId);
        String savedDeviceID = mSharedPreferences.getString(DEVICEID_KEY, null);
        CustomExceptionHandler.log("savedDeviceID:"+savedDeviceID);
        if (savedDeviceID != null) {
            if (deviceId == null) {
                deviceId = savedDeviceID;
            }
            if (!deviceId.equals(savedDeviceID)) {
                Toast.makeText(ctx, "Необходимо переустановить приложение!", Toast.LENGTH_LONG).show();
                System.exit(0);
            }
        } else {
            SharedPreferences.Editor ed = mSharedPreferences.edit();
            ed.putString(DEVICEID_KEY, deviceId);
            ed.commit();
        }

        String lts = mSharedPreferences.getString(LASTTIMESETTINGS_KEY, null);
        if (lts != null) {
            lastTimeSettings = Long.parseLong(lts);
            // Если интервал превышает максимальный - обнулить
            Date ltsD = new Date(lastTimeSettings + Integer.parseInt(ctx.getString(R.string.globalini_expire_interval_minutes)) * 60 * 1000);
            if (ltsD.before(Calendar.getInstance().getTime())) {
                setLastTimeSettings(null);
            }
        }

        CustomExceptionHandler.log("lastTimeSettings:"+lastTimeSettings);
        CustomExceptionHandler.log("is device rooted="+ RootUtils.isDeviceRooted());

        Calendar cal = Calendar.getInstance();
        Date timeNow = cal.getTime();
        CustomExceptionHandler.log("timeNow:"+timeNow +",timeNowMS="+timeNow.getTime()+",timeNow.getYear()="+timeNow.getYear());
        if (timeNow.getYear() == 70) {
//            CustomExceptionHandler.log("exiting");
//            System.exit(0);
        }

        // базовый путь
        baseFolder = definePathToVideo();

        // создадим директории
        videoPath = new File(baseFolder, "Video");
        videoPath.mkdir();
        updateApkPath = new File(Environment.getExternalStorageDirectory(), "imtv");
        updateApkPath.mkdir();

        wifiCheckTask = new WifiCheckTask(this);
        gpioCheckTask = new GpioCheckTask(this);
        CustomExceptionHandler.log("DAO created");
        CustomExceptionHandler.log("baseFolder="+baseFolder.getAbsolutePath());
        CustomExceptionHandler.log("videoPath="+getVideoPath());
        CustomExceptionHandler.log("updateApkPath="+getUpdateApkPath());
        this.mStatisticDBHelper.deleteOldData();
    }

    private File definePathToVideo() {
        File path = null;
        String p = mSharedPreferences.getString(PATH_KEY, null);
        if (p != null) {
            path = new File(p);
        }
        CustomExceptionHandler.log("define base path stored path="+path);
        // если пусто
        if (p == null || !Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(path))
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(EnvironmentCompat.getStorageState(path))
                || !path.exists()
                || !path.canWrite() ) {
            path = getBestAvailableFilesRoot();
            CustomExceptionHandler.log("define base path getbestpath path="+path);
            SharedPreferences.Editor ed = mSharedPreferences.edit();
            ed.putString(PATH_KEY, path.getAbsolutePath());
            ed.commit();
        }
        path.mkdirs();
        return path;
    }

    private File getBestAvailableFilesRoot() {
        // FIXME: переделать на приоритеты (usb - 1, ext_sd - 2, emulated - 3, если нет ничего - то локальное)

        File[] roots = new File[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            roots = ctx.getExternalFilesDirs(null);
        } else {
            roots = ContextCompat.getExternalFilesDirs(ctx, null);
        }
        if (roots != null) {
            File best = null;
            for (File root : roots) {
                if (root == null) {
                    continue;
                }

                if (Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(root))
                        &&  !Environment.MEDIA_MOUNTED_READ_ONLY.equals(EnvironmentCompat.getStorageState(root))
                        && root.exists()
                        && root.canWrite() ) {
                    CustomExceptionHandler.log("getBestAvailableFilesRoot root="+root);
                    long freeSize = root.getFreeSpace();
                    if ((best == null  || freeSize > best.getFreeSpace())) {
                        best = root;
                    }
                }
            }
            if (best != null) {
                return best;
            }
        }
        // Worst case, resort to internal storage
        return ctx.getFilesDir();
    }

    public String getDeviceId() {
        return deviceId;
    }


    public MTPlayListManager getPlayListManager() {
        if (playListManager == null) {
            playListManager = new MTPlayListManager(this.ctx, MTPlayList.TYPEPLAYLIST_1);
        }
        return playListManager;
    }

    public MTPlayListManager getPlayListManager2() {
        if (playListManager2 == null) {
            playListManager2 = new MTPlayListManager(this.ctx, MTPlayList.TYPEPLAYLIST_2);
        }
        return playListManager2;
    }

    public MTPlayListManager getPlayListManagerByType(int playListType) {
        if (playListType == MTPlayList.TYPEPLAYLIST_2) {
            return getPlayListManager2();
        }
        return getPlayListManager();
    }

    public Context getContext() {
        return ctx;
    }


    public Boolean getTerminated() {
        return isTerminated;
    }

    public void setTerminated(Boolean terminated) {
        isTerminated = terminated;
    }


    public Long getLastTimeSettings() {
        return lastTimeSettings;
    }

    public void setLastTimeSettings(Long lastTimeSettings) {
        this.lastTimeSettings = lastTimeSettings;
        CustomExceptionHandler.log("set savedDeviceID to :"+lastTimeSettings);
        SharedPreferences.Editor ed = mSharedPreferences.edit();
        if (lastTimeSettings == null) {
            ed.putString(LASTTIMESETTINGS_KEY, null);
        } else {
            ed.putString(LASTTIMESETTINGS_KEY, Long.toString(lastTimeSettings));
        }
        ed.apply();
    }

    public ScheduledExecutorService getExecutor() {
        return this.executorService;
    }


    public StatisticDBHelper getmStatisticDBHelper() {
        return mStatisticDBHelper;
    }

    public MTGlobalSetupRec getSetupRec() {
        return setupRec;
    }

    public void setSetupRec(MTGlobalSetupRec setupRec) {
        CustomExceptionHandler.log("setupRec try set to= " + setupRec);
        this.setupRec = setupRec;
        SharedPreferences.Editor ed = mSharedPreferences.edit();
        ed.putLong(MIN_COUNT_FREE, (setupRec == null) ? -1 : setupRec.getMin_count_free() == null ? -1 : setupRec.getMin_count_free());
        ed.putLong(COUNT_DAYS_BEFORE, (setupRec == null) ? -1 : setupRec.getCount_days_before() == null ? -1 : setupRec.getCount_days_before());
        ed.putLong(STAT_SENT_TIME, (setupRec == null) ? -1 : setupRec.getStats_send_time() == null ? 30 : setupRec.getStats_send_time());
        ed.apply();
        CustomExceptionHandler.log("setupRec has set  ");
    }
    public String getVideoPath() {
        return videoPath.getAbsolutePath();
    }

    public String getUpdateApkPath() {
        return updateApkPath.getAbsolutePath();
    }

    public void updateGpsCoord(MTGpsPoint lastGpsCoordinate) {
        this.lastGpsCoordinate = lastGpsCoordinate;
        this.lastGpsTime = System.currentTimeMillis();
        MTPlayListManager playListManager1 = getPlayListManagerByType(MTPlayList.TYPEPLAYLIST_1);
        playListManager1.checkAndMapGeoVideo(this.lastGpsCoordinate);

    }

    public MTGpsPoint getLastGpsCoordinate() {
        if ((System.currentTimeMillis() - lastGpsTime) > GPS_COORD_ACTUAL_TIMEOUT) {
            return null;
        }
        return lastGpsCoordinate;
    }

    public GpioCheckTask getGpioCheckTask() {
        return gpioCheckTask;
    }


    public PlayListFixedDBHelper getPlayListFixedDBHelper() {
        return mPlayListFixedDBHelper;
    }


}
