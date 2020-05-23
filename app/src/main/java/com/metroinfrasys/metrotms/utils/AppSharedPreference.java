package com.metroinfrasys.metrotms.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppSharedPreference {

    public static final String HOST_NAME = "host_name";
    public static final String IS_AUTH = "isLogin";
    public static final String USER_NAME = "user_name";
    public static final String USER_ID = "user_id";
    public static final String SHIFT_NUMBER = "shift_number";
    public static final String PORT_NO = "port";
    private static final String LANE_NUMBER = "lane_number";
    public static SharedPreferences mPrefs;
    public static final String PREF_FILE_NAME = "metro_tms";
    public static final String PLAZA_ID = "plaza_id";
    public static final String SEAL_NO = "seal_no";
    public static final String LOGIN_ID = "login_id";
    public static final String OPERATOR_LOGIN_STATUS = "operator_login_status";
    public static final String SUPERVISOR_LOGIN_STATUS = "supervisor_login_status";
    public static final String SERVER_STATUS = "server_status";
    public static final String SUPERVISOR_LOGIN_ID = "supervisor_login_id";

    private AppSharedPreference() {

    }

    public static void setHostIp(Context context,String hostName) {
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(HOST_NAME, hostName);
        editor.commit();
    }

    public static String getHostName(Context context){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return mPrefs.getString(HOST_NAME, "");
    }

    public static void setAuth(Context context,boolean isAuth){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(IS_AUTH, isAuth);
        editor.commit();
    }

    public static boolean isAuth(Context context){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return (boolean)mPrefs.getBoolean(IS_AUTH,false);
    }


    public static void setShiftNumber(Context context,String shiftNumber){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(SHIFT_NUMBER, shiftNumber);
        editor.commit();
    }

    public static String getShiftNumber(Context context){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return mPrefs.getString(SHIFT_NUMBER, "");
    }

    public static void setPlazaId(Context context,String plazaId){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PLAZA_ID, plazaId);
        editor.commit();
    }

    public static String getPlazaId(Context context){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return mPrefs.getString(PLAZA_ID, "");
    }

    public static void setSealNo(Context context,String sealNo){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(SEAL_NO, sealNo);
        editor.commit();
    }

    public static String getSealNo(Context context){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return mPrefs.getString(SEAL_NO, "");
    }

    public static void setLoginId(Context context,String loginId){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(LOGIN_ID, loginId);
        editor.apply();
    }

    public static String getLoginId(Context context){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return mPrefs.getString(LOGIN_ID, "");
    }

    public  static boolean setOperatorLoginStatus(Context context, boolean status){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(OPERATOR_LOGIN_STATUS,status);
        editor.commit();
        return false;
    }

    public static boolean getOperatorLoginStatus(Context context){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return mPrefs.getBoolean(OPERATOR_LOGIN_STATUS, false);
    }

    public static  boolean setSupervisorLoginStatus(Context context, boolean status){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(SUPERVISOR_LOGIN_STATUS,status);
        editor.commit();
        return false;
    }

    public static boolean getSupervisorLoginStatus(Context context){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return mPrefs.getBoolean(SUPERVISOR_LOGIN_STATUS, false);
    }

    public static boolean setServerStatus(Context context, boolean status){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(SERVER_STATUS,status);
        editor.commit();
        return false;
    }

    public static boolean getServerStatus(Context context){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return mPrefs.getBoolean(SERVER_STATUS, false);
    }

    public static void setSupervisorLoginId(Context context,String sealNo){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(SUPERVISOR_LOGIN_ID, sealNo);
        editor.apply();
    }

    public static String getSupervisorLoginId(Context context){
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return mPrefs.getString(SUPERVISOR_LOGIN_ID, "");
    }

    public static void setPort(Context context,String portNumber) {
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PORT_NO, portNumber);
        editor.commit();
    }

    public static String getPortNumber(Context context) {
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return mPrefs.getString(PORT_NO, "" );
    }

    public static void setLaneNumber(Context context,int laneNumber) {
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(LANE_NUMBER, laneNumber);
        editor.commit();
    }

    public static int getLaneNumber(Context context) {
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return mPrefs.getInt(LANE_NUMBER, 0);
    }
}
