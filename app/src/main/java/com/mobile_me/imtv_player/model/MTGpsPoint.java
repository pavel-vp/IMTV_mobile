package com.mobile_me.imtv_player.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MTGpsPoint implements Serializable {
    @SerializedName("x")
    private Double latitude;
    @SerializedName("y")
    private Double longitude;

    public MTGpsPoint(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public MTGpsPoint() {
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "MTGpsPoint{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
