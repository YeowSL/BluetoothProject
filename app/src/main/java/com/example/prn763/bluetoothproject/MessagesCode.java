package com.example.prn763.bluetoothproject;

/**
 * Created by PRN763 on 1/18/2016.
 */
public interface MessagesCode {
    //update dialog request code
    public static final int TOAST_DIALOG_DISPLAY = 0;
    public static final int TEXT_DIALOG_DISPLAY = 1;
    public static final int BTN_DIALOG_DISPLAY = 2;

    // Message types sent from the Bluetooth Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 2;
    public static final int REQUEST_ENABLE_BT = 3;

    // On click opcode
    public static final int ONCLICK_SAVE_ACTION = 0;
    public static final int ONCLICK_SCAN_ACTION = 1;

    // Key names received from the Bluetooth Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
}
