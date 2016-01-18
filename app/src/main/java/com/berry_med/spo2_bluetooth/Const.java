package com.berry_med.spo2_bluetooth;

import java.util.UUID;

/**
 * Created by ZXX on 2015/8/31.
 */
public class Const {

    public static final UUID UUID_CLIENT_CHARACTER_CONFIG       = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final int  MESSAGE_OXIMETER_PARAMS            = 2003;
    public static final int  MESSAGE_OXIMETER_WAVE              = 2004;

    public static final int MESSAGE_BLUETOOTH_A_DEVICE     = 0;
    public static final int MESSAGE_BLUETOOTH_STOP_SCAN    = 1;
    public static final int MESSAGE_BLUETOOTH_START_SCAN   = 2;
    public static final int MESSAGE_BLUETOOTH_STATE_CHANGE = 3;
    public static final int MESSAGE_BLUETOOTH_DEVICE_NAME  = 4;
    public static final int MESSAGE_BLUETOOTH_TOAST        = 5;
    public static final int MESSAGE_BLUETOOTH_CONNECT_FAIL = 6;
    public static final int MESSAGE_BLUETOOTH_WRITE        = 7;
    public static final int MESSAGE_BLUETOOTH_LOST         = 8;
    public static final int MESSAGE_BLUETOOTH_DATA         = 9;

    public static final String DEVICE_NAME = "bt_name";
    public static final String TOAST       = "toast";
    public static final String BT_NAME     = "BerryMed";

    public static final String GITHUB_SITE                      = "https://github.com/zh2x/SpO2-Bluetooth-for-Android";
}
