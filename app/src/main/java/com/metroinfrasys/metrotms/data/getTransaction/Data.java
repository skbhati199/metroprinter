package com.metroinfrasys.metrotms.data.getTransaction;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("MerchantId")
    @Expose
    private String merchantId;
    @SerializedName("TransactionId")
    @Expose
    private String transactionId;
    @SerializedName("CurrencyCode")
    @Expose
    private String currencyCode;
    @SerializedName("Amount")
    @Expose
    private String amount;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("ValidDuration")
    @Expose
    private String validDuration;
    @SerializedName("QrType")
    @Expose
    private String qrType;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValidDuration() {
        return validDuration;
    }

    public void setValidDuration(String validDuration) {
        this.validDuration = validDuration;
    }

    public String getQrType() {
        return qrType;
    }

    public void setQrType(String qrType) {
        this.qrType = qrType;
    }

}
