package com.example.prn763.bluetoothproject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ReceiverActivity extends AppCompatActivity implements MessagesCode{

    // Debugging
    private static final String TAG = "MDP";
    private static final boolean D = true;

    //object member
    private UIManager rcvrUIM;
    private BluetoothDataPacket rcvrBtDataPacket;
    private BluetoothManager rcvrBtMgr;
    private BluetoothAdapter rcvrBtAdapter;
    private BluetoothDevice [] rcvrDevice = new BluetoothDevice[20];
    private BluetoothDevice btDevice;

    // widget
    private TextView rcvrBtStatus, rcvrInSteamMsg;
    private ListView rcvrBtDeviceList;
    private ImageView tryAgain;
    private Button btn;

    // member variable
    private ArrayList rcvrBtList = new ArrayList();
    private ArrayAdapter<String> rcvrAdapter;
    private int numOfDeviceScanResult;
    private String deviceName = "Unknown Device";
    String [] MACAddressArray;
    private int lvPosi;
    private boolean trackingReceiverRegisterStatus = false;

    //Device Status tracking, the position for name and status array are from same device
    public String[] MAC = new String [20];
    public boolean[] deviceConnectedState = new boolean[20];

    public boolean nextFlag = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_intermediate);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initActivity();
        rcvrBtSetup();
    }

    @Override
    public void onDestroy() {

        rcvrBtMgr.stop(); //stop all the connection if any

        if(trackingReceiverRegisterStatus == true){
            unregisterReceiver(mReceiver);
        }


        if(rcvrBtAdapter.isEnabled()){
            rcvrBtAdapter.disable();
        }

        super.onDestroy();
    }

    private void rcvrBtSetup() {

        boolean deviceSupported = true;
        rcvrBtAdapter = rcvrBtMgr.getBtAdapter();


        if (!rcvrBtMgr.getDeviceSupport()) {
            rcvrUIM.dialogUpdate(TEXT_DIALOG_DISPLAY, "Bluetooth Status : Device Not Supported Bluetooth", rcvrBtStatus);
            deviceSupported = false;
        }

        if (deviceSupported){
            if(rcvrBtMgr.BluetoothDeviceIsEnable()){
                rcvrBtMgr.disableBluetooth();
            }
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        tryAgain.setVisibility(View.GONE);
        rcvrBtDeviceList.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActivity() {

        rcvrUIM = new UIManager(getApplicationContext());
        rcvrBtMgr = new BluetoothManager(getApplicationContext(), mHandler, ReceiverActivity.this);
        rcvrBtDataPacket = new BluetoothDataPacket(getApplicationContext(), mHandler, ReceiverActivity.this);

        rcvrBtStatus = (TextView)findViewById(R.id.rcvrBtStatus);
        rcvrBtDeviceList = (ListView)findViewById(R.id.rcvrBtDeviceList);
        tryAgain = (ImageView)findViewById(R.id.tryAgainImageView);


        rcvrBtDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lvPosi = position;

                rcvrBtMgr.generateUUIDAccording2MACAddress(rcvrDevice[lvPosi]);

                if (rcvrDevice[lvPosi].getBondState() != BluetoothDevice.BOND_BONDED){
                    rcvrBtMgr.createBond(rcvrDevice[lvPosi]);
                }else{
                    rcvrBtMgr.connect(rcvrDevice[lvPosi]);
                }

                btDevice = rcvrDevice[lvPosi]; //assign the intermediate device

            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String btStatus;

        switch(requestCode){
            case REQUEST_ENABLE_BT:
                if (rcvrBtMgr.BluetoothDeviceIsEnable()) {
                    btStatus = "Bluetooth Status : On";
                    registerReceiver(mReceiver, rcvrBtMgr.BluetoothRegScanAction());
                    rcvrBtAdapter.startDiscovery();
                    trackingReceiverRegisterStatus = true;
                    rcvrBtDeviceList.setVisibility(View.VISIBLE);
                }else{
                    btStatus = "Bluetooth Status : Failed";
                    tryAgain.setVisibility(View.VISIBLE);
                    rcvrBtStatus.setTextColor(Color.parseColor("#FF0500"));
                }
                rcvrUIM.dialogUpdate(TEXT_DIALOG_DISPLAY, btStatus, rcvrBtStatus);
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
                                rcvrUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "Connected to " + deviceName, null);
                            break;

                        case BluetoothManager.STATE_CONNECTING:
                            rcvrUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "Device Connecting... ", null);
                            break;

                        case BluetoothManager.STATE_LISTEN:
                        case BluetoothManager.STATE_NONE:
                            //rcvrUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "STATE_LISTEN", null);
                            break;

                        case BluetoothManager.STATE_SEND:
                            rcvrUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "STATE_SEND", null);
                            break;

                    }
                    break;

                case MESSAGE_WRITE:
                    rcvrUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "MESSAGE_WRITE", null);
                    break;

                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String remoteDeviceMACAdress = new String(readBuf, 0, msg.arg1);
                    MACAddressArray = new String [rcvrBtDataPacket.getNumOfMACAddres(remoteDeviceMACAdress)];
                    MACAddressArray = rcvrBtDataPacket.receiveBtDataPacket(remoteDeviceMACAdress);

                    if(rcvrBtDataPacket.retMacAddressValidationStatus()){
                        connectAccessories();//request pairing or connection to accessories
                    }else{
                        rcvrUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "Invalid MAC Address Received", null);
                    }


                    break;

                case MESSAGE_DEVICE_NAME:
                    deviceName = msg.getData().getString(DEVICE_NAME);
                    break;

                case BluetoothManager.STATE_CONNECT_FAILED:
                    rcvrUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "Connection Failed", null);
                    break;
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String pairStatus = "UnknownPairedSatatus";

            switch (action){
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                        rcvrBtMgr.connect(btDevice);
                        pairStatus = "Paired Success";
                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                        pairStatus = "Paired Failed";
                    }
                    rcvrUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, pairStatus, null);
                    rcvrAdapter.notifyDataSetChanged();
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    rcvrUIM.dialogUpdate(TEXT_DIALOG_DISPLAY, "Bluetooth Status : Searching...", rcvrBtStatus);
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    rcvrUIM.dialogUpdate(TEXT_DIALOG_DISPLAY, "Bluetooth Status : Search Finish", rcvrBtStatus);
                    if (numOfDeviceScanResult != 0){
                        rcvrBtDeviceList.setItemsCanFocus(false);
                        rcvrBtDeviceList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    }else{
                        rcvrUIM.dialogUpdate(TOAST_DIALOG_DISPLAY, "Device Not Found", null);
                    }
                    break;

                case BluetoothDevice.ACTION_FOUND:
                    String pairedStatus = "Unpaired";
                    rcvrDevice[numOfDeviceScanResult] = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (rcvrDevice[numOfDeviceScanResult].getBondState() == BluetoothDevice.BOND_BONDED){
                        pairedStatus = "Paired";
                    }
                    rcvrBtList.add((numOfDeviceScanResult+1) + ". " + rcvrDevice[numOfDeviceScanResult].getName() + " : " + pairedStatus);

                    rcvrAdapter = new ArrayAdapter<String>(ReceiverActivity.this, android.R.layout.simple_list_item_1, rcvrBtList);
                    rcvrBtDeviceList.setAdapter(rcvrAdapter);

                    numOfDeviceScanResult++;

                    rcvrAdapter = null;
                    break;

            }

        }
    };

    public void connectAccessories(){

        boolean next = true;
        int currentDeviceNum = 0;
        BluetoothDevice BD;
        int numOfPackets = rcvrBtDataPacket.retNumOfAddressInDataPacket();
        MAC = rcvrBtDataPacket.retReceiveBtDataPacket();
//        MAC[0] = "64:9C:8E:0A:06:9B";
//        MAC[1] = "F4:FC:32:DF:B9:31";



        while(next){
            byte [] word = {0x47, 0x4F, 0x20, 0x54, 0x4F, 0x20, 0x4E, 0x45, 0x58, 0x54, 0x0A};
            rcvrBtMgr.write(word);

                btDevice = null;

                BD = (rcvrBtMgr.getBtAdapter()).getRemoteDevice(MAC[currentDeviceNum]);

                btDevice = BD; //assign accessories device

                if (BD.getBondState() == BluetoothDevice.BOND_BONDED) {

                    Log.i("ConnectionDeviceThread", ": Device No. : " + currentDeviceNum + " | MAC : " + MAC[currentDeviceNum] + " is Pairing & in progress connecting");

                    rcvrBtMgr.connect(BD);


                } else {
                    Log.i("ConnectionDeviceThread", ": Device No. : " + currentDeviceNum + " | MAC : " + MAC[currentDeviceNum] + " |Not Paired, proceed pair and connect");
                    rcvrBtMgr.createBond(BD);

                }



                if (currentDeviceNum == numOfPackets-1){
                    currentDeviceNum = 0;
                    next = false;
                }

                currentDeviceNum++;//go to next device

                for(int k=0; k<1; k++){
                    for(long j=0; j<350000000;j++){

                    }
                }

        }
    }
}



