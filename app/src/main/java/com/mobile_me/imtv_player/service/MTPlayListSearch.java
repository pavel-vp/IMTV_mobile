package com.mobile_me.imtv_player.service;

import android.support.annotation.NonNull;
import com.mobile_me.imtv_player.model.MTGpsPoint;
import com.mobile_me.imtv_player.model.MTPlayList;
import com.mobile_me.imtv_player.model.MTPlayListRec;
import com.mobile_me.imtv_player.util.IMTLogger;
import com.mobile_me.imtv_player.util.MTGpsUtils;

import java.util.*;

/**
 * Created by pasha on 07.12.17.
 */
public class MTPlayListSearch {

    private MTPlayList playList;

    public Set<Long> getGeoVideoToPlay() {
        return geoVideoToPlay;
    }

    private Set<Long> geoVideoToPlay = new HashSet<>();

    public void setMTPlayList(MTPlayList playList){
        this.playList = playList;
    }

    public void checkAndMapGeoVideo(MTGpsPoint lastGpsCoordinate, IMTLogger logger) {
        if (playList == null) return; // Еще пустой плейлист
        boolean isAdded = false;
        // Проверить плейлист на георолик в этой точке
        for (MTPlayListRec plr : playList.getPlaylist()) {
            if (MTPlayListRec.TYPE_GEO.equals(plr.getType())) {
                if (MTGpsUtils.isPointInPolygon(lastGpsCoordinate, plr.getPolygonMarks())) {
                    this.geoVideoToPlay.add(plr.getId());
                    isAdded = true;
                }
            }
        }
        if (isAdded) {
            logger.log("checkAndMapGeoVideo lastGpsCoordinate=" + lastGpsCoordinate + " added to set = " + this.geoVideoToPlay);
        }
    }

    static class MTCommercialInfo {
        MTPlayListRec mtPlayListRec;
        double planQty = 0;
        double factQty = 0;
        double priority = 0;

        @Override
        public String toString() {
            return "MTCommercialInfo{" +
                    " planQty=" + planQty +
                    ", factQty=" + factQty +
                    ", priority=" + priority +
                    ", mtPlayListRec=" + mtPlayListRec +
                    '}';
        }
    }

    // Метод возвращает очередь по приоритетам, в которой коммерческие ролики отсортированы по
    // значению (план-факт) воспроизведения за это число минут. С наибольшим приоритетом - вверху
    private PriorityQueue<MTCommercialInfo> getCommercialPQ(int lastMinutes, List<MTPlayListRec> statList) {
        PriorityQueue<MTCommercialInfo> q = new PriorityQueue<>(100, new Comparator<MTCommercialInfo>() {
            @Override
            public int compare(MTCommercialInfo lhs, MTCommercialInfo rhs) {
                if (lhs.priority > rhs.priority) return -1;
                if (lhs.priority < rhs.priority) return 1;
                return 0;
            }
        });
        // соберем сперва в список все коммерческие ролики
        Map<Long, MTCommercialInfo> commMap = new HashMap<>();
        for (MTPlayListRec rc : playList.getPlaylist()) {
            if (rc.getState() == MTPlayListRec.STATE_UPTODATE &&
                    MTPlayListRec.TYPE_COMMERCIAL.equals(rc.getType())) {
                MTCommercialInfo ci = new MTCommercialInfo();
                ci.mtPlayListRec = rc;
                ci.planQty = (double)lastMinutes / (double)rc.getPeriodicity();
                ci.priority = ci.planQty;
                commMap.put(rc.getId(), ci);
            }
        }
        // пройтись по факту за последние минуты и расчитаем приоритет
        for (MTPlayListRec r : statList) {
            MTPlayListRec rc = playList.searchById(r.getId());
            if (rc != null &&
                    rc.getState() == MTPlayListRec.STATE_UPTODATE &&
                    MTPlayListRec.TYPE_COMMERCIAL.equals(r.getType())) {
                MTCommercialInfo ci = commMap.get(r.getId());
                if (ci == null) {
                    ci = new MTCommercialInfo();
                    ci.mtPlayListRec = rc;
                    commMap.put(r.getId(), ci);
                }
                ci.factQty = ci.factQty + 1;
                ci.priority = ci.planQty - ci.factQty;
            }
        }
        // из мапы перенесем в очередь
        for (MTCommercialInfo ci : commMap.values()) {
            q.add(ci);
        }
        return q;
    }

    static class MTFreeInfo {
        MTPlayListRec mtPlayListRec;
        double priority = 0;

        @Override
        public String toString() {
            return "MTFreeInfo{" +
                    "priority=" + priority +
                    ", mtPlayListRec=" + mtPlayListRec +
                    '}';
        }
    }
    // Метод возвращает очередь по приоритетам, в которой некоммерческие ролики отсортированы по
    // значению (факт) воспроизведения за это число минут. С наименьшим количеством воспроизведения - вверху
    private PriorityQueue<MTFreeInfo> getFreePQ(int lastMinutes, List<MTPlayListRec> statList) {
        PriorityQueue<MTFreeInfo> q = new PriorityQueue<>(100, new Comparator<MTFreeInfo>() {
            @Override
            public int compare(MTFreeInfo lhs, MTFreeInfo rhs) {
                if (lhs.priority < rhs.priority) return -1;
                if (lhs.priority > rhs.priority) return 1;
                return 0;
            }
        });
        // соберем сперва в список все некоммерческие ролики
        Map<Long, MTFreeInfo> freeMap = new HashMap<>();
        for (MTPlayListRec rc : playList.getPlaylist()) {
            if (rc.getState() == MTPlayListRec.STATE_UPTODATE &&
                    MTPlayListRec.TYPE_FREE.equals(rc.getType())) {
                MTFreeInfo ci = new MTFreeInfo();
                ci.mtPlayListRec = rc;
                freeMap.put(rc.getId(), ci);
            }
        }
        // пройтись по факту за последние минуты и расчитаем приоритет
        for (MTPlayListRec r : statList) {
            MTPlayListRec rc = playList.searchById(r.getId());
            if (rc != null &&
                    rc.getState() == MTPlayListRec.STATE_UPTODATE &&
                    MTPlayListRec.TYPE_FREE.equals(r.getType())) {
                MTFreeInfo ci = freeMap.get(r.getId());
                if (ci == null) {
                    ci = new MTFreeInfo();
                    ci.mtPlayListRec = rc;
                    freeMap.put(r.getId(), ci);
                }
                ci.priority = ci.priority + 1;
            }
        }
        // из мапы перенесем в очередь
        for (MTFreeInfo ci : freeMap.values()) {
            q.add(ci);
        }
        return q;
    }

    static class MTGeoInfo {
        MTPlayListRec mtPlayListRec;
        double planQty = 0;
        double factQty = 0;
        double priority = 0;

        @Override
        public String toString() {
            return "MTGeoInfo{" +
                    " planQty=" + planQty +
                    ", factQty=" + factQty +
                    ", priority=" + priority +
                    ", mtPlayListRec=" + mtPlayListRec +
                    '}';
        }
    }
    // Метод возвращает очередь по приоритетам, в котором георолики которые нужно воспроизвести отсортированы
    // значению (план-факт) воспроизведения за это число минут. С наибольшим приоритетом - вверху
    private PriorityQueue<MTGeoInfo> getGeoPQ(int lastMinutes, List<MTPlayListRec> statList) {
        PriorityQueue<MTGeoInfo> q = new PriorityQueue<>(100, new Comparator<MTGeoInfo>() {
            @Override
            public int compare(MTGeoInfo lhs, MTGeoInfo rhs) {
                if (lhs.priority > rhs.priority) return -1;
                if (lhs.priority < rhs.priority) return 1;
                return 0;
            }
        });
        // Пройдемся по всему набору ИД-шников геороликов, которые нужно воспроизвести
        if (this.geoVideoToPlay != null && geoVideoToPlay.size() > 0) {
            Map<Long, MTGeoInfo> geoMap = new HashMap<>();
            for (Long geoId : this.geoVideoToPlay) {
                // Найдем его в плейлисте
                MTPlayListRec rc = playList.searchById(geoId);
                if (rc != null &&
                            rc.getState() == MTPlayListRec.STATE_UPTODATE &&
                            MTPlayListRec.TYPE_GEO.equals(rc.getType()) &&
                            rc.getId().equals(geoId)) {

                    MTGeoInfo ci = new MTGeoInfo();
                    ci.mtPlayListRec = rc;
                    ci.planQty = rc.getMax_count();
                    ci.priority = ci.planQty;
                    geoMap.put(rc.getId(), ci);
                }
            }
            // пройтись по факту за последние минуты и расчитаем приоритет
            for (MTPlayListRec r : statList) {
                MTPlayListRec rc = playList.searchById(r.getId());
                if (rc != null &&
                        geoMap.get(r.getId()) != null &&
                        rc.getState() == MTPlayListRec.STATE_UPTODATE ) {
                    MTGeoInfo ci = geoMap.get(r.getId());
                    if (ci == null) {
                        ci = new MTGeoInfo();
                        ci.mtPlayListRec = rc;
                        geoMap.put(r.getId(), ci);
                    }
                    ci.factQty = ci.factQty + 1;
                    ci.priority = ci.planQty - ci.factQty;
                }
            }
            // из мапы перенесем в очередь
            q.addAll(geoMap.values());
        }
        return q;
    }


////////////////////////////////
    public MTPlayListRec getNextVideoFile(@NonNull List<MTPlayListRec> statList, int lastMinutes, IMTLogger logger, MTGpsPoint lastGpsPoint) {
        logger.log("start calc next file");
        MTPlayListRec lastRec = null;
        if (statList != null && statList.size() > 0) {
            lastRec = statList.get(0);
        }
        logger.log("lastRec="+lastRec + ", lastGpsPoint="+lastGpsPoint);


        // TODO: логировать данные о сохраненных проигрываниях (на основании чего считаем), и выбранного факта, чтобы потом на основании лога можно было понять почему проигралась эта запись
        logger.log("statList.size="+statList.size());

        try {
            // GPS
            if (lastGpsPoint != null && this.geoVideoToPlay != null && this.geoVideoToPlay.size() > 0) {
                // получим очередь по приоритетам с воспроизведением георолика в этой точке
                PriorityQueue<MTGeoInfo> q = getGeoPQ(lastMinutes, statList);
                logger.log("geo queue="+q.size());
                // пройдемся по очереди в поиске первого неотрицательного, не такого же как последний проигранный
                MTGeoInfo resGeo = q.poll();
                while (resGeo != null && resGeo.priority > 0) {
                    // Удалим его из мапы в любом случае
                    this.geoVideoToPlay.remove(resGeo.mtPlayListRec.getId());
                    if ((lastRec == null || lastRec.getId().longValue() != resGeo.mtPlayListRec.getId().longValue())) {
                        logger.log("resGeo=" + resGeo);
                        // проигрываем его
                        return resGeo.mtPlayListRec;
                    }
                    resGeo = q.poll();
                }

            }
            // КОММЕРЧЕСКОЕ
            // получим очередь по приоритетам с воспроизведением коммерческого.
            PriorityQueue<MTCommercialInfo> q = getCommercialPQ(lastMinutes, statList);
            // выведем очередь в лог
            logger.log("commercial queue="+q.size());
    /*for (MTCommercialInfo c : q) {
        CustomExceptionHandler.log("c=" + c);
    }*/
            // пройдемся по очереди в поиске первого неотрицательного, не такого же как последний проигранный
            MTCommercialInfo resComm = q.poll();
            while (resComm != null && resComm.priority > 0) {
                if ((lastRec == null || lastRec.getId().longValue() != resComm.mtPlayListRec.getId().longValue())) {
                    logger.log("resComm=" + resComm);
                    // проигрываем его
                    return resComm.mtPlayListRec;
                }
                resComm = q.poll();
            }

            // Нет ничего что проигрывать в коммерческой очереди (либо нет коммерческой вообще)
            // НЕКОММЕРЧЕСКОЕ
            // получим очередь по приоритетам для некоммерческого
            PriorityQueue<MTFreeInfo> qf = getFreePQ(lastMinutes, statList);
            // выведем очередь в лог
            logger.log("free queue="+qf.size());
    /*for (MTFreeInfo c : qf) {
        CustomExceptionHandler.log("c=" + c);
    }*/
            MTFreeInfo fi = qf.poll();
            // также пройдемся по очереди, выбирая первый не такой же как последний проигранный
            while (fi != null) {
                if ((lastRec == null || lastRec.getId().longValue() != fi.mtPlayListRec.getId().longValue())) {
                    logger.log("fi=" + fi);
                    // проигрываем его
                    return fi.mtPlayListRec;
                }
                fi = qf.poll();
            }

            logger.log("nothing to play");
            // Нет ничего что надо проигрывать по плану и в некоммерческом.
            // Если вообще есть что-то в коммерческой очереди
            q = getCommercialPQ(lastMinutes, statList);
            // пройдемся по очереди в поиска первого не такого же как последний
            resComm = q.poll();
            MTCommercialInfo firstComm = resComm;
            while (resComm != null) {
                if ((lastRec == null || lastRec.getId().longValue() != resComm.mtPlayListRec.getId().longValue())) {
                    logger.log("resComm=" + resComm);
                    // проигрываем его
                    return resComm.mtPlayListRec;
                }
                resComm = q.poll();
            }
            logger.log("may be first time play");
            // если дошли сюда - то просто возьмем первый из коммерческой очереди без всяких условий
            if (firstComm != null) {
                logger.log("firstComm=" + firstComm);
                return firstComm.mtPlayListRec;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.log("exception e="+e.getMessage());

            PriorityQueue<MTCommercialInfo> q = getCommercialPQ(lastMinutes, statList);
            MTCommercialInfo resComm = q.poll();
            return resComm.mtPlayListRec;

        }

        logger.log("nothing to play at all");

        return null; // ничего нет !
    }

}
