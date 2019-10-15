package com.mobile_me.imtv_player.service;

import android.content.Context;

import com.mobile_me.imtv_player.dao.Dao;
import com.mobile_me.imtv_player.model.MTGpsPoint;
import com.mobile_me.imtv_player.model.MTPlayList;
import com.mobile_me.imtv_player.model.MTPlayListRec;
import com.mobile_me.imtv_player.util.CustomExceptionHandler;
import com.mobile_me.imtv_player.util.IMTLogger;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by pasha on 8/27/16.
 */
public class MTPlayListManager implements IMTLogger {

    private final MTPlayList playList = new MTPlayList();
    private final MTPlayList playListFixed = new MTPlayList();
    private Context ctx;
    private Dao dao;
    private MTPlayListSearch playListSearch = new MTPlayListSearch();
    private AtomicInteger position = new AtomicInteger(-1);

    public MTPlayListManager(Context ctx, int typePlayList) {
        this.ctx = ctx;
        this.dao = Dao.getInstance(ctx);
        this.playList.setTypePlayList(typePlayList);
        CustomExceptionHandler.log("MTPlayListManager created for type="+typePlayList);
    }

    public MTPlayList getPlayList() {
        return playList;
    }

    public MTPlayListRec getRandomFile() {
        MTPlayListRec found = null;
        synchronized (playList) {
            int l =(int) (Math.random() * 10);
            for (int i = 0; i < l || found == null; i++) {
                int idx = i >= playList.getPlaylist().size() ? 0 : i;

                if (playList.getPlaylist().get(idx).getState() == MTPlayListRec.STATE_UPTODATE) {
                    found = playList.getPlaylist().get(idx);
                }
            }
        }
        CustomExceptionHandler.log("getRandomFile res="+found);
        return found;
    }

    /**
     * Верхняя процедура возвращает файл для проигрывания
     * @param forcedPlay
     * @return
     */
    public MTPlayListRec getNextVideoFileForPlay(boolean forcedPlay) {
        MTPlayListRec found = null;
        synchronized (playList) {
            CustomExceptionHandler.log("getNextVideoFileForPlay start for playList="+playList+", forcedPLay = "+forcedPlay);
            // ЭТАП 1. поиск файла для проигрывания в этом плейлисте
            found = getNextVideoFileForPlayInternal();
            // ЭТАП 2. не нашли - ищем любой форсированно
            if (found == null && forcedPlay) {
                // если не нашли ни одного для проигрывания - сбросить у всех статус и взять первый загруженный
                // это делаем, чтобы проигрывание не останавливалось - проигрываем все по кругу
                // но только если принудительный флаг установлен
                for (MTPlayListRec f : playList.getPlaylist()) {
                    f.setPlayed(MTPlayListRec.PLAYED_NO);
                    CustomExceptionHandler.log("getNextVideoFileForPlay setPlayed NO for f="+f);
                    if (f.getState() == MTPlayListRec.STATE_UPTODATE) {
                        if (found == null) {
                            found = f;
                        }
                    }
                }
            }
            if (found != null) {
                // выставить у найденного флаг проигрывания
                found.setPlayed(MTPlayListRec.PLAYED_YES);
                CustomExceptionHandler.log("getNextVideoFileForPlay setPlayed YES for f="+found);
            }
            Dao.getInstance(ctx).getPlayListDBHelper().updatePlayList(this.playList);
        }
        CustomExceptionHandler.log("getNextVideoFileForPlay res2="+found);
        return found;
    }


/*    static class MTStatInfo {
        int durationFreeSec = 0; // число секунд воспроиведения НЕКОММ видео
        Map<Long, Integer> cntMap = new HashMap<>();
        Map<Long, Long> durationMap = new HashMap<>();

    }
    private MTStatInfo getStatInfo(int lastMinutes) {
        MTStatInfo res = new MTStatInfo();
        // прочитаем данные статистики за последние 30 минут
        for (MTPlayListRec r : dao.getmStatisticDBHelper().readStatOnLastNMins(lastMinutes)) {
            if (MTPlayListRec.TYPE_FREE.equals(r.getType())) {
                res.durationFreeSec += r.getDuration();
            }
            Integer cnt =res.cntMap.get(r.getId());
            res.cntMap.put(r.getId(), cnt == null ? 1 : cnt + 1);
            Long duration =res.durationMap.get(r.getId());
            res.durationMap.put(r.getId(), duration == null ? 1 : duration + r.getDuration());

        }
        return res;
    }
*/


    /**
     * Функция реализует основной алгоритм поиска файла для проигрывания
     * @return
     */
    private MTPlayListRec getNextVideoFileForPlayInternal() {
        int lastMinutes = 30;
        this.playListSearch.setMTPlayList(this.playList);
        this.playListSearch.setMTPlayListFixed(this.playListFixed);
        return this.playListSearch.getNextVideoFileFixed(this, dao.getLastGpsCoordinate(), position);
    }

    private MTPlayListRec getNextVideoFileForPlayInternal_OLD() {
        int lastMinutes = 30; // FIXME: в настройки
        // на входе - статистика за последние 30 мин, текущий плейлист с актуальными файлами для проигрывания
        List<MTPlayListRec> statList = dao.getmStatisticDBHelper().readStatOnLastNMins(lastMinutes);
        this.playListSearch.setMTPlayList(this.playList);
        return this.playListSearch.getNextVideoFile(statList, lastMinutes, this, dao.getLastGpsCoordinate());
    }

    public void setFileStateFlag(MTPlayListRec rec, int flag) {
        synchronized (playList) {
            rec.setState(flag);
            Dao.getInstance(ctx).getPlayListDBHelper().updatePlayList(this.playList);
        }
    }

    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                inFiles.add(file);
            }
        }
        return inFiles;
    }

    public void mergeAndSavePlayList(MTPlayList playListNew) {
        synchronized (playList) {
            CustomExceptionHandler.log("mergeAndSavePlayList playList="+playList + " with new ="+playListNew);
                if (playListNew == null)
                    return;

                    // За основу берем новый плейлист, подтягивая данные по статусам из старого (по ИД)
                    for (MTPlayListRec newR : playListNew.getPlaylist()) {
                        // попробовать найти этот файл в текущем плейлисте
                        MTPlayListRec found = this.playList.searchById(newR.getId());
                        if (found != null) {
                            newR.setState(found.getState());
                            newR.setPlayed(found.getPlayed());
                        }
                    }
                    // а также удалить файлы, которых нет уже в новом
                    // файлы взять из директории
                    //File dirVideos = new File(dao.getDownVideoFolder());
                    List<File> files = getListFiles(new File(dao.getVideoPath()));
                    for (File f : files) {
                        // если в новом его нет
                        MTPlayListRec found = playListNew.searchByFileName(f.getAbsolutePath(), dao.getVideoPath());
                        if (found == null) {
                            // удалим саму запись, сам видеофайл тоже
                            CustomExceptionHandler.log("delete old video file "+f.getName());
                            f.delete();
                        }
                    }
                    this.playList.getPlaylist().clear();
                    this.playList.getPlaylist().addAll(playListNew.getPlaylist());
                dao.getPlayListDBHelper().updatePlayList(this.playList);
        }
    }

    public void savePlayListFixed(MTPlayList playListFixedNew) {
        synchronized (playList) {
            CustomExceptionHandler.log("savePlayListFixed playListFixed=" + playListFixed + " with new =" + playListFixedNew);
            if (playListFixedNew == null)
                return;
            this.playListFixed.getPlaylist().clear();
            this.playListFixed.getPlaylist().addAll(playListFixedNew.getPlaylist());
            dao.getPlayListFixedDBHelper().savePlayListFixed(playListFixedNew);
        }
    }


    public MTPlayListRec getNextFileToLoad() {
        CustomExceptionHandler.log("getNextFileToLoad start");
        MTPlayListRec fileToLoad = null;
        synchronized (playList) {
                Map<Long, MTPlayListRec> filesToLoad  = new HashMap<>();
                // по списку файлов пройтись и сравнить их с текущими. Если различаюься - поставить флаг необходимости скачивания
                for (MTPlayListRec f : playList.getPlaylist()) {
                    // прочитать локальные данные файла
                    File finfo = new File(dao.getVideoPath(), f.getFilename());
                    //log("loadVideoFileFromPlayList check file info for = "+finfo.getAbsolutePath());
                    if (finfo.exists() && finfo.length() == f.getSize()) { // TODO: потом сделать по MD5
                        f.setState(MTPlayListRec.STATE_UPTODATE);
                       // log("loadVideoFileFromPlayList file state uptodate");
                    } else {
                        f.setState(MTPlayListRec.STATE_NEED_LOAD);
                        filesToLoad.put(f.getId(),f);
                        //log("loadVideoFileFromPlayList file state need load");
                    }
                }
                // Получим массив для загрузки. Из него выберем файл для загрузки
                CustomExceptionHandler.log("getNextFileToLoad filesToLoad="+filesToLoad);
                if (filesToLoad.size() > 0) {
                    for (MTPlayListRec rec : playListFixed.getPlaylist()) {
                        if (filesToLoad.containsKey(rec.getId())) {
                            fileToLoad = filesToLoad.get(rec.getId());
                            break;
                        }
                    }
                    if (fileToLoad != null) {
                        // запустить реальное скачивание первого файла
                        fileToLoad.setState(MTPlayListRec.STATE_LOADING);
                    }
                }
        }
        CustomExceptionHandler.log("getNextFileToLoad file="+fileToLoad);
        return fileToLoad;
    }

    // Метод проверяет файлы на устройстве, из плейлиста.
    // если файла со статусом "закачан" реально нет по этому пути, то выставить у него статус "незакачан"
    public boolean checkPlayListFilesOnDisk(MTPlayList playListTest) {
        boolean result = false;
        CustomExceptionHandler.log("checkPlayListFilesOnDisk playListTest="+playListTest);
            File path = new File(dao.getVideoPath());
            for (MTPlayListRec plr : playListTest.getPlaylist()) {
                File f = new File(path, plr.getFilename());
                if (plr.getState() == MTPlayListRec.STATE_UPTODATE) {
                    if (!f.exists()) {
                        plr.setState(MTPlayListRec.STATE_NEED_LOAD);
                    } else {
                        result = true; // есть хоть один
                    }
                }
            }
        CustomExceptionHandler.log("checkPlayListFilesOnDisk res="+result);
        return result;
    }

    @Override
    public void log(String msg) {
        CustomExceptionHandler.log(msg);
    }

    public void checkAndMapGeoVideo(MTGpsPoint lastGpsCoordinate) {
        playListSearch.checkAndMapGeoVideo(lastGpsCoordinate, this);
    }

    public MTPlayList getPlayListFixed() {
        return playListFixed;
    }


}
