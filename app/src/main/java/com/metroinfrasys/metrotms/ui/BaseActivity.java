package com.metroinfrasys.metrotms.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.metroinfrasys.metrotms.R;
import com.metroinfrasys.metrotms.utils.DiscoveredPrinterListAdapter;
import com.metroinfrasys.metrotms.utils.FormatRefresher;
import com.metroinfrasys.metrotms.utils.NetworkUtils;
import com.metroinfrasys.metrotms.utils.SavedFormatProvider;
import com.metroinfrasys.metrotms.utils.SelectedPrinterManager;
import com.metroinfrasys.metrotms.utils.UIHelper;
import com.metroinfrasys.metrotms.utils.UsbHelper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;
import com.zebra.sdk.printer.discovery.DiscoveryException;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;
import com.zebra.sdk.printer.discovery.NetworkDiscoverer;
import com.zebra.sdk.printer.discovery.UrlPrinterDiscoverer;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    protected View progressCircular;
    public static final String APP_PREFERENCES_KEY = "PrintStationPreferences";
    public static final int DIALOG_DISCOVERY = 0;
    public static final int DIALOG_ABOUT = 1;
    public static final int REQUEST_ENABLE_BT = 0;
    private DiscoveredPrinterListAdapter discoveredPrinterListAdapter;
    private DiscoveredPrinter formatPrinter;
    protected BaseAdapter spinnerAdapter;
    private boolean isHostWroking;
    SimpleDateFormat timeFormat = new SimpleDateFormat(SavedFormatProvider.DATE_FORMAT);


    protected Connection connection;
    private UsbHelper usbHelper = new UsbHelper(this) {

        @Override
        public void usbDisconnected(UsbDevice device) {
//            uiHelper.dismissLoadingDialog();
            removeDisconnectedUsbPrinterFromHistory(device);
        }

        private void removeDisconnectedUsbPrinterFromHistory(UsbDevice device) {
            DiscoveredPrinter[] printers = SelectedPrinterManager.getPrinterHistory();
            for (int i = 0; i < printers.length; i++) {
                DiscoveredPrinter printer = printers[i];
                if (isPrinterToRemove(device, printer)) {
                    SelectedPrinterManager.removeHistoryItemAtIndex(i);
                    spinnerAdapter.notifyDataSetChanged();

                    if (SelectedPrinterManager.getSelectedPrinter() == null) {
                        showDialog(DIALOG_DISCOVERY);
                    } else {
                        if (i == 0) {
//                            uiHelper.dismissLoadingDialog();
                            FormatRefresher.execute(BaseActivity.this);
                        }
                    }
                    return;
                }
            }
        }

        private boolean isPrinterToRemove(UsbDevice device, DiscoveredPrinter printer) {
            if (printer instanceof DiscoveredPrinterUsb) {
                DiscoveredPrinterUsb usbPrinter = (DiscoveredPrinterUsb) printer;
                return device.getDeviceName().equals(usbPrinter.device.getDeviceName());
            }
            return false;
        }

        @Override
        public void usbConnectedAndPermissionGranted(UsbDevice device) {
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            SelectedPrinterManager.setSelectedPrinter(new DiscoveredPrinterUsb(device.getDeviceName(), usbManager, device));
            if (spinnerAdapter != null) {
                spinnerAdapter.notifyDataSetChanged();
            }
            FormatRefresher.execute(BaseActivity.this);
        }
    };
    private MyPrinterStatus myPrinterStatus;
    public boolean isReadyStatus;

    protected UIHelper uiHelper = new UIHelper(BaseActivity.this);

    public class MyPrinterStatus extends AsyncTask<Void, Boolean, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            uiHelper.showLoadingDialog("Check Printer Status");

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return isPrinterReady();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            isReadyStatus = aBoolean;
            uiHelper.dismissLoadingDialog();
            if (!isReadyStatus) {
                setStatus("Printer not Connected", Color.RED);
            } else {
//                setStatus("Printer Ready", Color.RED);
                printerUI();
            }

        }
    }

    protected abstract void printerUI();


    public void serverOnlineStatus(boolean isOnline) {
        isHostWroking = isOnline;
    }


    protected void printReceipt(final String plazaName, final String receiptNumber, final String tcName, final String laneId, final String journeyType, final String tcClass,
                                final String amount, final String transactionId, final String receiptType, final String sealNo) {

        new AsyncTask<Void, Void, Void>() {

            protected void onPreExecute() {
                uiHelper.updateLoadingDialog("Receipt Printing...");
                BaseActivity.this.findViewById(R.id.process).setBackgroundResource(R.color.buttonColorDisabled);
                BaseActivity.this.findViewById(R.id.process).setEnabled(false);
                Log.d(TAG, "Start printing");
                Toast.makeText(BaseActivity.this, "Receipt Printing", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                printFormat(plazaName, receiptNumber, tcName, laneId, journeyType, tcClass, amount, transactionId, receiptType, sealNo);
                return null;
            }

            protected void onPostExecute(Void result) {
                uiHelper.dismissLoadingDialog();
                BaseActivity.this.findViewById(R.id.process).setBackgroundResource(R.color.colorPrimary);
                BaseActivity.this.findViewById(R.id.process).setEnabled(true);
                startTimer();
            }

            ;
        }.execute((Void) null);
    }


    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();

    //To stop timer
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }


    //To start timer
    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (isConnected()) {
                            serverStatus();
                        }
                    }
                });
            }
        };
        timer.schedule(timerTask, 5000, 20000);
    }

    protected abstract void serverStatus();


    protected boolean isPrinterReady() {
        boolean isPrinterReady = false;
        connection = SelectedPrinterManager.getPrinterConnection();
        if (connection != null) {
            try {
                connection.open();
                ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
                ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);
                PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();
                if (printerStatus.isReadyToPrint) {
                    setStatus("Printer Ready", Color.parseColor("#12786c"));
                    isPrinterReady = true;
                } else if (printerStatus.isHeadOpen) {
                    setStatus("Printer Head Open", Color.RED);
                    isPrinterReady = false;
                } else if (printerStatus.isPaused) {
                    setStatus("Printer is Paused", Color.RED);
                    isPrinterReady = false;
                } else if (printerStatus.isPaperOut) {
                    setStatus("Printer Media Out", Color.RED);
                    isPrinterReady = false;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } catch (ConnectionException e) {
                e.printStackTrace();
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                e.printStackTrace();
                disconnect();
            } finally {
                try {
                    connection.close();
                } catch (ConnectionException e) {
                }
            }
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {

        }

        return isPrinterReady;
    }

    protected void printFormat(String plazaName, String receiptNumber, String tcName, String laneId, String journeyType,
                               String tcClass, String amount, String luhncode, String receiptType, String sealNo) {
        if (uiHelper != null && uiHelper.isDialogActive()) {
            uiHelper.showLoadingDialog("Printing  TMS");
        }
        connection = SelectedPrinterManager.getPrinterConnection();
        if (connection != null) {
            try {
                connection.open();
//                SGD.SET("device.languages", "cpcl", connection);
                try {
                    Date date = new Date();
                    String timestamp = timeFormat.format(date);
                    String receiptPrinterData = "";


                    /*switch (receiptType) {
                        case "logout":
                            receiptPrinterData = "^XA" +
                                    "^FX Top section with company logo, name and address." +
                                    "^CF0,25" +
                                    "^FO10,50^FD===============================================^FS" +
                                    "^CF0,30" +
                                    "^FO100,80^FDKM-42 Toll Plaza Manesar^FS" +
                                    "^CF0,25" +
                                    "^FO50,110^FD ^FS" +
                                    "^FO50,140^FDLogout User Name: " + tcName + "^FS" +
                                    "^FO50,170^FDSeal No: " + sealNo + "^FS" +
                                    "^FO50,200^FDLane No.: " + laneId + "^FS" +
                                    "^FO50,230^FDDate & Time : " + timestamp + "^FS" +
                                    "^FO10,260^FD===============================================^FS" +
                                    "^CF0,25" +
                                    "^FO100,290^FDThank you Have A Nice Day !^FS" +
                                    "^XZ";

                            logoutTCUSerActivity();
                            break;
                        case "login":
                            receiptPrinterData = "^XA" +
                                    "^FX Top section with company logo, name and address." +
                                    "^CF0,25" +
                                    "^FO10,50^FD===============================================^FS" +
                                    "^CF0,30" +
                                    "^FO100,80^FD" + plazaName + "^FS" +
                                    "^CF0,25" +
                                    "^FO50,90^FD ^FS" +
                                    "^FO50,120^FDLogin User Name: " + tcName + "^FS" +
                                    "^FO50,150^FDSeal No: " + sealNo + "^FS" +
                                    "^FO50,180^FDLane No.: " + laneId + "^FS" +
                                    "^FO50,210^FDDate & Time : " + timestamp + "^FS" +
                                    "^FO10,240^FD===============================================^FS" +
                                    "^CF0,25" +
                                    "^FO100,270^FDThank you Have A Nice Day !^FS" +
                                    "^XZ";
                            loginSupervisorOpenActivity();

                            break;
                        case "laneReceipt":
                            receiptPrinterData = printReceipt(plazaName, receiptNumber, tcName, laneId, journeyType, tcClass, amount, luhncode, timestamp);
                            break;
                        default:
                            Toast.makeText(this, "No receipt printing", Toast.LENGTH_SHORT).show();
                            break;
                    }*/
                    switch (receiptType) {
                        case "logout":
                            receiptPrinterData = "! 0 203 203 350 1" +
                                    "TEXT 5 0 30 40 " + plazaName + "\n" +
                                    "TEXT 5 0 30 70 Logout User Name: " + tcName + "\n" +
                                    "TEXT 5 0 30 100 Seal No: " + sealNo + "\n" +
                                    "TEXT 5 0 30 130 Lane No.: " + laneId + "\n" +
                                    "TEXT 5 0 30 160 Date & Time : " + timestamp + "\n" +
                                    "TEXT 5 0 30 190 ===========================\n" +
                                    "TEXT 5 0 30 220 Thank you Have A Nice Day !\n" +
                                    "PRINT" + "\n";

//                            logoutTCUSerActivity();
                            break;
                        case "login":
                            receiptPrinterData = "! 0 203 203 350 1" +
                                    "TEXT 5 0 30 70 " + plazaName + "\n" +
                                    "TEXT 5 0 30 100 Login User Name: " + tcName + "\n" +
                                    "TEXT 5 0 30 130 Seal No: " + sealNo + "\n" +
                                    "TEXT 5 0 30 160 Lane No.: " + laneId + "\n" +
                                    "TEXT 5 0 30 190 Date & Time : " + timestamp + "\n" +
                                    "TEXT 5 0 30 220 ============================" + "\n" +
                                    "TEXT 5 0 30 250 Thank you Have A Nice Day !" + "\n" +
                                    "PRINT" + "\n\n";
//                            loginSupervisorOpenActivity();
                            break;
                        case "laneReceipt":
                            receiptPrinterData = printReceipt(plazaName, receiptNumber, tcName, laneId, journeyType, tcClass, amount, luhncode, timestamp);
                            break;
                        default:
                            Toast.makeText(this, "No receipt printing", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    connection.write(receiptPrinterData.getBytes());

//                    if (connection instanceof BluetoothConnection) { //remove delay time
//                        String friendlyName = ((BluetoothConnection) connection).getFriendlyName();
//                        setStatus(friendlyName, Color.parseColor("#12786c"));
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
                } catch (ConnectionException e) {
                    setStatus(e.getMessage(), Color.RED);
                }
            } catch (ConnectionException e) {
                disconnect();
            } finally {
                try {
                    connection.close();
                } catch (ConnectionException e) {
                }
            }
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {

        }

        uiHelper.dismissLoadingDialog();
        processButtonUIShow();
        if (!receiptType.equalsIgnoreCase("laneReceipt")){
            openNextActivity();
        }
    }

    protected abstract void openNextActivity();

    protected abstract void processButtonUIShow();

//    protected abstract void loginSupervisorOpenActivity();

//    protected abstract void logoutTCUSerActivity();

    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
            setStatus("Printer Not Connected", Color.RED);
        } catch (ConnectionException e) {
            setStatus("COMM Error! Disconnected", Color.RED);
        }
    }

    protected void setStatus(final String statusMessage, final int color) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    ((TextView) BaseActivity.this.findViewById(R.id.messageTextView)).setBackgroundColor(color);
                    ((TextView) BaseActivity.this.findViewById(R.id.messageTextView)).setText(statusMessage);
                    ((TextView) BaseActivity.this.findViewById(R.id.messageTextView)).setTextColor(Color.WHITE);
                } catch (Exception e) {

                }

            }
        });
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    protected abstract void logoutOpenActivity();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Bundle extras = data.getExtras();
            String result = extras.getString(Intents.Scan.RESULT);

            if (result != null) {
                scanBarCodeResult(result);
            }
        }

    }

    protected void scanTapOnButton() {
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, 0);
    }

    @NonNull
    private String printReceipt(String plazaName, String receiptNumber, String tcName, String laneId, String journeyType, String tcClass, String amount, String luhncode, String timestamp) {
        String HPASSNUMBER = receiptNumber;
        String barcode = "! 0 203 203 203 1" +
                "TEXT 5 0 30 70" + " "+HPASSNUMBER + "\n" +
                "BT OFF" + "\n" +
                "B 128 1 20 60 30 0 " + "A"+HPASSNUMBER + "" + "\n" +
                "PRINT" + "\n";
        String receiptPrinterData;
        receiptPrinterData = "! 0 203 203 380 1" +
                "\n" +
                "TEXT 5 0 30 10 " + plazaName + "\n" +
                "TEXT 5 0 30 40 Receipt No.: " + receiptNumber + "^\n" +
                "TEXT 5 0 30 70 TC Name: " + tcName + "\n" +
                "TEXT 5 0 30 100 Lane No.: " + laneId + "\n" +
                "TEXT 5 0 30 130 Journey Type : " + journeyType + "\n" +
                "TEXT 5 0 30 160 TC Class : " + tcClass + "\n" +
                "TEXT 5 0 30 190 Fare : Rs. " + amount + "\n" +
                "TEXT 5 0 30 220 Date & Time : " + timestamp + "\n" +
                "TEXT 5 0 30 250 ===============================\n" +
                "TEXT 5 0 30 280 Thank you Have A Nice Day !\n" +
                "TEXT 5 0 30 310 Emergency No: 8800297112\n" +
                "TEXT 5 0 30 340 Toll Free No: 18001031700\n" +
                "PRINT\n" + barcode;
        return receiptPrinterData;
    }

    /* @NonNull
     private String printReceipt(String plazaName, String receiptNumber, String tcName, String laneId, String journeyType, String tcClass, String amount, String luhncode, String timestamp) {
         String receiptPrinterData;
         receiptPrinterData = "^XA" +
                 "^FX Top section with company logo, name and address." +
                 "^CF0,16" +
                 "^FO10,30^FD===============================================^FS" +
                 "^CF0,25" +
                 "^FO50,45^FD" + plazaName + "^FS" +
                 "^CF0,22" +
                 "^FO50,20^FD ^FS" +
                 "^FO50,80^FDReceipt No.: " + receiptNumber + "^FS" +
                 "^FO50,105^FDTC Name: " + tcName + "^FS" +
                 "^FO50,130^FDLane No.: " + laneId + "^FS" +
                 "^FO50,155^FDJourney Type : " + journeyType + "^FS" +
                 "^FO50,180^FDTC Class : " + tcClass + "^FS" +
                 "^FO50,205^FDFare : Rs. " + amount + "^FS" +
                 "^FO50,230^FDDate & Time : " + timestamp + "^FS" +
                 "^CF0,16" +
                 "^FO10,250^FD===============================================^FS" +
                 "^FX Third section with contact." +
                 "^CF0,20" +
                 "^FO120,270^FDThank you Have A Nice Day !^FS" +
                 "^FO120,295^FDEmergency No: 8800297112^FS" +
                 "^FO120,320^FDToll Free No: 18001031700^FS" +
                 "^FX Third section with barcode." +
                 "^BY3,4,70" +
                 "^FO50,360^BC^FDH" + receiptNumber + "" + luhncode + "^FS" +
                 "^XZ";
         return receiptPrinterData;
     }
 */
    protected abstract void scanBarCodeResult(String result);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResourceId());

        try {
            usbHelper.onCreate(getIntent());
        } catch (Exception e) {

        }

        SelectedPrinterManager.populatePrinterHistoryFromPreferences(this);


        DiscoveredPrinter selectedPrinter = SelectedPrinterManager.getSelectedPrinter();
        if (selectedPrinter != null && selectedPrinter instanceof DiscoveredPrinterUsb == false) {
            FormatRefresher.execute(this);
        }
    }

    protected abstract int getLayoutResourceId();

    @Override
    protected void onResume() {
        super.onResume();
        try {

            usbHelper.onResume();
        } catch (Exception e) {

        }

        removeDisconnectedUsbPrintersFromList();
//        processNfcScan(getIntent());

        DiscoveredPrinter selectedPrinter = SelectedPrinterManager.getSelectedPrinter();

        if (selectedPrinter == null) {
            showDialog(DIALOG_DISCOVERY);
        } else {
//            FormatRefresher.execute(this);
            myPrinterStatus = new MyPrinterStatus();
            myPrinterStatus.execute();
        }
    }


    @Override
    protected void onStart() {
        Log.e("home", "onStart");
        startTimer();
        super.onStart();
    }

    private void removeDisconnectedUsbPrintersFromList() {
        UsbManager usbService = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbService.getDeviceList();


        DiscoveredPrinter[] history = SelectedPrinterManager.getPrinterHistory();
        for (int i = 0; i < history.length; i++) {
            DiscoveredPrinter historyPrinter = history[i];
            if (historyPrinter instanceof DiscoveredPrinterUsb && deviceList.containsKey(historyPrinter.address) == false) {
                SelectedPrinterManager.removeHistoryItemAtIndex(i);
                i--;
                history = SelectedPrinterManager.getPrinterHistory();
            }
        }

        if (spinnerAdapter != null) {
            spinnerAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog;
        switch (id) {
            case DIALOG_DISCOVERY:
                dialog = new Dialog(this);

                dialog.setContentView(R.layout.discovery_dialog);
                dialog.setTitle("Select a Printer");
                dialog.setCancelable(true);

                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        if (SelectedPrinterManager.getSelectedPrinter() == null) {
                            finish();
                        }
                    }
                });

                ListView discoveryList = (ListView) dialog.findViewById(R.id.discoveryList);
                discoveryList.setEmptyView(dialog.findViewById(R.id.emptyDiscoveryLayout));

                discoveredPrinterListAdapter = new DiscoveredPrinterListAdapter(this);

                discoveryList.setAdapter(discoveredPrinterListAdapter);

                discoveryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                        DiscoveredPrinter printer = (DiscoveredPrinter) discoveredPrinterListAdapter.getPrinter(position);
                        dialog.dismiss();

                        SelectedPrinterManager.setSelectedPrinter(printer);
                        myPrinterStatus = new MyPrinterStatus();
                        myPrinterStatus.execute();
//                        spinnerAdapter.notifyDataSetChanged();
                        SelectedPrinterManager.storePrinterHistoryInPreferences(BaseActivity.this);
                        FormatRefresher.execute(BaseActivity.this);
                    }
                });
                break;
            default:
                dialog = super.onCreateDialog(id);
        }
        return dialog;
    }

    private volatile boolean usbDiscoveryComplete = false;
    private volatile boolean bluetoothDiscoveryComplete = false;
    private volatile boolean networkDiscoveryComplete = false;

    @Override
    protected void onPrepareDialog(int id, final Dialog dialog) {
        switch (id) {
            case DIALOG_DISCOVERY:
                dialog.findViewById(R.id.refreshPrintersButton).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        doDiscovery(dialog);
                    }
                });

                doDiscovery(dialog);
        }

        super.onPrepareDialog(id, dialog);
    }

    private void doDiscovery(final Dialog dialog) {
        discoveredPrinterListAdapter.clearPrinters();
        discoveredPrinterListAdapter.notifyDataSetChanged();

        ((TextView) dialog.findViewById(R.id.emptyDiscovery)).setText("Searching...");
        dialog.findViewById(R.id.searchingSpinner).setVisibility(View.VISIBLE);
        dialog.findViewById(R.id.refreshPrintersButton).setVisibility(View.INVISIBLE);

        usbDiscoveryComplete = false;
        bluetoothDiscoveryComplete = false;
        networkDiscoveryComplete = false;

        UsbDiscoverer.findPrinters(this, new DiscoveryHandler() {
            public void foundPrinter(final DiscoveredPrinter printer) {
                BaseActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        discoveredPrinterListAdapter.addPrinter(printer);
                    }
                });
            }

            public void discoveryFinished() {
                usbDiscoveryComplete = true;
            }

            public void discoveryError(String message) {
                usbDiscoveryComplete = true;
            }
        });

        try {
            BluetoothDiscoverer.findPrinters(this, new DiscoveryHandler() {

                public void foundPrinter(final DiscoveredPrinter printer) {
                    BaseActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            discoveredPrinterListAdapter.addPrinter(printer);
                        }
                    });
                }

                public void discoveryFinished() {
                    bluetoothDiscoveryComplete = true;
                }

                public void discoveryError(String message) {
                    bluetoothDiscoveryComplete = true;
                }
            });
        } catch (ConnectionException e) {
            bluetoothDiscoveryComplete = true;
        }

        try {
            NetworkDiscoverer.findPrinters(new DiscoveryHandler() {

                public void foundPrinter(final DiscoveredPrinter printer) {
                    BaseActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            discoveredPrinterListAdapter.addPrinter(printer);
                        }
                    });
                }

                public void discoveryFinished() {
                    networkDiscoveryComplete = true;
                }

                public void discoveryError(String message) {
                    networkDiscoveryComplete = true;
                }
            });
        } catch (DiscoveryException e) {
            networkDiscoveryComplete = true;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                while (usbDiscoveryComplete == false || bluetoothDiscoveryComplete == false || networkDiscoveryComplete == false) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                dialog.findViewById(R.id.searchingSpinner).setVisibility(View.INVISIBLE);
                dialog.findViewById(R.id.refreshPrintersButton).setVisibility(View.VISIBLE);
                ((TextView) dialog.findViewById(R.id.emptyDiscovery)).setText("No Printers Found");
                super.onPostExecute(result);
            }
        }.execute((Void) null);
    }

    private void processNfcScan(Intent intent) {
        Parcelable[] scannedTags = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (scannedTags != null && scannedTags.length > 0) {
            NdefMessage msg = (NdefMessage) scannedTags[0];
            AsyncTask<String, Void, Void> findNfcTask = new AsyncTask<String, Void, Void>() {

                ProgressDialog dialog;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    dialog = new ProgressDialog(BaseActivity.this, ProgressDialog.STYLE_SPINNER);
                    dialog.setMessage("Processing NFC Scan");
                    dialog.show();

                    try {
                        BaseActivity.this.dismissDialog(DIALOG_DISCOVERY);
                    } catch (IllegalArgumentException e) {
                    }
                }

                private volatile boolean discoveryComplete = false;
                private volatile boolean printerFound = false;

                @Override
                protected Void doInBackground(String... params) {
                    try {
                        UrlPrinterDiscoverer.findPrinters(params[0], new DiscoveryHandler() {

                            @Override
                            public void foundPrinter(DiscoveredPrinter printer) {
                                if (printerFound == false) {
                                    printerFound = true;
                                    SelectedPrinterManager.setSelectedPrinter(printer);
                                    SelectedPrinterManager.storePrinterHistoryInPreferences(BaseActivity.this);
                                }
                            }

                            @Override
                            public void discoveryFinished() {
                                discoveryComplete = true;
                            }

                            @Override
                            public void discoveryError(String message) {
                                discoveryComplete = true;
                            }
                        });
                    } catch (DiscoveryException e) {
                        e.printStackTrace();
                        discoveryComplete = true;
                    }

                    while (discoveryComplete == false && printerFound == false) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    dialog.dismiss();
                    try {
                        BaseActivity.this.dismissDialog(DIALOG_DISCOVERY);
                    } catch (IllegalArgumentException e) {
                    }

                    if (spinnerAdapter != null) {
                        spinnerAdapter.notifyDataSetChanged();
                        if (printerFound == true) {
                            FormatRefresher.execute(BaseActivity.this);
                        }
                    }

                    if (printerFound == false) {
                        Toast.makeText(BaseActivity.this, "Unable to find specified NFC printer", Toast.LENGTH_SHORT).show();
                    }

                    if (SelectedPrinterManager.getSelectedPrinter() == null) {
                        showDialog(DIALOG_DISCOVERY);
                    }
                }
            };
            String payload = new String(msg.getRecords()[0].getPayload());
            findNfcTask.execute(payload);


            intent.removeExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            usbHelper.onPause();

        } catch (Exception e) {

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {

            usbHelper.onNewIntent(intent);
        } catch (Exception e) {

        }
    }


    protected void setLoading() {
        progressCircular.setVisibility(View.VISIBLE);
    }

    protected void hideLoading() {
        if (progressCircular != null)
            progressCircular.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        hideLoading();
        Log.e("home", "onStop");
        stopTimer();
        super.onStop();
    }

    public boolean isConnected() {
        return NetworkUtils.isNetworkConnected(this);
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
