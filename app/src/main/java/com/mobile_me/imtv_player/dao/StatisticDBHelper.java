package com.mobile_me.imtv_player.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import android.os.Environment;
import com.mobile_me.imtv_player.R;
import com.mobile_me.imtv_player.model.MTGpsPoint;
import com.mobile_me.imtv_player.model.MTPlayListRec;
import com.mobile_me.imtv_player.model.MTStatRec;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by pasha on 20.12.16.
 */
public class StatisticDBHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "imtv_player_db_stat";
    private final static int DB_VERSION = 7;
    private static final String TABLE_NAME = "playstatdata";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");

    private static final String IDX = "idx";
    private static final String ID = "id";
    private static final String VPID = "vpid";
    private static final String DT = "dt";
    private static final String DTDAYHOUR = "dtdayhour";
    private static final String DURATION = "duration";
    private static final String POINT_LAT = "point_lat";
    private static final String POINT_LON = "point_lon";
    private static final String TYPE = "typelist";
    private static final String EXPORTED = "exported";
    private static final String GPIO14 = "gpio14";

    Context context;
    public static final String CREATE_TABLE = "create table " + TABLE_NAME + " ( "
            + IDX + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + ID + " integer, "
            + VPID + " integer, "
            + DT + " long, "
            + DTDAYHOUR + " long, "
            + DURATION + " int, "
            + POINT_LAT + " double, "
            + POINT_LON + " double, "
            + TYPE + " text, "
            + EXPORTED + " int,"
            + GPIO14 + " text "
            + ");";

    public static final String DROP_TABLE = "drop table "+ TABLE_NAME + ";";

    public StatisticDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }


    PriorityQueue<MTPlayListRec> queueStat = new PriorityQueue<>(1000, new Comparator<MTPlayListRec>() {
        @Override
        public int compare(MTPlayListRec lhs, MTPlayListRec rhs) {
            if (lhs.getIdx() > rhs.getIdx()) return -1;
            if (lhs.getIdx() < rhs.getIdx()) return 1;
            return 0;
        }
    });
    private long idx = 1;



    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(StatisticDBHelper.CREATE_TABLE);
        } catch (Exception e) {
            CustomExceptionHandler.logException("error ", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        for (int ver = oldVersion; ver<= newVersion; ver++) {
//            if (ver == 2) {
        CustomExceptionHandler.log("statDB onUpgrade oldVer:"+oldVersion+", newVersion:"+newVersion);
                db.execSQL(StatisticDBHelper.DROP_TABLE);
                db.execSQL(StatisticDBHelper.CREATE_TABLE);
//            }
        //}
        CustomExceptionHandler.log("onUpgrade done");
    }

    // Добавляем запись статистики
    public void addStat(MTPlayListRec recExt, final MTGpsPoint loc, Long timeMS) {
        final MTPlayListRec copy = recExt.getCopy();

        idx++;
        copy.setIdx(idx);
        copy.setPlayedTime(timeMS);
        CustomExceptionHandler.log("write stat  recExt="+copy);
        queueStat.add(copy);
        new Thread(new Runnable() {
            @Override
            public void run() {

                writeStatRecInDBBackgroud(copy, loc);
            }
        }).start();
        CustomExceptionHandler.log("write stat rec end ");
    }

    protected void writeStatRecInDBBackgroud(MTPlayListRec copy, MTGpsPoint loc) {
        synchronized (Dao.getInstance(context)) {
            try {
                SQLiteDatabase db = this.getWritableDatabase();

                ContentValues cv = new ContentValues();
                cv.put(ID, copy.getId());
                cv.put(VPID, copy.getVpid());
                cv.put(DURATION, copy.getDuration());
                Calendar cal = Calendar.getInstance();
                cv.put(DT, cal.getTimeInMillis());
                cv.put(DTDAYHOUR, Long.parseLong(sdf.format(cal.getTime())));
                if (loc != null) {
                    cv.put(POINT_LAT, loc.getLatitude());
                    cv.put(POINT_LON, loc.getLongitude());
                }
                cv.put(TYPE, copy.getType());
                cv.put(GPIO14, Dao.getInstance(context).getGpioCheckTask().getLastGpio14Data());
                db.insert(TABLE_NAME, null, cv);
            } catch (Exception e) {
                CustomExceptionHandler.logException("write stat rec error ", e);
                e.printStackTrace();
            }
        }
    }

    public void deleteOldData() {
        CustomExceptionHandler.log("start delete olddata");
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL(StatisticDBHelper.DROP_TABLE);
        } catch (Exception e) {
            CustomExceptionHandler.logException("error data", e);
        }
        db.execSQL(StatisticDBHelper.CREATE_TABLE);
//        db.execSQL("truncate table "+ TABLE_NAME + " ");
//        db.execSQL("delete from "+ TABLE_NAME + " where " + ID + " < (select min("+ID+" from (select "+ID+" from " + TABLE_NAME + " order by "+ ID + " ASC LIMIT 1000))");
        CustomExceptionHandler.log("end delete olddata");

/*        File filePath = new File(path);
        int c = 0;
        for (File f : filePath.listFiles()) {
            f.delete();
            c++;
        }
        CustomExceptionHandler.log("end delete old stat files ="+c);*/
    }

    // Метод для получения данных из статистики:
    // - читает данные по статистике за указанные последние N минут
    public List<MTPlayListRec> readStatOnLastNMins(int lastMinutes) {
        // попробовать прочитать локально сохраненный плейлист.
        CustomExceptionHandler.log("start read last minutes: "+ lastMinutes);
        long timeStart = Calendar.getInstance().getTimeInMillis() - (lastMinutes * 60 * 1000);
        CustomExceptionHandler.log("timeStart= "+timeStart);
        List<MTPlayListRec> res = new ArrayList<>();


        PriorityQueue<MTPlayListRec> queueCopy = new PriorityQueue<>(queueStat);
        boolean needNext = true;
        while (needNext) {
            needNext = false;
            MTPlayListRec rec = queueCopy.poll();
            if (rec != null) {
                res.add(rec);
//                CustomExceptionHandler.log("statrec="+rec);
                if (res.size() < 100) {
                    needNext = true;
                }
            }
        }


/*
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cur = db.rawQuery("select * from "+ TABLE_NAME + " where "+ DT + " >= ?  order by " + IDX +" DESC LIMIT 100", new String[] { String.valueOf(timeStart)});
        if (cur != null ) {
            if (cur.moveToFirst()) {
                do {
                    MTPlayListRec rec = new MTPlayListRec();
                    rec.setId(cur.getLong(cur.getColumnIndex(ID)));
                    rec.setDuration(cur.getLong(cur.getColumnIndex(DURATION)));
                    rec.setType(cur.getString(cur.getColumnIndex(TYPE)));
                    rec.setFilename(cur.getString(cur.getColumnIndex(DTDAYHOUR)));
                    res.add(rec);
                    CustomExceptionHandler.log("statrec="+rec);
                }  while (cur.moveToNext()) ;
            }
            cur.close();
        }
        */
        CustomExceptionHandler.log("end read ");
        return res;
    }

    public void clearExportedStatList(Long lastIDExported) {
        synchronized (Dao.getInstance(context)) {
            CustomExceptionHandler.log("start clear until " + lastIDExported);
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("update " + TABLE_NAME + " set " + EXPORTED + "= 1 where " + IDX + " <= ? ", new String[]{String.valueOf(lastIDExported)});
            CustomExceptionHandler.log("end clear stat");
        }
    }

    // Метод читает из статистики и возвращает не выгруженные записи ранее
    public List<MTStatRec> getNotExportedStatList() {
            CustomExceptionHandler.log("start read stat to log ");
            SQLiteDatabase db = this.getReadableDatabase();
            List<MTStatRec> list = new ArrayList<>();
            Cursor cur = db.rawQuery("select * from " + TABLE_NAME + " where " + EXPORTED + " is null order by " + IDX, null);
            if (cur != null) {
                if (cur.moveToFirst()) {
                    do {
                        MTStatRec rec = new MTStatRec();
                        rec.setIdx(cur.getLong(cur.getColumnIndex(IDX)));
                        rec.setId(cur.getLong(cur.getColumnIndex(ID)));
                        rec.setVpid(cur.getLong(cur.getColumnIndex(VPID)));
                        rec.setDt(cur.getString(cur.getColumnIndex(DT)));
                        rec.setLat(cur.getDouble(cur.getColumnIndex(POINT_LAT)));
                        rec.setLon(cur.getDouble(cur.getColumnIndex(POINT_LON)));
                        list.add(rec);
                    } while (cur.moveToNext());
                }
                cur.close();
            }
            CustomExceptionHandler.log("end read stat, list.size = " + list.size());
            return list;
    }
}
