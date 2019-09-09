package com.mobile_me.imtv_player.service.tasks;

import android.content.Context;
import android.net.wifi.WifiManager;
import com.mobile_me.imtv_player.R;
import com.mobile_me.imtv_player.dao.Dao;
import com.mobile_me.imtv_player.service.SettingsLoader;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;

import java.util.concurrent.TimeUnit;

public class WifiCheckTask {

    private Dao dao;

    public WifiCheckTask(Dao dao) {
        this.dao = dao;
        reLaunchCheckWifi();
    }


    class WifiCheckHelper implements Runnable {

        @Override
        public void run() {
            CustomExceptionHandler.log("WifiCheckHelper starts");
            WifiManager manager = (WifiManager) dao.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (manager != null && !manager.isWifiEnabled()) {
                CustomExceptionHandler.log("WifiCheckHelper no wifi");
                manager.setWifiEnabled(true);
                CustomExceptionHandler.log("WifiCheckHelper try turn on");
            }
            reLaunchCheckWifi();
        }
    }

    private void reLaunchCheckWifi() {
        if (!dao.getTerminated()) {
            CustomExceptionHandler.log("WifiCheckHelper reLaunchCheckWifi next try after " + dao.getContext().getResources().getString(R.string.checkwifi_interval_minutes) + " mins");
            dao.getExecutor().schedule(new WifiCheckHelper(), Integer.parseInt(dao.getContext().getResources().getString(R.string.checkwifi_interval_minutes)), TimeUnit.MINUTES);
        }
    }


}
