package com.example.prn763.bluetoothproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class BluetoothAgentActivity extends AppCompatActivity implements MessagesCode{

    private ImageView tryAgain;
    private Button save;
    private TextView agentBtStatusTV;
    private BluetoothAdapter agentBtAdapter;
    private ListView agentBtPairedList;
    private ArrayAdapter<String> agentAdapter;
    private ArrayList agentBtList = new ArrayList();

    private boolean BroadcastReceiverRegisterFlag = false;
    private BluetoothDevice [] agentDevice = new BluetoothDevice[20];
    private int numOfDeviceScanResult=0, tempPosi = 0;
    private int agentOnclickOpCode = 0;
    private boolean trackingReceiverRegisterStatus = false;

    // Debugging
    private static final String TAG = "Aero Dragon";
    private static final boolean D = true;

    // Key names received from the Bluetooth Handler
    private String agentDeviceName = "Unkown Device";

    //Object members
    private BluetoothManager agentBtMgr;
    private UIManager agentUIM;
    private BluetoothDataPacket agentBtDataPacket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initActivity();
        bluetoothSetup();
    }

    private void bluetoothSetup() {

        boolean deviceSupported = true;
        agentBtDataPacket = new BluetoothDataPacket(getApplicationContext(), mHandler, BluetoothAgentActivity.this);
        agentBtMgr = new BluetoothManager(getApplicationContext(), mHandler, BluetoothAgentActivity.this);
        agentBtAdapter = agentBtMgr.getBtAdapter();

        tryAgain.setVisibility(View.GONE);

        if (!agentBtMgr.getDeviceSupport()) {
            agentUIM.dialogUpdate(TEXT_DIALOG_DISPLAY, "Bluetooth Status : Device Not Supported Bluetooth", agentBtStatusTV);
            deviceSupported = false;
        }

        if (deviceSupported) {
            if (agentBtMgr.BluetoothDeviceIsEnable()) {
                agentBtMgr.disableBluetooth();
            }
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }
    }

    @Override
    public void onDestroy() {



        if(trackingReceiverRegisterStatus == true){
            unregisterReceiver(mReceiver);
        }


        if(agentBtAdapter.isEnabled()){
            agentBtAdapter.disable();
        }

        super.onDestroy();
    }

    private void initActivity() {

        agentUIM = new UIManager(getApplicationContext());
        save = (Button)findViewById(R.id.save);
        agentBtStatusTV = (TextView)findViewById(R.id.rcvrBtStatus);
        agentBtPairedList = (ListView)findViewById(R.id.btLv);
        tryAgain = (ImageView)findViewById(R.id.tryAgainImageView);

        //Dialog initial state
        agentUIM.dialogUpdate(TEXT_DIALOG_DISPLAY, "Bluetooth Status : Off", agentBtStatusTV);

        //Button initial state
        save.setVisibility(View.GONE);
        agentBtPairedList.setVisibility(View.GONE);

        //set onclick listener
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch(agentOnclickOpCode){
                    case ONCLICK_SAVE_ACTION:
                        SparseBooleanArray x = agentBtPairedList.getCheckedItemPositions();
                        if (x.size() != 0){
                             for (int i = 0; i < x.size(); i++ ){
                                if(x.valueAt(i)){
                                    tempPosi = x.keyAt(i);

                                    agentBtDataPacket.constructDataPacket(agentDevice[x.keyAt(i)].getAddress(), x.size(),i);
                                }
                            }
                           agentBtMgr.start();//listen to the incoming connection
                            agentUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "Save & Listening", null);
                            agentUIM.dialogUpdate(TEXT_DIALOG_DISPLAY, "Saved. Please Wait Connection...", agentBtStatusTV);
                            agentBtStatusTV.setTextColor(Color.parseColor("#2BFF3C"));
                        }else{
                            agentUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "Please Select Device", null);

                        }
                        break;
                    //yes one 123
                    case ONCLICK_SCAN_ACTION:
                        agentBtAdapter.startDiscovery();
                        break;
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //selecting things on action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        invalidateOptionsMenu();
        switch (item.getItemId()) {
            case R.id.requestConnection:
                //agentBtMgr.connect(agentDevice[tempPosi]);

                return true;
            case R.id.sendBtData:
                //byte [] word = new byte[]{0x49, 0x20, 0x57, 0x41, 0x4E, 0x4E, 0x41, 0x20, 0x46, 0x55, 0x43, 0x4B, 0x20, 0x59, 0x4F, 0x55};
                //byte [] word = agentDevice[tempPosi].getAddress().getBytes();
                //agentBtMgr.write(word);
                return true;
            case R.id.bt_pair:
                //agentUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "In progress pairing with " + agentDevice[tempPosi].getName(), null);
                //agentBtMgr.createBond(agentDevice[tempPosi]);
                return true;
            case R.id.dc_toggle:
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case REQUEST_ENABLE_BT:
                if(agentBtMgr.BluetoothDeviceIsEnable()) {
                    agentUIM.dialogUpdate(TEXT_DIALOG_DISPLAY, "Bluetooth Status : On", agentBtStatusTV);
                    registerReceiver(mReceiver, agentBtMgr.BluetoothRegScanAction());
                    agentBtAdapter.startDiscovery();
                    trackingReceiverRegisterStatus = true; // the flag is used to unregister at destroy state when this item is registered
                    agentBtPairedList.setVisibility(View.VISIBLE);
                }else{
                    agentUIM.dialogUpdate(TEXT_DIALOG_DISPLAY, "Bluetooth Status : Failed", agentBtStatusTV);
                    agentBtStatusTV.setTextColor(Color.parseColor("#FF0500"));
                    tryAgain.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    // The Handler that gets information back from the BluetoothManager
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothManager.STATE_CONNECTED:
                            agentUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "Connected & Sent to " + agentDeviceName, null);
                            byte [] word = agentBtDataPacket.retDataPacket2BeSend().getBytes();
                            agentBtMgr.write(word);

                            break;

                        case BluetoothManager.STATE_CONNECTING:
                            //agentUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "Connecting Device", null);
                            break;

                        case BluetoothManager.STATE_LISTEN:
                        case BluetoothManager.STATE_NONE:

                            break;

                        case BluetoothManager.STATE_SEND:
                            //agentUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "STATE_SEND", null);
                            break;
                    }
                    break;

                case MESSAGE_WRITE:
                    agentUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "MESSAGE_WRITE", null);
                    break;

                case MESSAGE_READ:
                    agentUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "MESSAGE_READ", null);
                    break;

                case MESSAGE_DEVICE_NAME:
                    agentDeviceName = msg.getData().getString(DEVICE_NAME);
                    break;

                case BluetoothManager.STATE_CONNECT_FAILED:
                    agentUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "Remote device not found, Please turn on", null);
                    break;
            }
        }
    };

private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch(action){
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:

                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    agentUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "Paired Success", null);
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    agentUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "Paired Failed", null);
                }
                agentAdapter.notifyDataSetChanged();

                break;

            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                agentUIM.dialogUpdate(TEXT_DIALOG_DISPLAY, "Bluetooth Status : Searching...", agentBtStatusTV);
                break;

            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:

                agentUIM.dialogUpdate(TEXT_DIALOG_DISPLAY, "Bluetooth Status : Search Finish", agentBtStatusTV);
                if(numOfDeviceScanResult != 0){
                    agentBtPairedList.setItemsCanFocus(false);
                    agentBtPairedList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    agentUIM.dialogUpdate(BTN_DIALOG_DISPLAY, "Save", save);
                    agentOnclickOpCode = ONCLICK_SAVE_ACTION;
                }else{
                    agentUIM.dialogUpdate(BTN_DIALOG_DISPLAY, "Scan Again", save);
                    agentUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "Device Not Found", null);
                    agentOnclickOpCode = ONCLICK_SCAN_ACTION;

                }
                save.setVisibility(View.VISIBLE);

                break;

            case BluetoothDevice.ACTION_FOUND:

                String pairedStatus = "Unpaired";
                agentDevice[numOfDeviceScanResult] = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (agentDevice[numOfDeviceScanResult].getBondState() == BluetoothDevice.BOND_BONDED){
                    pairedStatus = "Paired";
                }
                agentAdapter = new ArrayAdapter<String>(BluetoothAgentActivity.this, android.R.layout.simple_list_item_multiple_choice, agentBtList);
                agentBtPairedList.setAdapter(agentAdapter);
                agentBtList.add((numOfDeviceScanResult+1) + ". " + agentDevice[numOfDeviceScanResult].getName() + " : " + pairedStatus);
                numOfDeviceScanResult++;

                agentAdapter = null;

                break;
        }
    }
};

}
