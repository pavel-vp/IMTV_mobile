package com.mobile_me.imtv_player.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import com.mobile_me.imtv_player.model.MTDateRec;
import com.mobile_me.imtv_player.model.MTPlayList;
import com.mobile_me.imtv_player.model.MTPlayListRec;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;
import com.mobile_me.imtv_player.util.MTGpsUtils;

public class PlayListFixedDBHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "imtv_player_db_playlistfixed";
    private final static int DB_VERSION = 9;
    private static final String TABLE_NAME = "playlistfixed";

    private static final String ID = "id";
    private static final String VPID = "vpid";
    private static final String FILENAME = "filename";
    private static final String TYPE = "typerec";
    private static final String SIZE = "size";
    private static final String PERIODICITY = "periodicity";
    private static final String DURATION = "duration";
    private static final String DTFROM = "dtfrom";
    private static final String DTTO = "dtto";
    private static final String MD5 = "md5";

    Context context;
    public static final String CREATE_TABLE = "create table " + TABLE_NAME + " ( "
            + ID + " integer, "
            + VPID + " integer, "
            + FILENAME + " text, "
            + TYPE + " text,"
            + SIZE + " int, "
            + PERIODICITY + " int,"
            + DURATION + " int, "
            + DTFROM + " text,"
            + DTTO + " text,"
            + MD5 + " text"
            + ");";

    public static final String DELETE_TABLE = "delete from "+ TABLE_NAME;

    public PlayListFixedDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
            CustomExceptionHandler.logException("error ", e);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CustomExceptionHandler.log("playlistFixedDB onUpgrade oldVer:"+oldVersion+", newVersion:"+newVersion);
        CustomExceptionHandler.log("onUpgradeFixed done");
    }


    public void savePlayListFixed(final MTPlayList playListFixedNext) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updatePlayListFixedInBackground(playListFixedNext);
            }
        }).start();

    }

    private void updatePlayListFixedInBackground(MTPlayList playListFixedNext) {
        synchronized (Dao.getInstance(context)) {
            int typePlayList = playListFixedNext.getTypePlayList();
            CustomExceptionHandler.log("write playListFixed = " + playListFixedNext);

            try {
                SQLiteDatabase db = this.getWritableDatabase();
                db.execSQL(PlayListFixedDBHelper.DELETE_TABLE);

                int numord = 1;
                for (MTPlayListRec plr : playListFixedNext.getPlaylist()) {
                    ContentValues cv = new ContentValues();
                    cv.put(ID, plr.getId());
                    cv.put(VPID, plr.getVpid());
                    cv.put(FILENAME, plr.getFilename());
                    cv.put(SIZE, plr.getSize());
                    cv.put(PERIODICITY, plr.getPeriodicity());
                    cv.put(DURATION, plr.getDuration());
                    cv.put(TYPE, plr.getType());
                    cv.put(DTFROM, plr.getDate().getFrom());
                    cv.put(DTTO, plr.getDate().getTo());
                    cv.put(MD5, plr.getMd5());

                    db.insert(TABLE_NAME, null, cv);
                    numord++;
                }
            } catch (Exception e) {
                CustomExceptionHandler.logException("write playListFixed error ", e);
                e.printStackTrace();
            }
            CustomExceptionHandler.log("write playListFixedNext end " + playListFixedNext.getPlaylist().size());
        }
    }

    public MTPlayList readPlayListFixed(int typePlayList) {
        // попробовать прочитать локально сохраненный плейлист.
        CustomExceptionHandler.log("fixed start read "+ typePlayList);
        MTPlayList list = new MTPlayList();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("select * from "+ TABLE_NAME , new String[] { });
        if (cur != null ) {
            CustomExceptionHandler.log("fixed cur.getCount() "+ cur.getCount());
            if (cur.moveToFirst()) {
                do {
                    MTPlayListRec rec = new MTPlayListRec();
                    rec.setId(cur.getLong(cur.getColumnIndex(ID)));
                    rec.setVpid(cur.getLong(cur.getColumnIndex(VPID)));
                    rec.setFilename(cur.getString(cur.getColumnIndex(FILENAME)));
                    rec.setSize(cur.getLong(cur.getColumnIndex(SIZE)));
                    rec.setDuration(cur.getLong(cur.getColumnIndex(DURATION)));
                    rec.setType(cur.getString(cur.getColumnIndex(TYPE)));
                    rec.setDate(new MTDateRec(cur.getString(cur.getColumnIndex(DTFROM)), cur.getString(cur.getColumnIndex(DTTO))));
                    rec.setMd5(cur.getString(cur.getColumnIndex(MD5)));
                    rec.setPeriodicity(cur.getLong(cur.getColumnIndex(PERIODICITY)));
                    list.getPlaylist().add(rec);
                }  while (cur.moveToNext()) ;
            }
            cur.close();
        }
        CustomExceptionHandler.log("fixed end read "+ list);
        return list;
    }
}
