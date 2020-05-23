package com.metroinfrasys.metrotms.utils;

import com.speedata.libutils.excel.Excel;

public class EPCBean {

    @Excel(ignore = false, name = "EPC")
    String EPC;

    @Excel(ignore = true, name = "TID_USER")
    String TID_USER;


    public String getEPC() {
        return EPC;
    }

    public void setEPC(String EPC) {
        this.EPC = EPC;
    }

    public String getTID_USER() {
        return TID_USER;
    }

    public void setTID_USER(String TID_USER) {
        this.TID_USER = TID_USER;
    }
}