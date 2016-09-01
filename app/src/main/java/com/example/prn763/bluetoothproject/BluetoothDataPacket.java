package com.example.prn763.bluetoothproject;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by PRN763 on 1/21/2016.
 */
public class BluetoothDataPacket extends BluetoothManager{

    private int mPreviousEndIndex = 0, mNumOfData = 0;
    private String mDataString, mConstrucDataString;
    private boolean macAddressFormatStatus = true;
    private Context con;

    /**
     * Constructor. Prepares a new Bluetooth Service session.
     *
     * @param context  The UI Activity Context
     * @param handler
     * @param activity
     */


    public BluetoothDataPacket(Context context, Handler handler, Activity activity) {
        super(context, handler, activity);
        mConstrucDataString = "";
        con = context;
    }

    public boolean retMacAddressValidationStatus(){
        return macAddressFormatStatus;
    }

    public void checkMACAddressValidation(String addr){
        if(addr.length() < 17 || addr.length() >17){
            macAddressFormatStatus = false;
        }

        if(verifyMacAddressdoubleColem(addr) == false){
            macAddressFormatStatus = false;
        }

        Log.i("BluetoothDataPacket", "::checkMACAddressValidation : macAddressFormatStatus = " + macAddressFormatStatus);
    }

    public boolean verifyMacAddressdoubleColem(String addr){
        boolean state = true;
        char c;

        for (int i = 2; i <= 14; ){

            c = addr.charAt(i);

            if(c != ':'){
                state = false;
                break;
            }

            i += 3;
        }

        return state;
    }

    public synchronized String [] retReceiveBtDataPacket(){
        String [] data = receiveBtDataPacket(mDataString);
        return data;
    }

    public  synchronized void setDataString(String s){
        mDataString = s;
    }

    //Current single data packet protocol <1S12:4B:23:34:45:78>
    //2MAC coming data packet protocol <2S12:4B:23:34:45:78,12:4B:23:34:45:78>
    public synchronized String [] receiveBtDataPacket(String data){
        macAddressFormatStatus = true; //initialize to default for every data packet received before the velidation is carry
        String [] MACAddres = null;
        //String address;
        int sizeOfData = 0;
        int numOfAddress = 0;
        boolean format = true;

        sizeOfData = data.length();

        setDataString(data);


        if(data.charAt(0) == '<' || data.charAt(sizeOfData - 2) == '>'){//to do -2 because temporary the zoc got problem
            //do ntg
        }else{
            format = false;
            Log.i("BluetoothDataPacket", "::receiveBtDataPacket : Corrupted Data Packet");
        }

        if (format){
            numOfAddress = getNumOfMACAddres(data);
            MACAddres = new String [numOfAddress];
            Log.i("BluetoothDataPacket", "::receiveBtDataPacket : " + Integer.toString(numOfAddress) + " device received");
            for(int i = 0; i < numOfAddress; i++){

                MACAddres[i] = getMACAddressFromDataPacket(data, i);
                checkMACAddressValidation(MACAddres[i]);
            }
        }

        return MACAddres;
    }

    public synchronized String getMACAddressFromDataPacket(String data, int num){
        String singleAddress = "00:00:00:00:00:00";

        if(num == 0){
            if (getNumOfMACAddres(data) == 1){//if only one data is available
                singleAddress = data.substring(data.indexOf('S') + 1, data.indexOf('>'));
            }else{
                singleAddress = data.substring(data.indexOf('S') + 1, data.indexOf(','));
            }
            Log.i("BluetoothDataPacket", "::getMACAddressFromDataPacket : No." + num + " => "+ singleAddress);
        }else{
            singleAddress = data.substring(getAddressStartindex(data, num), getAddressEndIndex(data, num));
            Log.i("BluetoothDataPacket", "::getMACAddressFromDataPacket : No." + num + " => "+ singleAddress);
        }

        return singleAddress;
    }

    public synchronized int getAddressStartindex(String data, int num){
        int index = 0, start = 0;


        if(num >= 2){
           start  = mPreviousEndIndex;
        }

        index = data.indexOf(',', start) + 1;

        return index;
    }

    public synchronized int getAddressEndIndex(String data, int num){
        int index = 0;

        if(getNumOfMACAddres(data) == num+1){
            index = data.indexOf('>', getAddressStartindex(data, num));
        }else{
            index = data.indexOf(',', getAddressStartindex(data, num));
        }
        mPreviousEndIndex = index;

        return index;
    }

    public synchronized int getNumOfMACAddres(String data){
        int num = 0, indexOfS = 0;

        indexOfS = data.indexOf('S');
        String s = data.substring(1,indexOfS);
        num = Integer.parseInt(s);
        mNumOfData = num;
        return num;
    }

    public synchronized int retNumOfAddressInDataPacket(){
        return mNumOfData;
    }

    public synchronized void constructDataPacket(String address, int size, int currentPosi){

        mConstrucDataString = mConstrucDataString + address;

        if(currentPosi+1 == size){
            constuctPacketSize(size);
        }else{
            mConstrucDataString = mConstrucDataString + ",";
        }

    }

    public synchronized void constuctPacketSize(int size){
        mConstrucDataString = "<" + size + "S" + mConstrucDataString + ">";
        Log.i("constuctPacketSize", " : Data Packet : " + mConstrucDataString);
    }

    public synchronized String retDataPacket2BeSend(){
        return mConstrucDataString;
    }
}
