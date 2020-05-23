package com.metroinfrasys.metrotms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.metroinfrasys.metrotms.api.AppServiceAPI;
import com.metroinfrasys.metrotms.data.server.ServerModel;
import com.metroinfrasys.metrotms.data.supervisor.SupervisorModel;
import com.metroinfrasys.metrotms.ui.BaseActivity;
import com.metroinfrasys.metrotms.utils.AppSharedPreference;
import com.metroinfrasys.metrotms.utils.FormatRefresher;
import com.metroinfrasys.metrotms.utils.SelectedPrinterManager;
import com.metroinfrasys.metrotms.utils.UIHelper;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterNetwork;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupervisorActivity extends BaseActivity implements View.OnClickListener {

    private Button validateBtn, cancelBtn;
    EditText loginIdEditText, passwordEditText;
    TextView serverStatusTextView;
    View supervisorLayout;

    String str_plaza_id, str_seal_no, laneid;
    private AlertDialog dialog;
    boolean status;
    private android.support.v7.widget.Toolbar toolbar;


    private List<Map<String, String>> storedPrintersList = new ArrayList<Map<String, String>>();
    String[] storedPrinterAttributeKeys = new String[]{"printer_name", "printer_address"};
    int[] storedPrinterAttributeIds = new int[]{R.id.storedPrinterName, R.id.storedPrinterAddress};
    private boolean isReadyStatus;
    private boolean isSupervisorLogin;
    private MyPrinterStatus printerStatus;


    @Override
    protected void logoutOpenActivity() {

    }

    @Override
    protected void scanBarCodeResult(String result) {

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_supervisor;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supervisorLayout = findViewById(R.id.supervisorLayout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loginIdEditText = findViewById(R.id.login_id);
        progressCircular = findViewById(R.id.progressCircular);
        passwordEditText = findViewById(R.id.password);
        validateBtn = findViewById(R.id.process);
        cancelBtn = findViewById(R.id.cancel);
        validateBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        if (isReadyStatus) {
            if (isConnected()) {
                serverStatusAPI();
            } else {
                snakbarHelper();
                validateBtn.setEnabled(false);
                validateBtn.setBackgroundResource(R.color.buttonColorDisabled);
            }
        } else {
  //          setStatus("Printer not connected", Color.RED);
            Toast.makeText(this, "Printer not connected", Toast.LENGTH_SHORT).show();
        }


        status = AppSharedPreference.getServerStatus(SupervisorActivity.this);

        Spinner printerSpinner = (Spinner) findViewById(R.id.main_spinner);

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
                SelectedPrinterManager.storePrinterHistoryInPreferences(SupervisorActivity.this);
                FormatRefresher.execute(SupervisorActivity.this);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.process:
                loginIdEditText.setError(null);
                passwordEditText.setError(null);
                if (isValid(loginIdEditText.getText().toString(), passwordEditText.getText().toString())) {
                    if (isConnected()) {
                        str_plaza_id = AppSharedPreference.getPlazaId(this);
                        str_seal_no = AppSharedPreference.getSealNo(this);
                        laneid = String.valueOf(AppSharedPreference.getLaneNumber(this));
                        supervisorLoginTask();
                    } else {
                        snakbarHelper();
                    }
                } else {
                    loginIdEditText.setError("Enter Login ID");
                    passwordEditText.setError("Enter Password");
                }
                break;
            case R.id.cancel:
                finish();
                break;
            default:
                break;
        }
    }

    private void supervisorLoginTask() {
        final UIHelper uiHelper = new UIHelper(this);
        new AsyncTask<Void, Boolean, Boolean>() {

            @Override
            protected void onPreExecute() {
                uiHelper.showLoadingDialog("Loading...");
                validateBtn.setEnabled(false);
                validateBtn.setBackgroundResource(R.color.buttonColorDisabled);
            }

            @Override
            protected Boolean doInBackground(Void... strings) {
                return isPrinterReady();
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                uiHelper.dismissLoadingDialog();
                if (aBoolean) {
                    supervisorLoginAPI(loginIdEditText.getText().toString(), passwordEditText.getText().toString(), str_plaza_id, laneid, str_seal_no);
                } else {
                    Toast.makeText(SupervisorActivity.this, "Printer Not Connected", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void supervisorLoginAPI(final String loginID, String password, String plazaId, String laneId, String sealNo) {
//        setLoading();
        final UIHelper uiHelper = new UIHelper(this);
        uiHelper.showLoadingDialog("Loading...");
        AppServiceAPI serviceAPI = MetroTMSApp.createRetrofit(this);
        Call<SupervisorModel> userModelCall = serviceAPI.supervisorLogin(loginID, password, sealNo, "true", laneId, plazaId);
        userModelCall.enqueue(new Callback<SupervisorModel>() {
            @Override
            public void onResponse(Call<SupervisorModel> call, Response<SupervisorModel> response) {
                if (uiHelper != null && uiHelper.isDialogActive())
                    uiHelper.dismissLoadingDialog();
                if (response.body() == null) {
                    networkfailurepopup("Please contact server team.");
                    hideLoading();
                    validateBtn.setEnabled(true);
                    validateBtn.setBackgroundResource(R.color.colorPrimary);
                    return;
                }

                if (response.body().getStatus()) {
                    AppSharedPreference.setAuth(SupervisorActivity.this, true);
                    AppSharedPreference.setSupervisorLoginStatus(SupervisorActivity.this,true);
                    AppSharedPreference.setShiftNumber(SupervisorActivity.this, response.body().getData().getShiftNumber());
                    AppSharedPreference.setSupervisorLoginId(SupervisorActivity.this, loginID);
                    printReceipt("SIMHAPURI EXPRESSWAY LIMITED", "", AppSharedPreference.getLoginId(SupervisorActivity.this),
                            String.valueOf(AppSharedPreference.getLaneNumber(SupervisorActivity.this)), "",
                            "", "", "", "login", AppSharedPreference.getSealNo(SupervisorActivity.this));
                    authorizationSupervisorLoginActivity();
   //                 validateBtn.setEnabled(true);
   //                 validateBtn.setBackgroundResource(R.color.colorPrimary);
                } else {
                    AppSharedPreference.setAuth(SupervisorActivity.this, false);
                    Toast.makeText(SupervisorActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                }
                hideLoading();
            }

            @Override
            public void onFailure(Call<SupervisorModel> call, Throwable t) {
                networkfailurepopup("Server is not respond.");
                hideLoading();
                validateBtn.setEnabled(true);
                validateBtn.setBackgroundResource(R.color.colorPrimary);
            }
        });
    }


    public void networkfailurepopup(String message) {

        if (!this.isFinishing()) {
            //show dialog
            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this, R.style.AppTheme_Dialog);
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

    private void authorizationSupervisorLoginActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void snakbarHelper() {
        Snackbar snackbar = Snackbar.make(supervisorLayout, "No internet connection", Snackbar.LENGTH_LONG);
        final ViewGroup.LayoutParams params = snackbar.getView().getLayoutParams();
        if (params instanceof CoordinatorLayout.LayoutParams) {
            ((CoordinatorLayout.LayoutParams) params).gravity = Gravity.TOP | Gravity.CENTER;
        } else {
            ((FrameLayout.LayoutParams) params).gravity = Gravity.TOP;
        }
        snackbar.getView().setLayoutParams(params);
        snackbar.show();
    }

    private boolean isValid(String user, String password) {
        if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(password))
            return true;
        return false;
    }


    @Override
    protected void printerUI() {
        toolbar.setBackgroundResource(R.color.colorPrimary);
        validateBtn.setEnabled(true);
        validateBtn.setBackgroundResource(R.color.colorPrimary);
    }

    @Override
    protected void serverStatus() {
        serverStatusAPI();
    }

    @Override
    protected void openNextActivity() {
//        authorizationSupervisorLoginActivity();
    }

    @Override
    protected void processButtonUIShow() {
 //       validateBtn.setEnabled(true);
  //      validateBtn.setBackgroundResource(R.color.colorPrimary);
    }


    private void serverStatusAPI() {
        AppServiceAPI serviceAPI = MetroTMSApp.createRetrofit(this);
        Call<ServerModel> userModelCall = serviceAPI.getServerStatus();

        userModelCall.enqueue(new Callback<ServerModel>() {
            @Override
            public void onResponse(Call<ServerModel> call, Response<ServerModel> response) {
                if (response.body() == null) {
                    return;
                }

                if (response.body().getStatus()) {
                    status = AppSharedPreference.getServerStatus(SupervisorActivity.this);
                    AppSharedPreference.setServerStatus(SupervisorActivity.this, response.body().getStatus());
                    toolbar.setBackgroundResource(R.color.colorPrimary);
                    if (isReadyStatus) {
                        validateBtn.setEnabled(true);
                        validateBtn.setBackgroundResource(R.color.colorPrimary);
                        cancelBtn.setBackgroundResource(R.color.colorPrimary);
                    }
                } else {
                    status = AppSharedPreference.getServerStatus(SupervisorActivity.this);
                    AppSharedPreference.setServerStatus(SupervisorActivity.this, response.body().getStatus());
                    toolbar.setBackgroundResource(R.color.red);
                    validateBtn.setEnabled(false);
                    validateBtn.setBackgroundResource(R.color.buttonColorDisabled);
                    // do nothing
                }
            }

            @Override
            public void onFailure(Call<ServerModel> call, Throwable t) {
                toolbar.setBackgroundResource(R.color.red);
                validateBtn.setEnabled(false);
                validateBtn.setBackgroundResource(R.color.buttonColorDisabled);
            }
        });
    }
}
