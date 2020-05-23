package com.metroinfrasys.metrotms.data.googlepay;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QRCodeCreateAPI {

    @SerializedName("qrLink")
    @Expose
    private String qrLink;

    public String getQrLink() {
        return qrLink;
    }

    public void setQrLink(String qrLink) {
        this.qrLink = qrLink;
    }

}