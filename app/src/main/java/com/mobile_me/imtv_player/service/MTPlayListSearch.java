package com.mobile_me.imtv_player.service;

import android.support.annotation.NonNull;
import com.mobile_me.imtv_player.model.MTGpsPoint;
import com.mobile_me.imtv_player.model.MTPlayList;
import com.mobile_me.imtv_player.model.MTPlayListRec;
import com.mobile_me.imtv_player.util.IMTLogger;
import com.mobile_me.imtv_player.util.MTGpsUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by pasha on 07.12.17.
 */
public class MTPlayListSearch {

    private MTPlayList playListFixed;

    public Set<Long> getGeoVideoToPlay() {
        return geoVideoToPlay;
    }

    private Set<Long> geoVideoToPlay = new HashSet<>();

    public void checkAndMapGeoVideo(MTGpsPoint lastGpsCoordinate, IMTLogger logger) {
        if (playListFixed == null) return; // Еще пустой плейлист
        boolean isAdded = false;
        // Проверить плейлист на георолик в этой точке
        for (MTPlayListRec plr : playListFixed.getPlaylist()) {
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

    public void setMTPlayListFixed(MTPlayList playListFixed) {
        this.playListFixed = playListFixed;
    }



////////////////////////////////
    public MTPlayListRec getNextVideoFileForPlayFixed(IMTLogger logger, MTGpsPoint lastGpsCoordinate, AtomicInteger position) {
        logger.log("start calc next file FIXED");
        while (this.playListFixed == null || this.playListFixed.getPlaylist() == null || this.playListFixed.getPlaylist().size() == 0) {
            Thread.yield();
        }

        // TODO: добавить gps

        MTPlayListRec lastRec = null;
        for (int i = 0; i < this.playListFixed.getPlaylist().size(); i++) {
            lastRec = this.playListFixed.getPlaylist().get(i);
            logger.log("try FIXED file "+lastRec);
            if (lastRec.getPlayed() ==  MTPlayListRec.PLAYED_YES || lastRec.getState() != MTPlayListRec.STATE_UPTODATE ) {
                lastRec = null;
            } else {
                break;
            }
        }

        logger.log("found FIXED file "+lastRec);

        return lastRec;

    }



}
