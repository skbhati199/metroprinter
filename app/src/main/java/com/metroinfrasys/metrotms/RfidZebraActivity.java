package com.metroinfrasys.metrotms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.metroinfrasys.metrotms.api.AppServiceAPI;
import com.metroinfrasys.metrotms.data.fastagProcess.FastagProcessAPI;
import com.metroinfrasys.metrotms.utils.AppSharedPreference;
import com.metroinfrasys.metrotms.utils.NetworkUtils;
import com.metroinfrasys.metrotms.utils.UIHelper;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.TagData;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RfidZebraActivity extends AppCompatActivity implements DialogInterface.OnClickListener , AdapterView.OnItemSelectedListener , RFIDHandler.ResponseHandlerInterface {

    TextView txt_user_id, txt_shift_no, txt_epc, txt_tid,txt_vehicle_no;
    Button btn_scan, btn_back, btn_clear,btn_process;
    Spinner spinner_lane_id;
    private Toolbar toolBar;

    View mainLayout;
    String str_userName="Administration",str_shiftNumber="8112",str_lane_id,str_epc,str_userdata,str_plaza_id = "2",str_vehicle_id,
            str_tollzone_id = "073002",str_date_time;
    private AlertDialog dialog;
    protected static final String TAG = "RFID";
    RFIDHandler rfidHandler;

    String[] lane_id = {"Select Lane", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid_zebra);
        toolBar = findViewById(R.id.toolbar);
        toolBar.setBackgroundResource(R.color.colorPrimary);
        setSupportActionBar(toolBar);
        mainLayout = findViewById(R.id.mainLayout);
        spinner_lane_id = findViewById(R.id.spinner_lane_id);
        txt_user_id = findViewById(R.id.txt_user_id);
        txt_shift_no = findViewById(R.id.txt_shift_no);
        txt_epc = findViewById(R.id.txt_epc);
        txt_tid = findViewById(R.id.txt_tid);
        btn_scan = findViewById(R.id.btn_scan);
        btn_clear = findViewById(R.id.btn_clear);
        btn_back = findViewById(R.id.btn_back);
        btn_process = findViewById(R.id.process);
        txt_vehicle_no = findViewById(R.id.txt_vehicle_no);

        // RFID Handler
        rfidHandler = new RFIDHandler();
        rfidHandler.onCreate(this);

        toolBar.setTitle("RFID FASTag");
        spinner_lane_id.setOnItemSelectedListener(this);

        str_userName = AppSharedPreference.getLoginId(this);
        str_shiftNumber = AppSharedPreference.getShiftNumber(this);
        txt_user_id.setText(str_userName);
        txt_shift_no.setText(str_shiftNumber);

        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, lane_id);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinner_lane_id.setAdapter(aa);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RfidZebraActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_epc.setText("");
                txt_tid.setText("");
                txt_vehicle_no.setText("");

                ArrayAdapter aa = new ArrayAdapter(RfidZebraActivity.this, android.R.layout.simple_spinner_item, lane_id);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                spinner_lane_id.setAdapter(aa);
            }
        });

        btn_process.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {

                //     str_shiftNumber = AppSharedPreference.getShiftNumber(RFIDActivity.this);
                //       str_userName = AppSharedPreference.getSupervisorLoginId(RFIDActivity.this);
                str_lane_id = spinner_lane_id.getSelectedItem().toString();
                str_epc = txt_epc.getText().toString().trim();
                str_userdata = txt_tid.getText().toString().trim();

                Log.v("data",str_shiftNumber+"#"+str_userName+"#"+str_lane_id+"#"+str_epc+"#"+str_userdata+"#"+str_date_time+"#"+str_vehicle_id+"#");

                if(str_lane_id.equals("Select Lane")){
                    Toast.makeText(RfidZebraActivity.this, "Please Select Lane.", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(str_epc)){
                    Toast.makeText(RfidZebraActivity.this, "Please Scan FASTag Again.", Toast.LENGTH_SHORT).show();
                } else if(TextUtils.isEmpty(str_userdata)){
                    Toast.makeText(RfidZebraActivity.this, "Please Scan FASTag Again.", Toast.LENGTH_SHORT).show();
                } else{
                    if(isConnected()){
                        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                        str_date_time = currentDate+" "+currentTime;
                        CheckTagvalidity(str_epc,str_userdata);
                        getTransaction();

                    } else {
                        snakbarHelper();
                        Toast.makeText(RfidZebraActivity.this, "Your Device is not connected to Internet.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private void getTransaction() {
        final UIHelper uiHelper = new UIHelper(this);
        uiHelper.showLoadingDialog("Loading...");
        btn_process.setBackgroundResource(R.color.buttonColorDisabled);
        AppServiceAPI serviceAPI = MetroTMSApp.createRetrofit(this);
        Call<FastagProcessAPI> userModelCall = serviceAPI.fastagProcess(str_plaza_id,str_lane_id,str_epc,
                str_vehicle_id,str_userdata,str_date_time,str_tollzone_id);

        userModelCall.enqueue(new Callback<FastagProcessAPI>() {
            @Override
            public void onResponse(Call<FastagProcessAPI> call, Response<FastagProcessAPI> response) {
                uiHelper.dismissLoadingDialog();
                Log.e(TAG, "url: " + call.request().url());
                if (response.body() == null) {
                    txt_epc.setText("");
                    txt_tid.setText("");
                    txt_vehicle_no.setText("");
                    btn_process.setBackgroundResource(R.color.colorPrimary);
                    ArrayAdapter aa = new ArrayAdapter(RfidZebraActivity.this, android.R.layout.simple_spinner_item, lane_id);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_lane_id.setAdapter(aa);
                    networkfailurepopup("Server Error contact Administrator");
                    return;
                }

                if (response.body().getStatus()) {
                    btn_process.setBackgroundResource(R.color.colorPrimary);
                    txt_epc.setText("");
                    txt_tid.setText("");
                    txt_vehicle_no.setText("");
                    ArrayAdapter aa = new ArrayAdapter(RfidZebraActivity.this, android.R.layout.simple_spinner_item, lane_id);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_lane_id.setAdapter(aa);
                    networkfailurepopup(response.body().getMessage());

                } else {
                    btn_process.setBackgroundResource(R.color.colorPrimary);
                    txt_epc.setText("");
                    txt_tid.setText("");
                    txt_vehicle_no.setText("");
                    ArrayAdapter aa = new ArrayAdapter(RfidZebraActivity.this, android.R.layout.simple_spinner_item, lane_id);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_lane_id.setAdapter(aa);
                    networkfailurepopup(response.body().getMessage());
                }

            }

            @Override
            public void onFailure(Call<FastagProcessAPI> call, Throwable t) {
                btn_process.setBackgroundResource(R.color.colorPrimary);
                txt_epc.setText("");
                txt_tid.setText("");
                txt_vehicle_no.setText("");
                ArrayAdapter aa = new ArrayAdapter(RfidZebraActivity.this, android.R.layout.simple_spinner_item, lane_id);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                spinner_lane_id.setAdapter(aa);
                networkfailurepopup("Host IP" + AppSharedPreference.getHostName(RfidZebraActivity.this) + " is not unreachable");
                uiHelper.dismissLoadingDialog();

            }
        });
    }

    public void networkfailurepopup(String message) {

        if (!this.isFinishing()) {
            //show dialog
            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
            alertDialog.setTitle(getString(R.string.app_name));
            alertDialog.setMessage(message);
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            dialog = alertDialog.create();
            dialog.show();
        }

    }

    public boolean isConnected() {
        return NetworkUtils.isNetworkConnected(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        rfidHandler.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        String status = rfidHandler.onResume();
        Toast.makeText(RfidZebraActivity.this, status, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rfidHandler.onDestroy();
    }

    @Override
    public void handleTagdata(TagData[] tagData ) {
        final StringBuilder sb = new StringBuilder();
        for (int index = 0; index < tagData.length; index++) {
            sb.append(tagData[index].getTagID() + "\n");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt_epc.append(sb.toString());
            }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txt_epc.setText("");
                }
            });
            rfidHandler.performInventory();
        } else
            rfidHandler.stopInventory();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private final void CheckTagvalidity(String EPC, String userdata) {

        String UserID = userdata;
        byte[] bytes = hexStringToByteArray(UserID.substring(4, 20));
        String User_VehicleRegNo = new String(bytes, StandardCharsets.UTF_8);
        String Tag_Vehicle_Class = UserID.substring(24,26);
        BigInteger value = new BigInteger(Tag_Vehicle_Class, 16);
        str_vehicle_id = String.valueOf(value);
        txt_vehicle_no.setText(User_VehicleRegNo);
        Log.v("vehcile class",User_VehicleRegNo+"#"+value+"");
    }

    public static byte[] hexStringToByteArray(String hex) {
        int l = hex.length();
        byte[] data = new byte[l/2];
        for (int i = 0; i < l; i += 2) {
            data[i/2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    private void snakbarHelper() {
        Snackbar snackbar = Snackbar.make(mainLayout, "No internet connection", Snackbar.LENGTH_LONG);
        final ViewGroup.LayoutParams params = snackbar.getView().getLayoutParams();
        if (params instanceof CoordinatorLayout.LayoutParams) {
            ((CoordinatorLayout.LayoutParams) params).gravity = Gravity.TOP | Gravity.CENTER;
        } else {
            ((FrameLayout.LayoutParams) params).gravity = Gravity.TOP;
        }
        snackbar.getView().setLayoutParams(params);
        snackbar.show();
    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

