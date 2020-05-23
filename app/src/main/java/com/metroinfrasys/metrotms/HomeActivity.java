package com.metroinfrasys.metrotms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.metroinfrasys.metrotms.api.AppServiceAPI;
import com.metroinfrasys.metrotms.data.getFareDetails.GetFareDetailsAPI;
import com.metroinfrasys.metrotms.data.getTransaction.GetTransactionIdAPI;
import com.metroinfrasys.metrotms.data.laneprocess.LaneProcessModel;
import com.metroinfrasys.metrotms.data.logout.LogoutModel;
import com.metroinfrasys.metrotms.data.server.ServerModel;
import com.metroinfrasys.metrotms.data.validate.TransactionValidateAPI;
import com.metroinfrasys.metrotms.ui.BaseActivity;
import com.metroinfrasys.metrotms.utils.AppSharedPreference;
import com.metroinfrasys.metrotms.utils.FormatRefresher;
import com.metroinfrasys.metrotms.utils.SelectedPrinterManager;
import com.metroinfrasys.metrotms.utils.UIHelper;
import com.metroinfrasys.metrotms.utils.NetworkUtils;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterNetwork;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    Button mBtnProcess, mBtnCarJeep, mBtnLcv, mBtnBusTrack,mBtnmav,mBtnosv,mBtnRFID;
    TextView textViewUserId, textViewShiftNumber;


    public static int WHITE = 0xFFFFFFFF;
    public static int BLACK = 0xFF000000;
    public final static int WIDTH=500;

    boolean status, clicked = false, spin = false, journey = false;
    int pass_algo_int;
    private AlertDialog dialog;
    String str_plaza_id, str_pass_algo, dateToStr, str_pass_number, str_class_id, str_shift_number,
            str_supervisor_loginID, str_seal_no, str_tc_class, str_fare_amount,str_payment_mode,str_merchant_id,str_currency_code,
    str_description,str_valid_duration,str_QrType = "",str_journey_type="",car_single,lcv_single,truck_bus_single,mav_single,osv_single,
            car_double,lcv_double,truck_bus_double,mav_double,osv_double;
    private View homeLayout;
    private Toolbar toolbar;
    private String luhncode,str_transactionId;
    Spinner spinner_payment_mode,spinner_journey_type;
    ImageView image_qrcode;
     int laneNumber,receiptNumber;

    private boolean isReadyStatus = false;
    String qr;

    MyPrinterStatus printerStatus;
    private TextView textFareAmount;
    private boolean isConnectedHost = false;

    String[] payment_mode = { "Select Mode", "Cash", "UPI"};
    String[] journey_type = { "Select Journey", "Single", "Return"};

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView();
        toolbar = findViewById(R.id.toolbar);
        homeLayout = findViewById(R.id.homelayout);
        progressCircular = findViewById(R.id.progressCircular);
        textViewUserId = findViewById(R.id.txt_user_id);
        textViewShiftNumber = findViewById(R.id.txt_shift_no);
        textFareAmount = findViewById(R.id.fare_ammount);
        spinner_payment_mode = findViewById(R.id.spinner_payment_mode);
        spinner_journey_type = findViewById(R.id.spinner_journey_type);

        mBtnProcess = (Button) findViewById(R.id.process);
        mBtnRFID = (Button) findViewById(R.id.processRFID);
        mBtnCarJeep = (Button) findViewById(R.id.carJeepBtn);
        mBtnLcv = (Button) findViewById(R.id.lcvBtn);
        mBtnBusTrack = (Button) findViewById(R.id.busTrackBtn);
        mBtnmav = findViewById(R.id.mavBtn);
        mBtnosv = findViewById(R.id.osvBtn);
        Spinner printerSpinner = (Spinner) findViewById(R.id.main_spinner);
        toolbar.setTitle("HHM App");
        setSupportActionBar(toolbar);
        String host = AppSharedPreference.getHostName(this);
        String port = AppSharedPreference.getPortNumber(this);
        String userName = AppSharedPreference.getLoginId(this);
        String shiftNumber = AppSharedPreference.getShiftNumber(this);
        textViewUserId.setText(userName);
        textViewShiftNumber.setText(shiftNumber);

        spinner_payment_mode.setOnItemSelectedListener(this);
        spinner_journey_type.setOnItemSelectedListener(this);

       // fareDetailsAPI();

        if ((host != null && !TextUtils.isEmpty(host)) && (port != null && !TextUtils.isEmpty(port))) {
            if (isReadyStatus) {
                if (isConnected()) {
                    serverStatusAPI();
                } else {
                    toolbar.setBackgroundResource(R.color.red);
                    mBtnProcess.setEnabled(false);
                    mBtnProcess.setBackgroundResource(R.color.buttonColorDisabled);
                    snakbarHelper();
                    Toast.makeText(HomeActivity.this, "Your Device is not connected to Internet.", Toast.LENGTH_SHORT).show();
                }

            } else {
//                setStatus("Printer Not Connected", Color.RED);
                Toast.makeText(HomeActivity.this, "Printer not connected", Toast.LENGTH_SHORT).show();
            }
            startTimer();

            if (!AppSharedPreference.isAuth(this)) { // Not login App
                Intent intent = new Intent(this, ScreenLoginActivity.class);
                startActivity(intent);
                finish();
            } else if (!AppSharedPreference.getSupervisorLoginStatus(HomeActivity.this)) {
                Intent intent = new Intent(HomeActivity.this, SupervisorActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            startActivity(SettingsActivity.startSettingActivity(this));
            finish();

        }
        status = AppSharedPreference.getServerStatus(HomeActivity.this);

        mBtnRFID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RfidZebraActivity.class);
                startActivity(intent);
            }
        });


        mBtnCarJeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(str_journey_type.equals("Single")){
                    textFareAmount.setText(car_single);
                    str_fare_amount = car_single;
                    clicked = true;
                    str_class_id = "1";
                    str_tc_class = "Car/Jeep/Van";
                }else if(str_journey_type.equals("Return")){
                    textFareAmount.setText(car_double);
                    str_fare_amount = car_double;
                    clicked = true;
                    str_class_id = "1";
                    str_tc_class = "Car/Jeep/Van";
                } else {
                    Toast.makeText(HomeActivity.this, "Please Select Journey Type.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mBtnCarJeep.setBackgroundResource(R.color.buttonColorDisabled);
                mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                mBtnmav.setBackgroundResource(R.color.colorPrimary);
                mBtnosv.setBackgroundResource(R.color.colorPrimary);
            }
        });

        mBtnLcv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(str_journey_type.equals("Single")){
                    textFareAmount.setText(lcv_single);
                    str_fare_amount = lcv_single;
                    clicked = true;
                    str_class_id = "2";
                    str_tc_class = "LCV/MiniBus";
                }else if(str_journey_type.equals("Return")){
                    textFareAmount.setText(lcv_double);
                    str_fare_amount = lcv_double;
                    clicked = true;
                    str_class_id = "2";
                    str_tc_class = "LCV/MiniBus";
                } else{
                    Toast.makeText(HomeActivity.this, "Please Select Journey Type.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mBtnLcv.setBackgroundResource(R.color.buttonColorDisabled);
                mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                mBtnmav.setBackgroundResource(R.color.colorPrimary);
                mBtnosv.setBackgroundResource(R.color.colorPrimary);
            }
        });

        mBtnBusTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(str_journey_type.equals("Single")){
                    textFareAmount.setText(truck_bus_single);
                    str_fare_amount = truck_bus_single;
                    clicked = true;
                    str_class_id = "3";
                    str_tc_class = "Bus/Truck";
                }else if(str_journey_type.equals("Return")){
                    textFareAmount.setText(truck_bus_double);
                    str_fare_amount = truck_bus_double;
                    clicked = true;
                    str_class_id = "3";
                    str_tc_class = "Bus/Truck";
                } else{
                    Toast.makeText(HomeActivity.this, "Please Select Journey Type.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mBtnBusTrack.setBackgroundResource(R.color.buttonColorDisabled);
                mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                mBtnmav.setBackgroundResource(R.color.colorPrimary);
                mBtnosv.setBackgroundResource(R.color.colorPrimary);
            }
        });

        mBtnmav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(str_journey_type.equals("Single")){
                    textFareAmount.setText(mav_single);
                    str_fare_amount = mav_single;
                    clicked = true;
                    str_class_id = "6";
                    str_tc_class = "MAV";
                }else if(str_journey_type.equals("Return")){
                    textFareAmount.setText(mav_double);
                    str_fare_amount = mav_double;
                    clicked = true;
                    str_class_id = "6";
                    str_tc_class = "MAV";
                } else {
                    Toast.makeText(HomeActivity.this, "Please Select Journey Type.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mBtnmav.setBackgroundResource(R.color.buttonColorDisabled);
                mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                mBtnosv.setBackgroundResource(R.color.colorPrimary);

            }
        });


        mBtnosv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(str_journey_type.equals("Single")){
                    textFareAmount.setText(osv_single);
                    str_fare_amount = osv_single;
                    clicked = true;
                    str_class_id = "10";
                    str_tc_class = "OSV";
                }else if(str_journey_type.equals("Return")){
                    textFareAmount.setText(osv_double);
                    str_fare_amount = osv_double;
                    clicked = true;
                    str_class_id = "10";
                    str_tc_class = "OSV";
                } else{
                    Toast.makeText(HomeActivity.this, "Please Select Journey Type.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mBtnosv.setBackgroundResource(R.color.buttonColorDisabled);
                mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                mBtnmav.setBackgroundResource(R.color.colorPrimary);

            }
        });


        mBtnProcess.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {


                if (clicked == true) {
                    str_shift_number = AppSharedPreference.getShiftNumber(HomeActivity.this);
                    str_supervisor_loginID = AppSharedPreference.getSupervisorLoginId(HomeActivity.this);
                    str_seal_no = AppSharedPreference.getSealNo(HomeActivity.this);
                    try {
                        stopTimer();
                    } catch (Exception e) {

                    }
                    if (spin && journey) {
                    //    printerReadyTask();
                        if (isConnected()) {
                            getTransaction();
                        } else {
                            snakbarHelper();
                            Toast.makeText(HomeActivity.this, "Your Device is not connected to Internet.", Toast.LENGTH_SHORT).show();
                        }


                    //    printerReadyTask();
                    } else {
                        Toast.makeText(HomeActivity.this, "Please Select Payment Mode and Journey Type.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(HomeActivity.this, "Please Select Vehicle Class.", Toast.LENGTH_SHORT).show();
                }

            }
        });


        spinnerAdapter = new SimpleAdapter(this, storedPrintersList, R.layout.stored_printer_entry, storedPrinterAttributeKeys, storedPrinterAttributeIds) {
            @Override
            public void notifyDataSetChanged() {
                DiscoveredPrinter[] printerList = SelectedPrinterManager.getPrinterHistory();
                storedPrintersList.clear();
                for (DiscoveredPrinter selectedPrinter : printerList) {
                    Map<String, String> printerAttributes = new HashMap<String, String>();
                    if (selectedPrinter instanceof DiscoveredPrinterNetwork) {
                        printerAttributes.put(storedPrinterAttributeKeys[0], selectedPrinter.getDiscoveryDataMap().get("DNS_NAME"));
                    } else if (selectedPrinter instanceof DiscoveredPrinterBluetooth) {
                        printerAttributes.put(storedPrinterAttributeKeys[0], ((DiscoveredPrinterBluetooth) selectedPrinter).friendlyName);
                    } else if (selectedPrinter instanceof DiscoveredPrinterUsb) {
                        printerAttributes.put(storedPrinterAttributeKeys[0], "USB Printer");
                    }
                    printerAttributes.put(storedPrinterAttributeKeys[1], selectedPrinter.address);
                    storedPrintersList.add(storedPrintersList.size(), printerAttributes);
                }

                Map<String, String> findPrintersEntry = new HashMap<String, String>();
                findPrintersEntry.put(storedPrinterAttributeKeys[0], "Find Printer...");
                findPrintersEntry.put(storedPrinterAttributeKeys[1], "");
                storedPrintersList.add(storedPrintersList.size(), findPrintersEntry);
                super.notifyDataSetChanged();
            }
        };
        printerSpinner.setAdapter(spinnerAdapter);
        spinnerAdapter.notifyDataSetChanged();

        ((SimpleAdapter) spinnerAdapter).setDropDownViewResource(R.layout.stored_printer_entry);
        printerSpinner.setAdapter(spinnerAdapter);
        printerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                DiscoveredPrinter[] history = SelectedPrinterManager.getPrinterHistory();

                if (arg2 == history.length) {
                    showDialog(DIALOG_DISCOVERY);
                    ((Spinner) findViewById(R.id.main_spinner)).setSelection(0);
                    return;
                } else if (arg2 == 0) {
                    return;
                }

                SelectedPrinterManager.removeHistoryItemAtIndex(arg2);
                SelectedPrinterManager.setSelectedPrinter(history[arg2]);
                spinnerAdapter.notifyDataSetChanged();
                ((Spinner) findViewById(R.id.main_spinner)).setSelection(0);
                SelectedPrinterManager.storePrinterHistoryInPreferences(HomeActivity.this);
                FormatRefresher.execute(HomeActivity.this);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,payment_mode);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinner_payment_mode.setAdapter(aa);

        ArrayAdapter bb = new ArrayAdapter(this,android.R.layout.simple_spinner_item,journey_type);
        bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinner_journey_type.setAdapter(bb);

    }

    private void upiPayment(){

        Rect displayRectangle = new Rect();
        Window window = HomeActivity.this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        final AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this,R.style.CustomAlertDialog);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.customdialog, viewGroup, false);
        dialogView.setMinimumWidth((int)(displayRectangle.width() * 1f));
        dialogView.setMinimumHeight((int)(displayRectangle.height() * 1f));
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        Button buttonCancel=dialogView.findViewById(R.id.cancel);
        Button buttonValidate=dialogView.findViewById(R.id.validate);
        image_qrcode = dialogView.findViewById(R.id.image_qrcode);
        try {
            Bitmap bitmap = encodeAsBitmap(str_QrType);
            image_qrcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this,"Transaction Canceled!",Toast.LENGTH_LONG).show();
                clicked = false;
                textFareAmount.setText("");
                spin = false;
                journey = false;
                mBtnProcess.setBackgroundResource(R.color.colorPrimary);
                mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                mBtnmav.setBackgroundResource(R.color.colorPrimary);
                mBtnosv.setBackgroundResource(R.color.colorPrimary);
                ArrayAdapter aa = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,payment_mode);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                spinner_payment_mode.setAdapter(aa);

                ArrayAdapter bb = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,journey_type);
                bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                spinner_journey_type.setAdapter(bb);
                mBtnProcess.setEnabled(true);

                alertDialog.dismiss();

            }
        });
        buttonValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              //  validate();
                printerReadyTask();
                alertDialog.dismiss();

            }
        });
        alertDialog.show();
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 500, 0, 0, w, h);
        return bitmap;
    }


    private void printerReadyTask() {
        final UIHelper uiHelper= new UIHelper(this);
        new AsyncTask<Void, Boolean, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                uiHelper.showLoadingDialog("Loading...");
                mBtnProcess.setEnabled(false);
                mBtnProcess.setBackgroundResource(R.color.buttonColorDisabled);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                return isPrinterReady();
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                uiHelper.dismissLoadingDialog();
                isReadyStatus = aBoolean;
                if (isReadyStatus) {
                    if (isConnected()) {
                        if (isConnectedHost) {

                              //  processReeipt();
                            validate();

                        } else {
                            Toast.makeText(HomeActivity.this, "Host not connected", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        snakbarHelper();
                    }
                } else {
                    setStatus("Printer Not Connected", Color.RED);
                    Toast.makeText(HomeActivity.this, "Printer Not Connected", Toast.LENGTH_SHORT).show();
                    clicked = false;
                    textFareAmount.setText("");
                    spin = false;
                    journey = false;
                    mBtnProcess.setBackgroundResource(R.color.colorPrimary);
                    mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                    mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                    mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                    mBtnmav.setBackgroundResource(R.color.colorPrimary);
                    mBtnosv.setBackgroundResource(R.color.colorPrimary);
                    ArrayAdapter aa = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,payment_mode);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_payment_mode.setAdapter(aa);

                    ArrayAdapter bb = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,journey_type);
                    bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_journey_type.setAdapter(bb);
                    mBtnProcess.setEnabled(true);
                }

            }
        }.execute();
    }


    private void snakbarHelper() {
        Snackbar snackbar = Snackbar.make(homeLayout, "No internet connection", Snackbar.LENGTH_LONG);
        final ViewGroup.LayoutParams params = snackbar.getView().getLayoutParams();
        if (params instanceof CoordinatorLayout.LayoutParams) {
            ((CoordinatorLayout.LayoutParams) params).gravity = Gravity.TOP | Gravity.CENTER;
        } else {
            ((FrameLayout.LayoutParams) params).gravity = Gravity.TOP;
        }
        snackbar.getView().setLayoutParams(params);
        snackbar.show();
    }


    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(new Date());
    }

    public boolean isConnected() {
        return NetworkUtils.isNetworkConnected(this);
    }


    protected void logoutOpenActivity() {
        Intent intent = new Intent(this, ScreenLoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onActivityResult(int mRequestCode, int mResultCode,
                                 Intent mDataIntent) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent);

    }

    @Override
    protected void scanBarCodeResult(String result) {
        Log.i(TAG, result);
    }


    private List<Map<String, String>> storedPrintersList = new ArrayList<Map<String, String>>();
    String[] storedPrinterAttributeKeys = new String[]{"printer_name", "printer_address"};
    int[] storedPrinterAttributeIds = new int[]{R.id.storedPrinterName, R.id.storedPrinterAddress};

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
//                Toast.makeText(this, "User Logot", Toast.LENGTH_SHORT).show();
                logoutTask(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logoutTask(final MenuItem item) {
        final UIHelper uiHelper = new UIHelper(this);
        new AsyncTask<Void, Boolean, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                uiHelper.showLoadingDialog("Please wait logout...");
                item.setEnabled(false); // hide logout button fix issues multiple times logout
                item.setVisible(false);
                mBtnProcess.setEnabled(false);
                mBtnProcess.setBackgroundResource(R.color.buttonColorDisabled);

            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                return isPrinterReady();
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                item.setEnabled(true); // show logout button
                item.setVisible(true);
                uiHelper.dismissLoadingDialog();
                if (aBoolean) {
                    if (isConnected()) {
                        logout();
                    } else {
                        Toast.makeText(HomeActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Printer Not Connected", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }



    @Override
    protected void printerUI() {
        setStatus("Printer Connected", Color.parseColor("#12786c"));
    }

    @Override
    protected void serverStatus() {
        serverStatusAPI();
    }

    @Override
    protected void openNextActivity() {
//        logoutOpenActivity();
    }

    @Override
    protected void processButtonUIShow() {
      }



    private void serverStatusAPI() {
        AppServiceAPI serviceAPI = MetroTMSApp.createRetrofit(this);
        Call<ServerModel> userModelCall = serviceAPI.getServerStatus();
        userModelCall.enqueue(new Callback<ServerModel>() {
            @Override
            public void onResponse(Call<ServerModel> call, Response<ServerModel> response) {
                if (response.body() == null) {
                    Log.e("home", "null");
                    return;
                }

                if (response.body().getStatus()) {
                    isConnectedHost = true;
                    status = AppSharedPreference.getServerStatus(HomeActivity.this);
                    AppSharedPreference.setServerStatus(HomeActivity.this, response.body().getStatus());
                    toolbar.setBackgroundResource(R.color.colorPrimary);
                    if (isReadyStatus) {
                        mBtnProcess.setEnabled(true);
                        mBtnProcess.setBackgroundResource(R.color.colorPrimary);
                        mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                        mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                        mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                        mBtnmav.setBackgroundResource(R.color.colorPrimary);
                        mBtnosv.setBackgroundResource(R.color.colorPrimary);
                    }
                    Log.e("home", "true");
                } else {
                    isConnectedHost = false;
                    status = AppSharedPreference.getServerStatus(HomeActivity.this);
                    AppSharedPreference.setServerStatus(HomeActivity.this, response.body().getStatus());
                    toolbar.setBackgroundResource(R.color.red);
                    mBtnProcess.setEnabled(false);
                    mBtnProcess.setBackgroundResource(R.color.buttonColorDisabled);
                    Log.e("home", "false");
                    // do nothing
                }
            }

            @Override
            public void onFailure(Call<ServerModel> call, Throwable t) {
                Log.e("home", "onFailure");
                status = AppSharedPreference.getServerStatus(HomeActivity.this);
                AppSharedPreference.setServerStatus(HomeActivity.this, false);
                toolbar.setBackgroundResource(R.color.red);
                mBtnProcess.setEnabled(false);
                mBtnProcess.setBackgroundResource(R.color.buttonColorDisabled);
                isConnectedHost = false;
            }
        });
    }

    private void fareDetailsAPI() {
        final UIHelper uiHelper = new UIHelper(this);
        uiHelper.showLoadingDialog("Loading...");
        AppServiceAPI serviceAPI = MetroTMSApp.createRetrofit(this);
        Call<GetFareDetailsAPI> userModelCall = serviceAPI.getFare();
        userModelCall.enqueue(new Callback<GetFareDetailsAPI>() {
            @Override
            public void onResponse(Call<GetFareDetailsAPI> call, Response<GetFareDetailsAPI> response) {
                uiHelper.dismissLoadingDialog();
                if (response.body() == null) {
                    Log.e("home", "null");
                    return;
                }

                if (response.body().getStatus()) {
                    Log.e("home", "true");
                        car_single = response.body().getData().get(1).getAmount();
                        lcv_single = response.body().getData().get(2).getAmount();
                        truck_bus_single = response.body().getData().get(3).getAmount();
                        mav_single = response.body().getData().get(6).getAmount();
                        osv_single = response.body().getData().get(10).getAmount();
                    car_double = response.body().getData().get(16).getAmount();
                    lcv_double = response.body().getData().get(17).getAmount();
                    truck_bus_double = response.body().getData().get(18).getAmount();
                    mav_double = response.body().getData().get(21).getAmount();
                    osv_double = response.body().getData().get(25).getAmount();

                } else {
                    // do nothing
                }
            }

            @Override
            public void onFailure(Call<GetFareDetailsAPI> call, Throwable t) {
                uiHelper.dismissLoadingDialog();
                Log.e("home", "onFailure");
            }
        });
    }


    private void getTransaction() {
        final UIHelper uiHelper = new UIHelper(this);
        uiHelper.showLoadingDialog("Loading...");
        mBtnProcess.setEnabled(false);
        mBtnProcess.setBackgroundResource(R.color.buttonColorDisabled);
        AppServiceAPI serviceAPI = MetroTMSApp.createRetrofit(this);
        laneNumber = AppSharedPreference.getLaneNumber(this);
        receiptNumber = MetroTMSApp.getPreparedReceiptNumber();
        Call<GetTransactionIdAPI> userModelCall = serviceAPI.getTransactoinId(str_supervisor_loginID, str_shift_number,
                str_fare_amount,String.valueOf(laneNumber),String.valueOf("0"), str_seal_no, str_class_id,str_payment_mode);
//        Call<LaneProcessModel> userModelCall = serviceAPI.getTransactoinId(String.valueOf(laneNumber), str_plaza_id,
//                String.valueOf("0"), str_shift_number, str_supervisor_loginID, str_class_id, str_seal_no);

        userModelCall.enqueue(new Callback<GetTransactionIdAPI>() {
            @Override
            public void onResponse(Call<GetTransactionIdAPI> call, Response<GetTransactionIdAPI> response) {
                uiHelper.dismissLoadingDialog();
                Log.e(TAG, "url: " + call.request().url());
                if (response.body() == null) {
                    textFareAmount.setText("");
                    mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                    mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                    mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                    mBtnProcess.setBackgroundResource(R.color.colorPrimary);
                    mBtnmav.setBackgroundResource(R.color.colorPrimary);
                    mBtnosv.setBackgroundResource(R.color.colorPrimary);
                    ArrayAdapter aa = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,payment_mode);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_payment_mode.setAdapter(aa);

                    ArrayAdapter bb = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,journey_type);
                    bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_journey_type.setAdapter(bb);
                    mBtnProcess.setEnabled(true);
                    networkfailurepopup("Server Error contact Administrator");
                    return;
                }

                if (response.body().getStatus()) {

                    if(response.body().getData().getQrType().isEmpty()){

                        str_fare_amount = response.body().getData().getAmount();
                        str_transactionId = response.body().getData().getTransactionId();

                    //    Toast.makeText(HomeActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                     //   clicked = true;
                     //   spin = false;

                      // validate();
                        printerReadyTask();
                    } else {
                        str_fare_amount = response.body().getData().getAmount();
                        str_transactionId = response.body().getData().getTransactionId();
                        str_currency_code = response.body().getData().getCurrencyCode();
                        str_merchant_id = response.body().getData().getMerchantId();
                        str_description = response.body().getData().getDescription();
                        str_valid_duration = response.body().getData().getValidDuration();
                        str_QrType = response.body().getData().getQrType();

                        upiPayment();

                      //  createQR();
                    }



                } else {
                    mBtnProcess.setEnabled(true);
                    mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                    mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                    mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                    mBtnProcess.setBackgroundResource(R.color.colorPrimary);
                    mBtnmav.setBackgroundResource(R.color.colorPrimary);
                    mBtnosv.setBackgroundResource(R.color.colorPrimary);
                    ArrayAdapter aa = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,payment_mode);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_payment_mode.setAdapter(aa);

                    ArrayAdapter bb = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,journey_type);
                    bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_journey_type.setAdapter(bb);
                  //  Toast.makeText(HomeActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();

                    // do nothing
                }

            }

            @Override
            public void onFailure(Call<GetTransactionIdAPI> call, Throwable t) {
                mBtnProcess.setEnabled(true);
                mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                mBtnProcess.setBackgroundResource(R.color.colorPrimary);
                mBtnmav.setBackgroundResource(R.color.colorPrimary);
                mBtnosv.setBackgroundResource(R.color.colorPrimary);
                ArrayAdapter aa = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,payment_mode);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                spinner_payment_mode.setAdapter(aa);

                ArrayAdapter bb = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,journey_type);
                bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                spinner_journey_type.setAdapter(bb);
                networkfailurepopup("Host IP" + AppSharedPreference.getHostName(HomeActivity.this) + " is not unreachable");
                uiHelper.dismissLoadingDialog();
                startTimer();

            }
        });
    }


    private void processReeipt() {
        final UIHelper uiHelper = new UIHelper(this);
        uiHelper.showLoadingDialog("Loading...");
        mBtnProcess.setEnabled(false);
        mBtnProcess.setBackgroundResource(R.color.buttonColorDisabled);
        AppServiceAPI serviceAPI = MetroTMSApp.createRetrofit(this);
        final int laneNumber = AppSharedPreference.getLaneNumber(this);
        final int receiptNumber = MetroTMSApp.getPreparedReceiptNumber();
        Call<LaneProcessModel> userModelCall = serviceAPI.laneprocess(String.valueOf(laneNumber), str_plaza_id,
                String.valueOf("0"), str_shift_number, str_supervisor_loginID, str_class_id, str_seal_no);

        userModelCall.enqueue(new Callback<LaneProcessModel>() {
            @Override
            public void onResponse(Call<LaneProcessModel> call, Response<LaneProcessModel> response) {
                uiHelper.dismissLoadingDialog();
                Log.e(TAG, "url: " + call.request().url());
                if (response.body() == null) {
                    networkfailurepopup("Server Error contact Administrator");
                    return;
                }

                if (response.body().getStatus()) {
                    String plazaName = "KM-42 Toll Plaza Name", tcName = AppSharedPreference.getLoginId(HomeActivity.this),
                            journeyType = "Single Journey";
                    textFareAmount.setText("");
                    str_fare_amount = response.body().getData().getFareAmount();
                    luhncode = response.body().getData().getLuhncode();

                   // Toast.makeText(HomeActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                    mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                    mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                    mBtnmav.setBackgroundResource(R.color.colorPrimary);
                    mBtnosv.setBackgroundResource(R.color.colorPrimary);

                    printReceipt(plazaName, String.valueOf(receiptNumber), tcName, String.valueOf(laneNumber), journeyType,
                            str_tc_class, str_fare_amount, luhncode, "laneReceipt", "");


                } else {
                    mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                    mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                    mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                    mBtnmav.setBackgroundResource(R.color.colorPrimary);
                    mBtnosv.setBackgroundResource(R.color.colorPrimary);
                    Toast.makeText(HomeActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();

                    // do nothing
                }

            }

            @Override
            public void onFailure(Call<LaneProcessModel> call, Throwable t) {
                networkfailurepopup("Host IP" + AppSharedPreference.getHostName(HomeActivity.this) + " is not unreachable");
                uiHelper.dismissLoadingDialog();
                startTimer();

            }
        });
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    private void logout() {
        final UIHelper uiHelper = new UIHelper(this);
        uiHelper.showLoadingDialog("Logout Please wait...");
        String laneID = String.valueOf(AppSharedPreference.getLaneNumber(this));
        String plazaID = AppSharedPreference.getPlazaId(this);
        String sealNo = AppSharedPreference.getSealNo(this);
        AppServiceAPI serviceAPI = MetroTMSApp.createRetrofit(this);
        Call<LogoutModel> userModelCall = serviceAPI.logout(laneID, plazaID, sealNo);
        userModelCall.enqueue(new Callback<LogoutModel>() {
            @Override
            public void onResponse(Call<LogoutModel> call, Response<LogoutModel> response) {
                if (response.body() == null) {
                    networkfailurepopup("Please contact server team.");
                    return;
                }

                if (response.body().getStatus()) {
                    AppSharedPreference.setAuth(HomeActivity.this, false);

                    printReceipt("SEL Toll Plaza Name", "", AppSharedPreference.getLoginId(HomeActivity.this),
                            String.valueOf(AppSharedPreference.getLaneNumber(HomeActivity.this)), "",
                            "", "", "", "logout", AppSharedPreference.getSealNo(HomeActivity.this));
                    // TODO
                    logoutOpenActivity();
                } else {
                    AppSharedPreference.setAuth(HomeActivity.this, true);
                    Toast.makeText(HomeActivity.this, "Please contact System Administrator", Toast.LENGTH_SHORT).show();
                }
                uiHelper.dismissLoadingDialog();

            }

            @Override
            public void onFailure(Call<LogoutModel> call, Throwable t) {
                uiHelper.dismissLoadingDialog();
                networkfailurepopup("Server is not respond.");
            }
        });
    }

    private void validate() {
        final UIHelper uiHelper = new UIHelper(this);
        uiHelper.showLoadingDialog("Payment Validating Please wait...");
        AppServiceAPI serviceAPI = MetroTMSApp.createRetrofit(this);
        Call<TransactionValidateAPI> userModelCall = serviceAPI.validateTransaction(str_transactionId, str_payment_mode);
        userModelCall.enqueue(new Callback<TransactionValidateAPI>() {
            @Override
            public void onResponse(Call<TransactionValidateAPI> call, Response<TransactionValidateAPI> response) {
                if (response.body() == null) {
                    textFareAmount.setText("");
                    mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                    mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                    mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                    mBtnProcess.setBackgroundResource(R.color.colorPrimary);
                    mBtnmav.setBackgroundResource(R.color.colorPrimary);
                    mBtnosv.setBackgroundResource(R.color.colorPrimary);
                    ArrayAdapter aa = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,payment_mode);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_payment_mode.setAdapter(aa);

                    ArrayAdapter bb = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,journey_type);
                    bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_journey_type.setAdapter(bb);
                    mBtnProcess.setEnabled(true);
                    networkfailurepopup("Please contact server team.");
                    uiHelper.dismissLoadingDialog();
                    return;
                }

                if (response.body().getStatus()) {

                    textFareAmount.setText("");
                    mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                    mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                    mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                    mBtnProcess.setBackgroundResource(R.color.colorPrimary);
                    mBtnmav.setBackgroundResource(R.color.colorPrimary);
                    mBtnosv.setBackgroundResource(R.color.colorPrimary);
                    ArrayAdapter aa = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,payment_mode);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_payment_mode.setAdapter(aa);

                    ArrayAdapter bb = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,journey_type);
                    bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_journey_type.setAdapter(bb);
                    mBtnProcess.setEnabled(true);

                    if(str_journey_type.equals("Single")){
                        String plazaName = "SIMHAPURI EXPRESSWAY LIMITED TP2", tcName = AppSharedPreference.getLoginId(HomeActivity.this),
                                journeyType = "Single Journey";

                        printReceipt(plazaName, String.valueOf(str_transactionId), tcName, String.valueOf(laneNumber), journeyType,
                                str_tc_class, str_fare_amount, str_transactionId, "laneReceipt", "");
                    } if (str_journey_type.equals("Return")){
                        String plazaName = "SIMHAPURI EXPRESSWAY LIMITED TP2", tcName = AppSharedPreference.getLoginId(HomeActivity.this),
                                journeyType = "Return Journey";

                        printReceipt(plazaName, String.valueOf(str_transactionId), tcName, String.valueOf(laneNumber), journeyType,
                                str_tc_class, str_fare_amount, str_transactionId, "laneReceipt", "");
                    }

                } else {

                    textFareAmount.setText("");
                    mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                    mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                    mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                    mBtnProcess.setBackgroundResource(R.color.colorPrimary);
                    mBtnmav.setBackgroundResource(R.color.colorPrimary);
                    mBtnosv.setBackgroundResource(R.color.colorPrimary);
                    ArrayAdapter aa = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,payment_mode);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_payment_mode.setAdapter(aa);

                    ArrayAdapter bb = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,journey_type);
                    bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Setting the ArrayAdapter data on the Spinner
                    spinner_journey_type.setAdapter(bb);
                    mBtnProcess.setEnabled(true);
                    networkfailurepopup("Transaction Failed!");
                  //  Toast.makeText(HomeActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    uiHelper.dismissLoadingDialog();
                }
                uiHelper.dismissLoadingDialog();

            }

            @Override
            public void onFailure(Call<TransactionValidateAPI> call, Throwable t) {
                uiHelper.dismissLoadingDialog();
                textFareAmount.setText("");
                mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                mBtnProcess.setBackgroundResource(R.color.colorPrimary);
                mBtnmav.setBackgroundResource(R.color.colorPrimary);
                mBtnosv.setBackgroundResource(R.color.colorPrimary);
                ArrayAdapter aa = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,payment_mode);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                spinner_payment_mode.setAdapter(aa);

                ArrayAdapter bb = new ArrayAdapter(HomeActivity.this,android.R.layout.simple_spinner_item,journey_type);
                bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                spinner_journey_type.setAdapter(bb);
                mBtnProcess.setEnabled(true);
                networkfailurepopup("Server is not respond.");
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


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        int pos = spinner_payment_mode.getSelectedItemPosition();
        int pos1 = spinner_journey_type.getSelectedItemPosition();

        if(pos == 1){
            str_payment_mode = "Cash";
            spin = true;
       //     Toast.makeText(HomeActivity.this,str_payment_mode,Toast.LENGTH_LONG).show();
        }else if(pos == 2){
            str_payment_mode = "UPI";
            spin = true;
          //  Toast.makeText(HomeActivity.this,str_payment_mode,Toast.LENGTH_LONG).show();
        } else{
            spin = false;
            clicked = false;
        }

        if(pos1 == 1){
            if (isConnected()) {
                str_journey_type = "Single";
                journey = true;
                mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                mBtnmav.setBackgroundResource(R.color.colorPrimary);
                mBtnosv.setBackgroundResource(R.color.colorPrimary);
                textFareAmount.setText("");
                fareDetailsAPI();
            } else {
                snakbarHelper();
                Toast.makeText(HomeActivity.this, "Your Device is not connected to Internet.", Toast.LENGTH_SHORT).show();
            }

          //  spinner_journey_type.setEnabled(false);
          //  Toast.makeText(HomeActivity.this,str_journey_type,Toast.LENGTH_LONG).show();
        } else if(pos1 == 2){
            if (isConnected()) {
                str_journey_type = "Return";
                journey = true;
                mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
                mBtnLcv.setBackgroundResource(R.color.colorPrimary);
                mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
                mBtnmav.setBackgroundResource(R.color.colorPrimary);
                mBtnosv.setBackgroundResource(R.color.colorPrimary);
                textFareAmount.setText("");
                fareDetailsAPI();
            } else {
                snakbarHelper();
                Toast.makeText(HomeActivity.this, "Your Device is not connected to Internet.", Toast.LENGTH_SHORT).show();
            }

          //  spinner_journey_type.setEnabled(false);
          //  Toast.makeText(HomeActivity.this,str_journey_type,Toast.LENGTH_LONG).show();
        } else{
            str_journey_type = "";
            journey = false;
           // spinner_journey_type.setEnabled(true);
            mBtnCarJeep.setBackgroundResource(R.color.colorPrimary);
            mBtnLcv.setBackgroundResource(R.color.colorPrimary);
            mBtnBusTrack.setBackgroundResource(R.color.colorPrimary);
            mBtnmav.setBackgroundResource(R.color.colorPrimary);
            mBtnosv.setBackgroundResource(R.color.colorPrimary);
            textFareAmount.setText("");
          //  Toast.makeText(HomeActivity.this,str_journey_type,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}


