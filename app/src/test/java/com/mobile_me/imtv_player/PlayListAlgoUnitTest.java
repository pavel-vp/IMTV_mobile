package com.mobile_me.imtv_player;

import com.mobile_me.imtv_player.model.MTPlayList;
import com.mobile_me.imtv_player.model.MTPlayListRec;
import com.mobile_me.imtv_player.service.MTPlayListSearch;
import com.mobile_me.imtv_player.util.IMTLogger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class PlayListAlgoUnitTest {
    private MTPlayListSearch playListSearch = new MTPlayListSearch();
    private IMTLogger logger = new IMTLogger() {
        @Override
        public void log(String msg) { }
    };

    @Test
    public void check_null_playlist() throws Exception {
        // init
        MTPlayList playList = new MTPlayList();
        playListSearch.setMTPlayList(playList);
        List<MTPlayListRec> stat = new ArrayList<>();
        // do
        MTPlayListRec res = playListSearch.getNextVideoFile(stat, 30, logger);
        // assert
        assertEquals(res, null);
    }

    //////////////////
    private MTPlayList getUploadedPlayList() {
        MTPlayList playList = new MTPlayList();
        playList.getPlaylist().add(new MTPlayListRec(1L, 1L, "one", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 10L, null, 0d, 0L, 0L, MTPlayListRec.STATE_UPTODATE, MTPlayListRec.PLAYED_NO));
        playList.getPlaylist().add(new MTPlayListRec(2L, 2L, "two", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 10L, null, 0d, 0L, 0L, MTPlayListRec.STATE_UPTODATE, MTPlayListRec.PLAYED_NO));
        playList.getPlaylist().add(new MTPlayListRec(3L, 3L, "three", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 10L, null, 0d, 0L, 0L, MTPlayListRec.STATE_UPTODATE, MTPlayListRec.PLAYED_NO));
        playList.getPlaylist().add(new MTPlayListRec(4L, 4L, "four", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 10L, null, 0d, 0L, 0L, MTPlayListRec.STATE_UPTODATE, MTPlayListRec.PLAYED_NO));
        return playList;
    }

    private List<MTPlayListRec> getStat1() {
        List<MTPlayListRec> stat = new ArrayList<>();
        stat.add(new MTPlayListRec(1L, 1L, "one", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 0L, null, 0d, 0L, 0L, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO));
        stat.add(new MTPlayListRec(2L, 2L, "two", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 0L, null, 0d, 0L, 0L, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO));
        stat.add(new MTPlayListRec(3L, 3L, "three", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 0L, null, 0d, 0L, 0L, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO));
        stat.add(new MTPlayListRec(4L, 4L, "four", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 0L, null, 0d, 0L, 0L, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO));
        stat.add(new MTPlayListRec(5L, 5L, "five", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 0L, null, 0d, 0L, 0L, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO));
        stat.add(new MTPlayListRec(6L, 6L, "six", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 0L, null, 0d, 0L, 0L, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO));
        return stat;
    }

    @Test
    public void check_playlist_equals() throws Exception {
        // init
        MTPlayList playList = getUploadedPlayList();
        playListSearch.setMTPlayList(playList);
        List<MTPlayListRec> stat = getStat1();
        // do
        MTPlayListRec res = playListSearch.getNextVideoFile(stat, 30, logger);
        // assert
        assertEquals(playList.getPlaylist().get(playList.getPlaylist().size()-1), res);
    }

    @Test
    public void check_playlist_1_high_periodicity() throws Exception {
        // init
        MTPlayList playList = getUploadedPlayList();
        playList.getPlaylist().get(1).setPeriodicity(5L);
        playListSearch.setMTPlayList(playList);
        List<MTPlayListRec> stat = getStat1();
        // do
        MTPlayListRec res = playListSearch.getNextVideoFile(stat, 30, logger);
        // assert
        assertEquals(playList.getPlaylist().get(1), res);
    }


}