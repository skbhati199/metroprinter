package com.metroinfrasys.metrotms.data.validate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TransactionValidateAPI {

    @SerializedName("Status")
    @Expose
    private Boolean status;
    @SerializedName("Error")
    @Expose
    private String error;
    @SerializedName("Message")
    @Expose
    private String message;
    @SerializedName("Data")
    @Expose
    private String data;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}

