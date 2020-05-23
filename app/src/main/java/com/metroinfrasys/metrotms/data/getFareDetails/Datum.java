package com.metroinfrasys.metrotms.data.getFareDetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum {

    @SerializedName("PlazaID")
    @Expose
    private String plazaID;
    @SerializedName("FareID")
    @Expose
    private String fareID;
    @SerializedName("Paymentmeanstype")
    @Expose
    private String paymentmeanstype;
    @SerializedName("ClassID")
    @Expose
    private String classID;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("ShortDescription")
    @Expose
    private String shortDescription;
    @SerializedName("Amount")
    @Expose
    private String amount;
    @SerializedName("AdminFee")
    @Expose
    private String adminFee;
    @SerializedName("Securityfee")
    @Expose
    private String securityfee;
    @SerializedName("Basefare")
    @Expose
    private String basefare;

    public String getPlazaID() {
        return plazaID;
    }

    public void setPlazaID(String plazaID) {
        this.plazaID = plazaID;
    }

    public String getFareID() {
        return fareID;
    }

    public void setFareID(String fareID) {
        this.fareID = fareID;
    }

    public String getPaymentmeanstype() {
        return paymentmeanstype;
    }

    public void setPaymentmeanstype(String paymentmeanstype) {
        this.paymentmeanstype = paymentmeanstype;
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAdminFee() {
        return adminFee;
    }

    public void setAdminFee(String adminFee) {
        this.adminFee = adminFee;
    }

    public String getSecurityfee() {
        return securityfee;
    }

    public void setSecurityfee(String securityfee) {
        this.securityfee = securityfee;
    }

    public String getBasefare() {
        return basefare;
    }

    public void setBasefare(String basefare) {
        this.basefare = basefare;
    }

}