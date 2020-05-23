package com.metroinfrasys.metrotms.data.laneprocess;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("FareAmount")
    @Expose
    private String fareAmount;
    @SerializedName("luhncode")
    @Expose
    private String luhncode;

    public String getFareAmount() {
        return fareAmount;
    }

    public void setFareAmount(String fareAmount) {
        this.fareAmount = fareAmount;
    }

    public String getLuhncode() {
        return luhncode;
    }

    public void setLuhncode(String luhncode) {
        this.luhncode = luhncode;
    }
}