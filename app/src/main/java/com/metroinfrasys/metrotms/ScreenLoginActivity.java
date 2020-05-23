package com.metroinfrasys.metrotms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.metroinfrasys.metrotms.api.AppServiceAPI;
import com.metroinfrasys.metrotms.data.login.LoginModel;
import com.metroinfrasys.metrotms.data.server.ServerModel;
import com.metroinfrasys.metrotms.ui.BaseActivity;
import com.metroinfrasys.metrotms.utils.AppSharedPreference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenLoginActivity extends BaseActivity implements View.OnClickListener {

    private Button loginBtn, cancelBtn;
    private EditText loginIDEditText, passwordEditText, sealNumberEditText;
    private AlertDialog dialog;
    View mainLayout;
    private TextView messageTextView;
    boolean status;
    private Toolbar toolBar;
    private boolean isReadyStatus;
    private MyPrinterStatus printerStatus;

    @Override
    protected void printerUI() {
        toolBar.setBackgroundResource(R.color.colorPrimary);
        loginBtn.setBackgroundResource(R.color.colorPrimary);
    }

    @Override
    protected void serverStatus() {
        serverStatusAPI();
    }

    @Override
    protected void openNextActivity() {

    }

    @Override
    protected void processButtonUIShow() {

    }



    @Override
    protected void logoutOpenActivity() {

    }

    @Override
    protected void scanBarCodeResult(String result) {

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_screen_login;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_screen_login);
        toolBar =findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        mainLayout = findViewById(R.id.mainLayout);
        loginBtn = findViewById(R.id.login);
        cancelBtn = findViewById(R.id.cancel);
        loginIDEditText = findViewById(R.id.loginID);
        passwordEditText = findViewById(R.id.password);
        sealNumberEditText = findViewById(R.id.sealno);
        progressCircular = findViewById(R.id.progressCircular);
        messageTextView = findViewById(R.id.message);
        hideLoading();
        loginBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        AppSharedPreference.setOperatorLoginStatus(ScreenLoginActivity.this,false);

        if (isReadyStatus){
            setStatus("Printer Ready", Color.parseColor("#12786c"));
            if(isConnected()){
                serverStatusAPI();
            } else {
                snakbarHelper();
            }
        }  else {
            setStatus("Printer not connected", Color.RED);
            Toast.makeText(this, "Printer not connected", Toast.LENGTH_SHORT).show();
        }
        startTimer();
        status = AppSharedPreference.getServerStatus(ScreenLoginActivity.this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_user_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                openSettinsActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void openSettinsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login: {
                hideKeyboard();
                messageTextView.setVisibility(View.INVISIBLE);
                messageTextView.setText("");
                loginIDEditText.setError(null);
                passwordEditText.setError(null);
                sealNumberEditText.setError(null);
                if (isValid(loginIDEditText.getText().toString(), passwordEditText.getText().toString(), sealNumberEditText.getText().toString())) {
                    if (isConnected()) {
                        String laneID = String.valueOf(AppSharedPreference.getLaneNumber(ScreenLoginActivity.this));
                        loginAPI(loginIDEditText.getText().toString(), passwordEditText.getText().toString(),"true", laneID, sealNumberEditText.getText().toString());
                    } else {
                        snakbarHelper();
                        Toast.makeText(ScreenLoginActivity.this, "Your Device is not connected to Internet.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    loginIDEditText.setError("Enter Login ID");
                    passwordEditText.setError("Enter Password");
                    sealNumberEditText.setError("Enter Seal No");
                }

            }
            break;
            case R.id.cancel:
                finish();
                break;
            default:
                break;
        }
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

    private void authorizationSupervisorLoginActivity() {
        Intent intent = new Intent(this, SupervisorActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isValid(String user, String password, String sealNumber) {
        if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(sealNumber))
            return true;
        return false;
    }

    private void loginAPI(String userID, String password, String toLane, String laneId, String sealNumber) {
        setLoading();
        AppServiceAPI serviceAPI = MetroTMSApp.createRetrofit(this);
        Call<LoginModel> userModelCall = serviceAPI.login(userID, password,toLane,laneId, sealNumber);
        userModelCall.enqueue(new Callback<LoginModel>() {
            @Override
            public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                if (response.body() == null) {
                    networkfailurepopup("Please contact server team.");
                    hideLoading();
                    return;
                }

                if (response.body().getStatus()) {
                    AppSharedPreference.setAuth(ScreenLoginActivity.this,true);
                    AppSharedPreference.setLoginId(ScreenLoginActivity.this,response.body().getData().getLoginID());
                    AppSharedPreference.setPlazaId(ScreenLoginActivity.this,Integer.toString(response.body().getData().getPlazaID()));
                    AppSharedPreference.setSealNo(ScreenLoginActivity.this,response.body().getData().getSealNo());
                    AppSharedPreference.setOperatorLoginStatus(ScreenLoginActivity.this,true);
                    authorizationSupervisorLoginActivity();
                } else {
                    AppSharedPreference.setAuth(ScreenLoginActivity.this,false);
                    messageTextView.setVisibility(View.VISIBLE);
                    messageTextView.setText(response.body().getMessage());
                }
                hideLoading();
            }

            @Override
            public void onFailure(Call<LoginModel> call, Throwable t) {
                networkfailurepopup("Host ip "+AppSharedPreference.getHostName(ScreenLoginActivity.this)+" not working");
                hideLoading();
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
                    status = AppSharedPreference.getServerStatus(ScreenLoginActivity.this);
                    AppSharedPreference.setServerStatus(ScreenLoginActivity.this,response.body().getStatus());
                    toolBar.setBackgroundResource(R.color.colorPrimary);
                    serverOnlineStatus(true);
                    loginBtn.setEnabled(true);
                    loginBtn.setBackgroundResource(R.color.colorPrimary);
                    cancelBtn.setBackgroundResource(R.color.colorPrimary);

                } else {
                    status = AppSharedPreference.getServerStatus(ScreenLoginActivity.this);
                    AppSharedPreference.setServerStatus(ScreenLoginActivity.this,response.body().getStatus());
                    toolBar.setBackgroundResource(R.color.red);
                    loginBtn.setEnabled(false);
                    serverOnlineStatus(true);
                    loginBtn.setBackgroundResource(R.color.buttonColorDisabled);
                    cancelBtn.setBackgroundResource(R.color.colorPrimary);
                    // do nothing
                }
            }

            @Override
            public void onFailure(Call<ServerModel> call, Throwable t) {
                serverOnlineStatus(true);
                toolBar.setBackgroundResource(R.color.red);
            }
        });
    }
}
