package com.mobile_me.imtv_player;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobile_me.imtv_player.model.MTGpsPoint;
import com.mobile_me.imtv_player.model.MTPlayList;
import com.mobile_me.imtv_player.model.MTPlayListRec;
import com.mobile_me.imtv_player.util.MTGpsUtils;
import com.mobile_me.imtv_player.service.MTPlayListSearch;
import com.mobile_me.imtv_player.util.IMTLogger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class PlayListAlgoUnitTest {
    private MTPlayListSearch playListSearch = new MTPlayListSearch();
    private IMTLogger logger = new IMTLogger() {
        @Override
        public void log(String msg) {
            System.out.println(msg);
        }
    };

    @Test
    public void check_null_playlist() throws Exception {
        // init
        MTPlayList playList = new MTPlayList();
        playListSearch.setMTPlayList(playList);
        List<MTPlayListRec> stat = new ArrayList<>();
        // do
        MTPlayListRec res = playListSearch.getNextVideoFile(stat, 30, logger, null);
        // assert
        assertEquals(res, null);
    }

    //////////////////
    private MTPlayList getUploadedPlayList() {
        MTPlayList playList = new MTPlayList();
        playList.getPlaylist().add(new MTPlayListRec(1L, 1L, 1L, "one", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 10L, null, MTPlayListRec.STATE_UPTODATE, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        playList.getPlaylist().add(new MTPlayListRec(2L, 2L, 1L, "two", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 10L, null, MTPlayListRec.STATE_UPTODATE, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        playList.getPlaylist().add(new MTPlayListRec(3L, 3L, 1L, "three", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 10L, null,MTPlayListRec.STATE_UPTODATE, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        playList.getPlaylist().add(new MTPlayListRec(4L, 4L,  1L,"four", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 10L, null, MTPlayListRec.STATE_UPTODATE, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        return playList;
    }

    private List<MTPlayListRec> getStat1() {
        List<MTPlayListRec> stat = new ArrayList<>();
        stat.add(new MTPlayListRec(1L, 1L,  1L,"one", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 0L, null, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        stat.add(new MTPlayListRec(2L, 2L,  1L,"two", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 0L, null, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        stat.add(new MTPlayListRec(3L, 3L,  1L,"three", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 0L, null, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        stat.add(new MTPlayListRec(4L, 4L,  1L,"four", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 0L, null, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        stat.add(new MTPlayListRec(5L, 5L,  1L,"five", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 0L, null, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        stat.add(new MTPlayListRec(6L, 6L,  1L,"six", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 0L, null, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        return stat;
    }

    @Test
    public void check_playlist_equals() throws Exception {
        // init
        MTPlayList playList = getUploadedPlayList();
        playListSearch.setMTPlayList(playList);
        List<MTPlayListRec> stat = getStat1();
        // do
        MTPlayListRec res = playListSearch.getNextVideoFile(stat, 30, logger, null);
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
        MTPlayListRec res = playListSearch.getNextVideoFile(stat, 30, logger, null);
        // assert
        assertEquals(playList.getPlaylist().get(1), res);
    }

    ////////////////////////////////
    private MTGpsPoint[] getPolygon_1() { // izhevsk
        MTGpsPoint[] pointList = new MTGpsPoint[4];
        pointList[0] = new MTGpsPoint(56.885680, 53.166901);
        pointList[1] = new MTGpsPoint(56.890323, 53.323415);
        pointList[2] = new MTGpsPoint(56.830277, 53.325305);
        pointList[3] = new MTGpsPoint(56.829817, 53.163957);
        return pointList;
    }

    @Test
    public void test_point_inside() {
        boolean result = MTGpsUtils.isPointInPolygon(new MTGpsPoint(56.856837, 53.210516), getPolygon_1());
        assertTrue(result);
    }

    @Test
    public void test_point_outside() {
        boolean result = MTGpsUtils.isPointInPolygon(new MTGpsPoint(56.821953, 53.441226), getPolygon_1());
        assertFalse(result);
    }

    private MTGpsPoint[] getPolygon_2() { // home
        MTGpsPoint[] pointList = new MTGpsPoint[4];
        pointList[0] = new MTGpsPoint(56.875508, 53.201435);
        pointList[1] = new MTGpsPoint(56.875191, 53.202870);
        pointList[2] = new MTGpsPoint(56.874485, 53.202933);
        pointList[3] = new MTGpsPoint(56.874467, 53.201024);
        return pointList;
    }
    private MTGpsPoint[] getPolygon_3() { // city TRC
        MTGpsPoint[] pointList = new MTGpsPoint[4];
        pointList[0] = new MTGpsPoint(56.876309, 53.210116);
        pointList[1] = new MTGpsPoint(56.876298, 53.211716);
        pointList[2] = new MTGpsPoint(56.875558, 53.211962);
        pointList[3] = new MTGpsPoint(56.875553, 53.210084);
        return pointList;
    }

    private MTGpsPoint[] getPolygon_4() { // ?
        MTGpsPoint[] pointList = new MTGpsPoint[4];
        pointList[0] = new MTGpsPoint(56.82921718103309, 53.148169306047);
        pointList[1] = new MTGpsPoint(56.828230984975804, 53.14784744096539);
        pointList[2] = new MTGpsPoint(56.82821924439014, 53.150529649980285);
        pointList[3] = new MTGpsPoint(56.828935413380854, 53.15093734575055);
        return pointList;
    }

    @Test
    public void check_playlist_gps_1_point_away() throws Exception {
        // init
        MTPlayList playList = getUploadedPlayList();
        playList.getPlaylist().get(0).setType(MTPlayListRec.TYPE_GEO);
        playList.getPlaylist().get(0).setPolygonMarks(getPolygon_2());
        playList.getPlaylist().get(1).setType(MTPlayListRec.TYPE_GEO);
        playList.getPlaylist().get(1).setPolygonMarks(getPolygon_3());

        playListSearch.setMTPlayList(playList);
        List<MTPlayListRec> stat = getStat1();
        // do
        MTPlayListRec res = playListSearch.getNextVideoFile(stat, 30, logger, new MTGpsPoint(57.876309, 54.210116));
        // assert
        assertEquals(playList.getPlaylist().get(2), res);
    }

    private MTPlayList getUploadedPlayList_Geo() {
        MTPlayList playList = new MTPlayList();
        playList.getPlaylist().add(new MTPlayListRec(1L, 1L,  1L,"one", 10L, 0L, MTPlayListRec.TYPE_GEO, null, null, 10L, getPolygon_2(), MTPlayListRec.STATE_UPTODATE, MTPlayListRec.PLAYED_NO, 5L, 0L, 0L));
        playList.getPlaylist().add(new MTPlayListRec(2L, 2L,  1L,"two", 10L, 0L, MTPlayListRec.TYPE_GEO, null, null, 10L, getPolygon_2(), MTPlayListRec.STATE_UPTODATE, MTPlayListRec.PLAYED_NO, 5L, 0L, 0L));
        playList.getPlaylist().add(new MTPlayListRec(3L, 3L,  1L,"three", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 10L, null,MTPlayListRec.STATE_UPTODATE, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        playList.getPlaylist().add(new MTPlayListRec(4L, 4L,  1L,"four", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 10L, null, MTPlayListRec.STATE_UPTODATE, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        return playList;
    }

    private List<MTPlayListRec> getStat2_Geo() {
        List<MTPlayListRec> stat = new ArrayList<>();
        stat.add(new MTPlayListRec(3L, 3L,  1L,"three", 10L, 0L, MTPlayListRec.TYPE_COMMERCIAL, null, null, 0L, null, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        stat.add(new MTPlayListRec(2L, 2L,  1L,"two", 10L, 0L, MTPlayListRec.TYPE_GEO, null, null, 0L, null, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        stat.add(new MTPlayListRec(1L, 1L,  1L,"one", 10L, 0L, MTPlayListRec.TYPE_GEO, null, null, 0L, null, MTPlayListRec.STATE_UNKNOWN, MTPlayListRec.PLAYED_NO, 0L, 0L, 0L));
        return stat;
    }

    @Test
    public void check_playlist_gps_1_point_home() throws Exception {
        // init
        MTPlayList playList = getUploadedPlayList_Geo();
        playList.getPlaylist().get(0).setPolygonMarks(getPolygon_2());
        playList.getPlaylist().get(0).setMax_count((long) 3);
        playList.getPlaylist().get(1).setPolygonMarks(getPolygon_2());
        playList.getPlaylist().get(1).setMax_count((long) 30);
        playList.getPlaylist().get(1).setMin_delay((long) 10);

        playListSearch.setMTPlayList(playList);
        playListSearch.checkAndMapGeoVideo(new MTGpsPoint(56.875298, 53.202012), logger);

        List<MTPlayListRec> stat = getStat2_Geo();
        stat.get(1).setPlayedTime(System.currentTimeMillis()-1000 * 60 *20);
        stat.get(2).setPlayedTime(System.currentTimeMillis()-1000 * 60 *20);
        // do
        MTPlayListRec res = playListSearch.getNextVideoFile(stat, 30, logger, new MTGpsPoint(56.875298, 53.202012));
        // assert
        assertEquals(playList.getPlaylist().get(1), res);
    }

    @Test
    public void check_point_inside() throws Exception {
        // init
        boolean isInside = MTGpsUtils.isPointInPolygon(new MTGpsPoint(56.828025,53.137340), getPolygon_4());
        // assert
        assertEquals(isInside, false);
    }

    @Test
    public void check_add_point_to_set() {
        // init
        MTPlayList playList = getUploadedPlayList();
        playList.getPlaylist().get(0).setType(MTPlayListRec.TYPE_GEO);
        playList.getPlaylist().get(0).setPolygonMarks(getPolygon_3());
        playList.getPlaylist().get(1).setType(MTPlayListRec.TYPE_GEO);
        playList.getPlaylist().get(1).setPolygonMarks(getPolygon_2());
        playListSearch.setMTPlayList(playList);

        //do
        playListSearch.checkAndMapGeoVideo(new MTGpsPoint(56.875298, 53.202012), logger);

        // assert
        assertEquals(playListSearch.getGeoVideoToPlay().contains(playList.getPlaylist().get(1).getId()), true);

    }

    ////
    @Test
    public void polygon_serialize_test() {
        MTGpsPoint[] pointList = getPolygon_2();

        String s = MTGpsUtils.writePolygonAsString(pointList);
        MTGpsPoint[] pointList2 = MTGpsUtils.parsePolygon(s);

        for (int i = 0; i<pointList.length; i++) {
            assertEquals(pointList[i].getLatitude(), pointList2[i].getLatitude());
        }
    }

    @Test
    public void playlist_list_parse_test() {
        String s = "[{\"id\":\"2191\",\"filename\":\"/6/2765_6101.mp4\",\"type\":\"COMMERCE\",\"size\":\"9524995\",\"periodicity\":\"10\",\"duration\":\"15\",\"date\":{\"from\":\"2019-04-14\",\"to\":\"2019-09-14\"},\"md5\":\"31b38debceea5fd1b55f6537f1a8b912\"},{\"id\":\"2204\",\"filename\":\"/5/3764_6264.mp4\",\"type\":\"GEO\",\"PolygonMarks\":[{\"x\":\"56.86157226175208\",\"y\":\"53.21571350097656\"},{\"x\":\"56.85495588325215\",\"y\":\"53.21571350097656\"},{\"x\":\"56.85589444787667\",\"y\":\"53.22747230529785\"},{\"x\":\"56.861806863619975\",\"y\":\"53.22798728942871\"},{\"x\":\"56.86434047003716\",\"y\":\"53.222408294677734\"},{\"x\":\"56.86312060688904\",\"y\":\"53.21760177612305\"}],\"max_count\":\"15\",\"min_count\":\"1\",\"size\":\"7592093\",\"duration\":\"20\",\"date\":{\"from\":\"2019-04-15\",\"to\":\"2019-05-15\"},\"md5\":\"bd33edfcf6a692cc59af40691505566d\"},{\"id\":\"2190\",\"filename\":\"/6/2737_6572.mp4\",\"type\":\"COMMERCE\",\"size\":\"7514651\",\"periodicity\":\"15\",\"duration\":\"20\",\"date\":{\"from\":\"2019-04-14\",\"to\":\"2019-09-14\"},\"md5\":\"1469d3743062e180026eadf734add85a\"},{\"id\":\"2208\",\"filename\":\"/6/6468_5927.mp4\",\"type\":\"COMMERCE\",\"size\":\"22307711\",\"periodicity\":\"30\",\"duration\":\"31\",\"date\":{\"from\":\"2019-04-17\",\"to\":\"2019-12-17\"},\"md5\":\"4a4fd3a4dc7461078dbcc436669bfb35\"},{\"id\":\"2151\",\"filename\":\"/6/0541_6688.mp4\",\"type\":\"COMMERCE\",\"size\":\"7330531\",\"periodicity\":\"10\",\"duration\":\"20\",\"date\":{\"from\":\"2019-03-30\",\"to\":\"2019-04-29\"},\"md5\":\"ea7111dbdab5babc6e60762ff2b0501a\"},{\"id\":\"2167\",\"filename\":\"/6/7843_6854.mp4\",\"type\":\"COMMERCE\",\"size\":\"11370843\",\"periodicity\":\"30\",\"duration\":\"30\",\"date\":{\"from\":\"2019-04-06\",\"to\":\"2019-12-06\"},\"md5\":\"990ebe124eaadbfd6dc1f95c261007a8\"},{\"id\":\"2186\",\"filename\":\"/6/2613_1743.mp4\",\"type\":\"COMMERCE\",\"size\":\"5635480\",\"periodicity\":\"15\",\"duration\":\"15\",\"date\":{\"from\":\"2019-04-14\",\"to\":\"2019-08-14\"},\"md5\":\"38a82601a4d27999b090b8c5a377f073\"},{\"id\":\"1952\",\"filename\":\"/6/1947_6164.mp4\",\"type\":\"COMMERCE\",\"size\":\"7743133\",\"periodicity\":\"10\",\"duration\":\"15\",\"date\":{\"from\":\"2019-02-19\",\"to\":\"2019-12-31\"},\"md5\":\"38b6bbe42540b1197922c30ca05a2841\"},{\"id\":\"1951\",\"filename\":\"/6/1920_8791.mp4\",\"type\":\"COMMERCE\",\"size\":\"6636342\",\"periodicity\":\"10\",\"duration\":\"16\",\"date\":{\"from\":\"2019-02-20\",\"to\":\"2019-12-31\"},\"md5\":\"8debdce312f59d8ce38d4b3634e4d4e8\"},{\"id\":\"2188\",\"filename\":\"/6/2667_8809.mp4\",\"type\":\"COMMERCE\",\"size\":\"2112565\",\"periodicity\":\"30\",\"duration\":\"16\",\"date\":{\"from\":\"2019-04-14\",\"to\":\"2019-05-14\"},\"md5\":\"73d45e3b11b379275b8d121dde19de72\"},{\"id\":\"2019\",\"filename\":\"/6/2938_4926.mp4\",\"type\":\"COMMERCE\",\"size\":\"25062474\",\"periodicity\":\"15\",\"duration\":\"50\",\"date\":{\"from\":\"2019-02-20\",\"to\":\"2019-12-31\"},\"md5\":\"b54d133fcbcd566eff63a86b54c1cac7\"},{\"id\":\"2182\",\"filename\":\"/6/8102_8546.mp4\",\"type\":\"COMMERCE\",\"size\":\"3769854\",\"periodicity\":\"30\",\"duration\":\"10\",\"date\":{\"from\":\"2019-04-12\",\"to\":\"2019-05-12\"},\"md5\":\"b669b0fff5a2fcad98d3089d6439ff5c\"},{\"id\":\"2183\",\"filename\":\"/6/8133_3102.mp4\",\"type\":\"COMMERCE\",\"size\":\"3780489\",\"periodicity\":\"30\",\"duration\":\"10\",\"date\":{\"from\":\"2019-04-12\",\"to\":\"2019-05-12\"},\"md5\":\"65abff4a6ed92cee8ffbd6c43fce7b42\"},{\"id\":\"2187\",\"filename\":\"/6/2651_4938.mp4\",\"type\":\"COMMERCE\",\"size\":\"1971626\",\"periodicity\":\"30\",\"duration\":\"16\",\"date\":{\"from\":\"2019-04-14\",\"to\":\"2019-05-14\"},\"md5\":\"6299870634703e53a03657436e81c7d3\"},{\"id\":\"1961\",\"filename\":\"/6/9831_2493.mp4\",\"type\":\"COMMERCE\",\"size\":\"2213640\",\"periodicity\":\"5\",\"duration\":\"39\",\"date\":{\"from\":\"2019-02-19\",\"to\":\"2019-12-31\"},\"md5\":\"727a7d513e2c6db6c35165cd237f0862\"},{\"id\":\"1157\",\"filename\":\"/6/4833_8047.mp4\",\"type\":\"COMMERCE\",\"size\":\"21311414\",\"periodicity\":\"10\",\"duration\":\"58\",\"date\":{\"from\":\"2019-02-19\",\"to\":\"2019-12-31\"},\"md5\":\"839d96db085f217dc1c3433fa5ecc979\"},{\"id\":\"2189\",\"filename\":\"/6/2689_5094.mp4\",\"type\":\"COMMERCE\",\"size\":\"3421848\",\"periodicity\":\"15\",\"duration\":\"10\",\"date\":{\"from\":\"2019-04-14\",\"to\":\"2019-09-14\"},\"md5\":\"ebe6606887d092a5eb68270a301abc21\"},{\"id\":\"2169\",\"filename\":\"/6/7890_3590.mp4\",\"type\":\"COMMERCE\",\"size\":\"7579818\",\"periodicity\":\"10\",\"duration\":\"20\",\"date\":{\"from\":\"2019-04-06\",\"to\":\"2019-12-06\"},\"md5\":\"d44908437ccd204bb7aa841b2860fe99\"},{\"id\":\"2011\",\"filename\":\"/5/6059_4477.mp4\",\"type\":\"COMMERCE\",\"size\":\"10721996\",\"periodicity\":\"30\",\"duration\":\"28\",\"date\":{\"from\":\"2019-02-19\",\"to\":\"2019-12-31\"},\"md5\":\"677399b192e064327816459ccb3ff86e\"},{\"id\":\"2152\",\"filename\":\"/6/0556_4620.mp4\",\"type\":\"COMMERCE\",\"size\":\"3178078\",\"periodicity\":\"10\",\"duration\":\"12\",\"date\":{\"from\":\"2019-03-30\",\"to\":\"2019-04-29\"},\"md5\":\"29e602c782eb5bfc95ca611a1cb87918\"},{\"id\":\"1612\",\"filename\":\"/6/6227_4088.mp4\",\"type\":\"COMMERCE\",\"size\":\"5792581\",\"periodicity\":\"15\",\"duration\":\"15\",\"date\":{\"from\":\"2019-03-23\",\"to\":\"2019-05-22\"},\"md5\":\"4af3bff71d0e6a9c557ba70fb88e8a27\"},{\"id\":\"1949\",\"filename\":\"/6/5560_6840.mp4\",\"type\":\"COMMERCE\",\"size\":\"14514053\",\"periodicity\":\"10\",\"duration\":\"30\",\"date\":{\"from\":\"2019-02-19\",\"to\":\"2019-12-31\"},\"md5\":\"aef7d5b921b8700a16ee5fe4e8ffac20\"},{\"id\":\"1948\",\"filename\":\"/6/5420_9747.mp4\",\"type\":\"COMMERCE\",\"size\":\"17519866\",\"periodicity\":\"10\",\"duration\":\"32\",\"date\":{\"from\":\"2019-02-19\",\"to\":\"2019-12-31\"},\"md5\":\"412ec396981eaba2ff302acf38db15e0\"},{\"id\":\"1962\",\"filename\":\"/6/9855_2168.mp4\",\"type\":\"COMMERCE\",\"size\":\"3775506\",\"periodicity\":\"10\",\"duration\":\"16\",\"date\":{\"from\":\"2019-02-20\",\"to\":\"2019-12-31\"},\"md5\":\"2a9478cd5b6902704c15aa6a3bae7931\"},{\"id\":\"1960\",\"filename\":\"/6/9815_6000.mp4\",\"type\":\"COMMERCE\",\"size\":\"5605305\",\"periodicity\":\"10\",\"duration\":\"16\",\"date\":{\"from\":\"2019-02-19\",\"to\":\"2019-12-31\"},\"md5\":\"950e505791037e962e84936b5db9354f\"},{\"id\":\"2106\",\"filename\":\"/6/2491_7846.mp4\",\"type\":\"COMMERCE\",\"size\":\"6295033\",\"periodicity\":\"15\",\"duration\":\"20\",\"date\":{\"from\":\"2019-04-14\",\"to\":\"2019-09-14\"},\"md5\":\"0cc761cd8daa553e66124d4ff79ce52b\"},{\"id\":\"2014\",\"filename\":\"/6/1064_1761.mp4\",\"type\":\"COMMERCE\",\"size\":\"9368670\",\"periodicity\":\"15\",\"duration\":\"20\",\"date\":{\"from\":\"2019-03-23\",\"to\":\"2019-06-22\"},\"md5\":\"74ffdd9893728bc167274f198fed7c50\"},{\"id\":\"1471\",\"filename\":\"/6/4946_7773.mp4\",\"type\":\"COMMERCE\",\"size\":\"10229832\",\"periodicity\":\"15\",\"duration\":\"28\",\"date\":{\"from\":\"2019-03-23\",\"to\":\"2019-04-22\"},\"md5\":\"68c3a153bff586c61c47e9cc01070850\"}]";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            MTPlayListRec[]  pl = objectMapper.readValue(s, new TypeReference<MTPlayListRec[]>() {});
            System.out.println(pl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}