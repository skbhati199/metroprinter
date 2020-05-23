package com.metroinfrasys.metrotms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.metroinfrasys.metrotms.api.AppServiceAPI;
import com.metroinfrasys.metrotms.data.googlepay.QRCodeCreateAPI;
import com.metroinfrasys.metrotms.data.logout.LogoutModel;
import com.metroinfrasys.metrotms.data.server.ServerModel;
import com.metroinfrasys.metrotms.ui.BaseActivity;
import com.metroinfrasys.metrotms.utils.AppSharedPreference;
import com.metroinfrasys.metrotms.utils.UIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentQRValidateActivity extends BaseActivity {

    protected static final String TAG = "QRPayment";
    private Toolbar toolbar;
    ImageView image_qrcode;
    private View homeLayout;
    Button mValidate;
    boolean status;

    public static int WHITE = 0xFFFFFFFF;
    public static int BLACK = 0xFF000000;
    public final static int WIDTH=500;

    private AlertDialog dialog;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_payment_qrvalidate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar = findViewById(R.id.toolbar);
        homeLayout = findViewById(R.id.qrlayout);
        image_qrcode = findViewById(R.id.image_qrcode);
        mValidate = findViewById(R.id.validate);

        try {
            Bitmap bitmap = encodeAsBitmap("upi://pay?pa=12345678@okbizaxis&pn=XXXX&mc=5611&tr=GOOGQRTransactionReference&tn=description&am=100");
            image_qrcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        status = AppSharedPreference.getServerStatus(PaymentQRValidateActivity.this);

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
                    status = AppSharedPreference.getServerStatus(PaymentQRValidateActivity.this);
                    AppSharedPreference.setServerStatus(PaymentQRValidateActivity.this, response.body().getStatus());
                    toolbar.setBackgroundResource(R.color.colorPrimary);
                    if (isReadyStatus) {
                        mValidate.setEnabled(true);
                        mValidate.setBackgroundResource(R.color.colorPrimary);
                        createQR();
                    }
                } else {
                    status = AppSharedPreference.getServerStatus(PaymentQRValidateActivity.this);
                    AppSharedPreference.setServerStatus(PaymentQRValidateActivity.this, response.body().getStatus());
                    toolbar.setBackgroundResource(R.color.red);
                    mValidate.setEnabled(false);
                    mValidate.setBackgroundResource(R.color.buttonColorDisabled);
                    // do nothing
                }
            }

            @Override
            public void onFailure(Call<ServerModel> call, Throwable t) {
                toolbar.setBackgroundResource(R.color.red);
                mValidate.setEnabled(false);
                mValidate.setBackgroundResource(R.color.buttonColorDisabled);
            }
        });
    }


    @Override
    protected void printerUI() {
        setStatus("Printer Connected", Color.parseColor("#12786c"));

    }

    @Override
    protected void serverStatus() {
        serverStatusAPI();
        createQR();

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
        Log.i(TAG, result);

    }

    private void createQR() {

        JSONObject object = new JSONObject();
        try {

            JSONObject merchantInfo = new JSONObject();
            merchantInfo.put("googleMerchantId", "2609664923298430976");
            object.put("merchantInfo", merchantInfo);


            JSONObject transactionDetails = new JSONObject();
            transactionDetails.put("transactionId", "65424545456");
            JSONObject amount = new JSONObject();
            amount.put("currencyCode", "INR");
            amount.put("units", 100);
            amount.put("nanos", 0);

            transactionDetails.put("amount", amount);
            transactionDetails.put("description", "5463245");

            object.put("transactionDetails", transactionDetails);

            JSONObject validDuration = new JSONObject();
            validDuration.put("seconds", 60);
            validDuration.put("nanos", 0);

            object.put("validDuration", validDuration);
            object.put("qrType", "UPI_QR");

            Log.d ("json",object.toString ());
            checkout (object.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkout(String data) {
        uiHelper.showLoadingDialog("Loading...");
        String url= "https://mock-qrcode.firebaseapp.com/api/v1/create";
        AppServiceAPI serviceAPI = MetroTMSApp.createRetrofit(this);
        String token = "Bearer  ya29.c.Ko8BvAfOCAwa9H2HPwKi50wdHX2z32DM4V7V6kJb6cnB0UPVheFAGg6VCsCJcEF3RJG3XGmw5jYDb0LKaWpS-ebTPji_jcnUtktuOwKxsizhhhS2X2hC3kvnDE6yS9VGvstneamhXimVz4YwNnXMQ10_S-VfFoe92Yn-2bL0aJDmgdo2VLXxf7z5X0zS8fN1ijQ";
        Call<QRCodeCreateAPI> userModelCall = serviceAPI.createqrcode(url,token,data);
        userModelCall.enqueue(new Callback<QRCodeCreateAPI>() {
            @Override
            public void onResponse(Call<QRCodeCreateAPI> call, Response<QRCodeCreateAPI> response) {
                if (response.body() == null) {
                    networkfailurepopup("Please contact server team.");
                    return;
                }

            String qr = response.body().getQrLink();
                Toast.makeText(PaymentQRValidateActivity.this, qr, Toast.LENGTH_SHORT).show();
                uiHelper.dismissLoadingDialog();

            }

            @Override
            public void onFailure(Call<QRCodeCreateAPI> call, Throwable t) {
                uiHelper.dismissLoadingDialog();
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

}
