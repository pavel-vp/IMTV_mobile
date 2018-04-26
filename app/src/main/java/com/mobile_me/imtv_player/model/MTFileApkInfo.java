package com.mobile_me.imtv_player.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by pasha on 25.04.18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MTFileApkInfo implements Serializable {
    private String fn;
    private String dateTime;
    private Long size;

    @Override
    public String toString() {
        return "MTFileApkInfo{" +
                "fn='" + fn + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", size=" + size +
                '}';
    }

    public String getFn() {
        return fn;
    }

    public String getDateTime() {
        return dateTime;
    }

    public Long getSize() {
        return size;
    }
}
