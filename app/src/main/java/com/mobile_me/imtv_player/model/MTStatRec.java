package com.mobile_me.imtv_player.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by pasha on 06.03.18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MTStatRec implements Serializable {
    @JsonIgnore
    private Long idx;
    private Long id;
    private Long vpid;
    private String dt;
    private double lat;
    private double lon;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public Long getIdx() {
        return idx;
    }

    public void setIdx(Long idx) {
        this.idx = idx;
    }

    public Long getVpid() {
        return vpid;
    }

    public void setVpid(Long vpid) {
        this.vpid = vpid;
    }
}
