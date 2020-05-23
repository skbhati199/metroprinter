package com.metroinfrasys.metrotms;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.metroinfrasys.metrotms.api.AppServiceAPI;
import com.metroinfrasys.metrotms.utils.AppSharedPreference;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.utils.SharedXmlUtil;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MetroTMSApp extends Application {

    private static Retrofit retrofit;
    static int value = 100000;
    private static MetroTMSApp m_application;
    private IUHFService iuhfService;
    public static boolean isOpenDev = false;
    public static boolean isOpenServer = true;
    public static MetroTMSApp getInstance() {
        return m_application;
    }

    public static int getPreparedReceiptNumber() {
        return value++;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

    }

    @Override
    public void onCreate() {
        super.onCreate();

//        Log.d("APP", "onCreate");
//        m_application = this;
//        Context context = getApplicationContext();
//        // 获取当前包名
//        String packageName = context.getPackageName();
//        // 获取当前进程名
//        String processName = getProcessName(android.os.Process.myPid());
//        // 设置是否为上报进程
//        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
//        strategy.setUploadProcess(processName == null || processName.equals(packageName));
//        // 初始化Bugly
//        Bugly.init(getApplicationContext(), "75242a29e5", true, strategy);
//
//        Log.d("UHFService","MyApp onCreate");

    }

//    public IUHFService getIuhfService() {
//        return iuhfService;
//    }
//
//    public void setIuhfService(){
//
//        try {
//            iuhfService = UHFManager.getUHFService(getApplicationContext());
//            Log.d("UHFService","iuhfService初始化: "+iuhfService);
//        } catch (Exception e) {
//            e.printStackTrace();
//            boolean cn = getApplicationContext().getResources().getConfiguration().locale.getCountry().equals("CN");
//            if (cn) {
//                Toast.makeText(getApplicationContext(), "模块不存在", Toast.LENGTH_SHORT).show();
//            } else {
////                Toast.makeText(getApplicationContext(), "Module does not exist", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//    }

//    private static String getProcessName(int pid) {
//        BufferedReader reader = null;
//        try {
//            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
//            String processName = reader.readLine();
//            if (!TextUtils.isEmpty(processName)) {
//                processName = processName.trim();
//            }
//            return processName;
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        } finally {
//            try {
//                if (reader != null) {
//                    reader.close();
//                }
//            } catch (IOException exception) {
//                exception.printStackTrace();
//            }
//        }
//        return null;
//    }

//    @Override
//    public void onTerminate() {
//        stopService(new Intent(this,MyService.class));
//        SharedXmlUtil.getInstance(this).write("server", false);
//        iuhfService.closeDev();
//        MetroTMSApp.isOpenDev = false;
//        UHFManager.closeUHFService();
//        super.onTerminate();
//    }


    public static AppServiceAPI createRetrofit(Context context) {
        String url = "http://"+AppSharedPreference.getHostName(context)+":"+AppSharedPreference.getPortNumber(context);
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(AppServiceAPI.class);
    }
}
