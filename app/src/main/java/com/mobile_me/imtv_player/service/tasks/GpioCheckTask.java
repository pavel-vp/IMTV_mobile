package com.mobile_me.imtv_player.service.tasks;

import com.mobile_me.imtv_player.R;
import com.mobile_me.imtv_player.dao.Dao;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GpioCheckTask {
    private Dao dao;

    private String lastGpio14Data;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public GpioCheckTask(final Dao dao) {
        this.dao = dao;

        // Только один раз (инициализация)
        execCmd("sh -c echo 13 > /sys/class/gpio/export");
        execCmd("sh -c echo 14 > /sys/class/gpio/export");
        execCmd("sh -c echo out > /sys/class/gpio/gpio13/direction");
        execCmd("sh -c echo input > /sys/class/gpio/gpio14/direction");



        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                CustomExceptionHandler.log("GpioCheckTask starts");
                execCmd("sh -c echo 1 > /sys/class/gpio/gpio13/value");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                execCmd("sh -c echo 0 > /sys/class/gpio/gpio13/value");

            }
        }, 0, Long.parseLong(dao.getContext().getResources().getString(R.string.checkgpio_interval_seconds)), TimeUnit.SECONDS);
    }

    private void execCmd(String cmd) {
        CustomExceptionHandler.log("GpioCheckTask task " + cmd);
        Process process1 = null;
        try {
            process1 = Runtime.getRuntime().exec(cmd);
        } catch (Throwable t) {
            CustomExceptionHandler.log("GpioCheckTask error " + t.getMessage() );
        } finally {
            if (process1 != null) process1.destroy();
        }
    }

    public String getLastGpio14Data() {
        return lastGpio14Data;
    }


}
