package com.metroinfrasys.metrotms.data.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("LoginID")
    @Expose
    private String loginID;
    @SerializedName("Lat")
    @Expose
    private Object lat;
    @SerializedName("Lng")
    @Expose
    private Object lng;
    @SerializedName("Active")
    @Expose
    private Boolean active;
    @SerializedName("PlazaID")
    @Expose
    private Integer plazaID;
    @SerializedName("SealNo")
    @Expose
    private String sealNo;

    public String getLoginID() {
        return loginID;
    }

    public void setLoginID(String loginID) {
        this.loginID = loginID;
    }

    public Object getLat() {
        return lat;
    }

    public void setLat(Object lat) {
        this.lat = lat;
    }

    public Object getLng() {
        return lng;
    }

    public void setLng(Object lng) {
        this.lng = lng;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getPlazaID() {
        return plazaID;
    }

    public void setPlazaID(Integer plazaID) {
        this.plazaID = plazaID;
    }

    public String getSealNo() {
        return sealNo;
    }

    public void setSealNo(String sealNo) {
        this.sealNo = sealNo;
    }

}