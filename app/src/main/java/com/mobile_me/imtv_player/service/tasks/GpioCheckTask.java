package com.mobile_me.imtv_player.service.tasks;

import com.mobile_me.imtv_player.R;
import com.mobile_me.imtv_player.dao.Dao;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
        execCmd(new String[] {"echo", "gpio13", ">", "/sys/class/gpio/export"});
        execCmd(new String[] {"echo", "gpio14", ">", "/sys/class/gpio/export"});
        execCmd(new String[] {"echo", "out", ">", "/sys/class/gpio/gpio13/direction"});
        execCmd(new String[] {"echo", "input", ">", "/sys/class/gpio/gpio14/direction"});



        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                CustomExceptionHandler.log("GpioCheckTask starts");
                execCmd(new String[] {"echo", "1", ">", "/sys/class/gpio/gpio13/value"});
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                execCmd(new String[] {"echo", "0", ">", "/sys/class/gpio/gpio13/value"});

                Process process14 = null;
                try {
                    process14 = Runtime.getRuntime().exec(new String[] {   "cat", "/sys/class/gpio/gpio14/value" });
                    BufferedReader in = new BufferedReader(new InputStreamReader(process14.getInputStream()));
                    String b = in.readLine();
                    if (b != null) {
                        CustomExceptionHandler.log("GpioCheckTask 14 response " + b );
                        lastGpio14Data = b;
                    }
                } catch (Throwable t) {
                    CustomExceptionHandler.log("GpioCheckTask 14 error " + t.getMessage() );
                } finally {
                    if (process14 != null) process14.destroy();
                }
            }
        }, 0, Long.parseLong(dao.getContext().getResources().getString(R.string.checkgpio_interval_seconds)), TimeUnit.SECONDS);
    }

    private void execCmd(String[] cmd) {
        CustomExceptionHandler.log("GpioCheckTask task " + cmd );
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
