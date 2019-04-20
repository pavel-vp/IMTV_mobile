package com.mobile_me.imtv_player.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.mobile_me.imtv_player.R;
import com.mobile_me.imtv_player.dao.Dao;
import com.mobile_me.imtv_player.model.MTGpsPoint;
import com.mobile_me.imtv_player.model.MTPlayList;
import com.mobile_me.imtv_player.model.MTPlayListRec;
import com.mobile_me.imtv_player.service.MTLoaderManager;
import com.mobile_me.imtv_player.service.MTPlayListManager;
import com.mobile_me.imtv_player.service.StatUpload;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;

import java.io.File;
import java.util.Calendar;

/**
 * Created by pasha on 10/12/16.
 */
public class MainActivity2 extends Activity implements SensorEventListener, LocationListener {

    private static final int REQUEST_CODE_LOCATION = 1;
    private static final int MIN_TIME_INTERVAL_COORDINATE_MS = 1000;
    //    VideoView vw1;
    VideoView vw2;
    MTLoaderManager loaderManager;
    Dao dao;
    private boolean isActive = false;
    private long lastStartPlayFile;

    boolean isInPreparing = false;
    boolean isInPreparing2 = false;
    private boolean is2Players = false;
    private Handler handler = new Handler();

    private SimpleExoPlayerView exoPlayerView;
    private SimpleExoPlayer exoPlayer;
    private LocationManager locationManager;
    private String provider;
    private volatile String fileToPlay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);
        Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler.getLog());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        lastStartPlayFile = Calendar.getInstance().getTimeInMillis();
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor TempSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mSensorManager.registerListener(this, TempSensor, SensorManager.SENSOR_DELAY_NORMAL);

        exoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exoplayer);
        exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);

        exoPlayerView.hideController();
        exoPlayerView.setControllerVisibilityListener(new PlaybackControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int i) {
                if (i == 0) {
                    exoPlayerView.hideController();
                }
            }
        });

        //vw2 = (VideoView) findViewById(R.id.videoView2);

        loaderManager = MTLoaderManager.getInstance(this);
        dao = Dao.getInstance(this);


/*        vw2.setMediaController(null);
        vw2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                CustomExceptionHandler.log("onCompletion 2");
                playNextVideoFile(vw2);
            }
        });
        */
        is2Players = Integer.parseInt(getResources().getString(R.string.playlists_count)) > 1;

        if (is2Players) {
            // установить размеры плееров (первый - 4 на 3, остальной - оставшаяся ширина
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int height = displaymetrics.heightPixels;
            int width = height * 4 / 3;
            ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height - 1);
            //vw1.setLayoutParams(layoutParams);
            //holder.setFixedSize(width, height-1);

            vw2.setVisibility(View.VISIBLE);
            layoutParams = new LinearLayout.LayoutParams(displaymetrics.widthPixels - width, height - 1);
            vw2.setLayoutParams(layoutParams);
            //holder2.setFixedSize(displaymetrics.widthPixels - width, height-1);
        } else {
            //    ViewGroup.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
            //    vw1.setLayoutParams(layoutParams);
/*            layoutParams = new LinearLayout.LayoutParams(0, 0, 0);
            vw2.setLayoutParams(layoutParams);
            vw2.setVisibility(View.GONE);*/
        }

        StatUpload.getInstance(dao).startUploadStat();
        checkLocationPermission();
    }

    Player.EventListener exoPlayerListener = new Player.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            CustomExceptionHandler.log("onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            CustomExceptionHandler.log("onLoadingChanged " + isLoading);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String s = null;
            switch (playbackState) {
                case Player.STATE_IDLE:
                    s = "STATE_IDLE";
                    break;
                case Player.STATE_BUFFERING:
                    s = "STATE_BUFFERING";
                    break;
                case Player.STATE_READY:
                    s = "STATE_READY";
                    break;
                case Player.STATE_ENDED:
                    s = "STATE_ENDED";
            }
            CustomExceptionHandler.log("onPlayerStateChanged " + playWhenReady + "," + s);
            if (playbackState == Player.STATE_ENDED) {
                playNextVideoFile(true);
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            CustomExceptionHandler.logException("onPlayerError", error);
            playNextVideoFile(true);
        }

        @Override
        public void onPositionDiscontinuity() {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }
    };

    private void initializePlayer() {
        if (exoPlayer == null) {
            releasePlayer();
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(), new DefaultLoadControl());
            exoPlayer.addListener(exoPlayerListener);
            exoPlayerView.setPlayer(exoPlayer);
        }
    }

    private void initializePlayerAndPlayFile(String filePath) {
        initializePlayer();
        Uri uri = Uri.fromFile(new File(filePath));
        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);

        exoPlayer.prepare(audioSource);
        exoPlayer.setPlayWhenReady(true);
        //   exoPlayer.seekTo(0);
        // сразу следующий
        getNextVideoFileToPlay();

    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource(uri,
                new DefaultHttpDataSourceFactory("ua"),
                new DefaultExtractorsFactory(), null, null);
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        exoPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            /*exoPlayer.removeListener(exoPlayerListener);
            exoPlayer.release();
            exoPlayer = null;*/
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(provider, MIN_TIME_INTERVAL_COORDINATE_MS, 0, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideSystemUi();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.removeUpdates(this);
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Внимание")
                        .setMessage("Необходимо подтверить разрешение на использование координат")
                        .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity2.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_CODE_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        locationManager.requestLocationUpdates(provider, MIN_TIME_INTERVAL_COORDINATE_MS, 0, this);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }
    @Override
    protected void onStart() {
        CustomExceptionHandler.log("onStart");
        super.onStart();
        isActive = true;


        getNextVideoFileToPlay();
        new Thread(new Runnable() {
            @Override
            public void run() {
                CustomExceptionHandler.log("hang checking isactive="+isActive+", dao.getTerminated()="+dao.getTerminated());
                while (isActive && !dao.getTerminated()) {
                    // проверить на то что завис плеер (старт предыдущего проигрывания)
                    long now = Calendar.getInstance().getTimeInMillis();
                    CustomExceptionHandler.log("hang checking now-last="+(now - lastStartPlayFile));
                    if ((now - lastStartPlayFile) > (1000 * 60 * Long.parseLong(getString(R.string.hang_interval_minutes))) ) {
                        CustomExceptionHandler.log("hang detected");
                        dao.setTerminated(true);
                        break;
                    }
                    try {
                        Thread.sleep(60000);
//                        Thread.sleep(6);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (dao.getTerminated()) {
                    CustomExceptionHandler.log("finish by hang");
                    //MainActivity2.this.finish();
                    //System.exit(0);

                    try {
                        String command;
                        command = "reboot";
                        Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                        int res = 0;
                        res = proc.waitFor();
                        CustomExceptionHandler.log("res procReboot=" + res);
                    } catch (Exception e) {
                        CustomExceptionHandler.logException("error reboot - ", e);
                    }

                }
            }
        }).start();

        // запуск проигрывания сразу
        playNextVideoFile(false);
        if (is2Players) {
            playNextVideoFile(false);
        }

    }

    @Override
    protected void onStop() {
        CustomExceptionHandler.log("onStop");
        super.onStop();
        isActive = false;
        isInPreparing = false;
        isInPreparing2 = false;
        releasePlayer();
        if (is2Players) {
            vw2.stopPlayback();
        }
    }

    public void playNextVideoFile(boolean isAsync) {
        if (isAsync) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initializePlayerAndPlayFile(MainActivity2.this.fileToPlay);
                }
            }, 100);
        } else {
            while(this.fileToPlay == null){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            initializePlayerAndPlayFile(MainActivity2.this.fileToPlay);
        }
    }


    public void getNextVideoFileToPlay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                lastStartPlayFile = Calendar.getInstance().getTimeInMillis();
                int type = MTPlayList.TYPEPLAYLIST_1 ;
                CustomExceptionHandler.log("playNextVideoFile started. type="+type+",lastStartPlayFile="+lastStartPlayFile);
                logMemory();
    //            if (exoPlayer != null) {
//                    releasePlayer();
//            CustomExceptionHandler.log("playNextVideoFile playing exit, type="+type);
//            return;
  //              }

                String filePathToPlay = null;

                if (false) {
                    filePathToPlay.substring(1);
                }

                boolean forcedPlay = false;
                MTPlayListManager playListManager = Dao.getInstance(MainActivity2.this).getPlayListManagerByType(type );
                MTPlayListRec found = null;
                if (type == MTPlayList.TYPEPLAYLIST_1) {
                    // взять из плейлиста следующий непроигранный
                    if (playListManager.getPlayList() != null) {
                        found = playListManager.getNextVideoFileForPlay(forcedPlay);
                    }
                    // если непроигранного нет - значит все проиграли, запустим поиск с принудительнм возвращением хотя бы одного,
                    // при этом по логике отбора файла - он должен вернуть хотя бы один, и возможно сбросить все состояния проигрывания.
                    if (found == null) {
                        forcedPlay = true;
                        found = playListManager.getNextVideoFileForPlay(forcedPlay);
                    }
                    filePathToPlay = dao.getVideoPath() + found.getFilename();
                } else {
                    found = playListManager.getRandomFile();
                    filePathToPlay = dao.getVideoPath() + found.getFilename();
                }
                if (filePathToPlay != null && isActive && found != null) {
                    // запустить проигрывание этого файла
                    CustomExceptionHandler.log("playList start playing, type="+type+", filePathToPlay="+filePathToPlay);
                    setFileToPlay(filePathToPlay);
                    dao.getmStatisticDBHelper().addStat(found, dao.getLastGpsCoordinate());
                }
                if (forcedPlay) {
                    // запустить загрузку плейлиста TODO: не надо же уже?
                    //helper.loadPlayListFromServer();
                }
                CustomExceptionHandler.log("playNextVideoFile finished. type="+type);
            }
        }).start();
    }

    private void setFileToPlay(String file) {
        this.fileToPlay = file;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float temp = event.values[0];
        CustomExceptionHandler.log("sensor event="+event+", sensor="+event.sensor.getName()+ ", value="+temp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void logMemory() {
        Runtime info = Runtime.getRuntime();
        long freeSize = info.freeMemory();
        long totalSize = info.totalMemory();
        long maxSize = info.maxMemory();
        CustomExceptionHandler.log("memory: freeSize="+freeSize+", totalSize="+totalSize+ ", maxSize="+maxSize);
    }

    @Override
    public void onLocationChanged(Location location) {
        //MTGpsPoint point = new MTGpsPoint(56.858652, 53.221357);
        MTGpsPoint point = new MTGpsPoint(location.getLatitude(), location.getLongitude());
        Dao.getInstance(this).updateGpsCoord(point);
        CustomExceptionHandler.log("currentLocation: "+location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
