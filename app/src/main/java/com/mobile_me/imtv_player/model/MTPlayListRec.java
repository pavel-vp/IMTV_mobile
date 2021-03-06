package com.mobile_me.imtv_player.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by pasha on 24.12.16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MTPlayListRec implements Serializable {

    public static final String TYPE_COMMERCIAL = "COMMERCE";
    public static final String TYPE_FREE = "FREE";
    public static final String TYPE_GEO = "GEO";

    public static final int STATE_UNKNOWN = 0;
    public static final int STATE_NEED_LOAD = 1;
    public static final int STATE_LOADING = 2;
    public static final int STATE_UPTODATE = 3;

    public static final int PLAYED_NO = 0;
    public static final int PLAYED_YES = 1;

    // Общее
    private Long idx;
    private Long id;
    private Long vpid;
    private String filename; // ИмяФайла
    private Long duration = Long.valueOf(0); // Время ролика: 23сек.
    private Long size = Long.valueOf(0); // размер в байтах
    private String type;
    private MTDateRec date = new MTDateRec(); // Период выхода ролика
    private String md5; // md5(“ИмяФайла” + “Время ролика”)

    // Для коммерческого
    private Long periodicity = Long.valueOf(0); // Периодичность: раз в 10 мин
    // GPS таргетированные
    @SerializedName("PolygonMarks")
    private MTGpsPoint[] polygonMarks;
    private Long max_count;
    private Long min_count;
    private Long min_delay; // минимальное время между воспроизведениями этого ролика в минутах

    private transient int state = STATE_UNKNOWN;
    private transient int played = PLAYED_NO;
    private transient Long playedTime = 0L;

    public MTPlayListRec() {  }

    public MTPlayListRec(Long idx, Long id, Long vpid, String filename, Long duration, Long size, String type, MTDateRec date, String md5, Long periodicity, MTGpsPoint[] polygonMarks, int state, int played, Long max_count, Long min_count, Long min_delay) {
        this.idx = idx;
        this.id = id;
        this.vpid = vpid;
        this.filename = filename;
        this.duration = duration;
        this.size = size;
        this.type = type;
        this.date = date;
        this.md5 = md5;
        this.periodicity = periodicity;
        this.polygonMarks = polygonMarks;
        this.state = state;
        this.played = played;
        this.max_count = max_count;
        this.min_count = min_count;
        this.min_delay = min_delay;
    }

    public Long getIdx() {
        return idx;
    }

    public void setIdx(Long idx) {
        this.idx = idx;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MTDateRec getDate() {
        return date;
    }

    public void setDate(MTDateRec date) {
        this.date = date;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Long getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(Long periodicity) {
        this.periodicity = periodicity;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getPlayed() {
        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public MTPlayListRec getCopy() {
        MTPlayListRec copy = new MTPlayListRec(idx, id, vpid, filename, duration, size, type, date, md5, periodicity, polygonMarks, STATE_UNKNOWN, PLAYED_NO, max_count, min_count, min_delay);
        return copy;
    }

    public MTGpsPoint[] getPolygonMarks() {
        return this.polygonMarks;
    }

    public void setPolygonMarks(MTGpsPoint[] polygonMarks) {
        this.polygonMarks = polygonMarks;
    }


    public Long getMax_count() {
        return max_count;
    }

    public void setMax_count(Long max_count) {
        this.max_count = max_count;
    }

    public Long getMin_count() {
        return min_count;
    }

    public void setMin_count(Long min_count) {
        this.min_count = min_count;
    }

    public Long getVpid() {
        return vpid;
    }

    public void setVpid(Long vpid) {
        this.vpid = vpid;
    }


    public Long getMin_delay() {
        return min_delay;
    }

    public void setMin_delay(Long min_delay) {
        this.min_delay = min_delay;
    }


    public Long getPlayedTime() {
        return playedTime;
    }

    public void setPlayedTime(Long playedTime) {
        this.playedTime = playedTime;
    }

    @Override
    public String toString() {
        return "MTPlayListRec{" +
                "idx=" + idx +
                ", id=" + id +
                ", vpid=" + vpid +
                ", filename='" + filename + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", type='" + type + '\'' +
                ", date=" + date +
                ", md5='" + md5 + '\'' +
                ", periodicity=" + periodicity +
                ", polygonMarks=" + Arrays.toString(polygonMarks) +
                ", max_count=" + max_count +
                ", min_count=" + min_count +
                ", min_delay=" + min_delay +
                ", state=" + state +
                ", played=" + played +
                ", playedTime=" + playedTime +
                '}';
    }

    public boolean isEqualTo(MTPlayListRec recOther) {
        if (recOther == null) return false;
        if (Objects.equals(this.id, recOther.getId()) &&
                Objects.equals(this.vpid, recOther.getVpid()) &&
                Objects.equals(this.filename, recOther.getFilename()) &&
                Objects.equals(this.md5, recOther.getMd5()) &&
                this.date.isEquals(recOther.getDate()))
            return true;
        return false;
    }
}

