package com.metroinfrasys.metrotms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.metroinfrasys.metrotms.utils.AppConstants;
import com.metroinfrasys.metrotms.utils.AppSharedPreference;


public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private AppSharedPreference mSharedPreference;
    EditText hostValue;
    EditText portValue;
    EditText laneValue;
    Button saveSettingBtn, editSettingsBtn;

    TextView messageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        hostValue = findViewById(R.id.hostValue);
        portValue = findViewById(R.id.portValue);
        laneValue = findViewById(R.id.laneValue);
        messageTextView = findViewById(R.id.messageTextView);
        saveSettingBtn = findViewById(R.id.saveSettingsBtn);
        editSettingsBtn = findViewById(R.id.editSettingsBtn);

        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start,
                                       int end, Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) +
                            source.subSequence(start, end) +
                            destTxt.substring(dend);
                    if (!resultingTxt.matches ("^\\d{1,3}(\\." +
                            "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                        return "";
                    } else {
                        String[] splits = resultingTxt.split("\\.");
                        for (int i=0; i<splits.length; i++) {
                            if (Integer.valueOf(splits[i]) > 255) {
                                return "";
                            }
                        }
                    }
                }
                return null;
            }
        };

        hostValue.setFilters(filters);

        String host = AppSharedPreference.getHostName(this);
        String port = AppSharedPreference.getPortNumber(this);
        int lane = AppSharedPreference.getLaneNumber(this);

        if ((host != null && !TextUtils.isEmpty(host)) && (port != null && !TextUtils.isEmpty(port))
                && lane > 0) {
            hostValue.setText(host);
            portValue.setText(port);
            laneValue.setText(String.valueOf(lane));

            hostValue.setEnabled(false);
            portValue.setEnabled(false);
            laneValue.setEnabled(false);
        } else {
            hostValue.setEnabled(true);
            portValue.setEnabled(true);
            laneValue.setEnabled(true);
        }

        saveSettingBtn.setOnClickListener(this);
        editSettingsBtn.setOnClickListener(this);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveSettingsBtn:
                String hostIp = hostValue.getText().toString();
                String port = portValue.getText().toString();
                int lane = Integer.parseInt(laneValue.getText().toString());
                messageTextView.setText("");
                if (!TextUtils.isEmpty(hostIp) && !TextUtils.isEmpty(port) && lane > 0) {
                    hostValue.setEnabled(false);
                    portValue.setEnabled(false);
                    laneValue.setEnabled(false);
                    AppSharedPreference.setHostIp(this,hostValue.getText().toString());
                    AppSharedPreference.setPort(this,portValue.getText().toString());
                    AppSharedPreference.setLaneNumber(this,Integer.parseInt(laneValue.getText().toString()));
                    Toast.makeText(this, "Save Settings", Toast.LENGTH_SHORT).show();
                    openLoginActivity();
                } else {
                    messageTextView.setText("Please provide Server IP, PORT No and Lane No.");
                }
                break;
            case R.id.editSettingsBtn:
                messageTextView.setText("");
                hostValue.setEnabled(true);
                portValue.setEnabled(true);
                laneValue.setEnabled(true);
                Toast.makeText(this, "Edit Settings", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, RfidZebraActivity.class);
        startActivity(intent);
        finish();
    }


    public static Intent startSettingActivity(Context context) {
        return new Intent(context, SettingsActivity.class);
    }
}
