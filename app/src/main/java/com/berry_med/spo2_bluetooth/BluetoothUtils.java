package com.berry_med.spo2_bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * Created by ZXX on 2015/11/24.
 */
public class BluetoothUtils{
    //TAG
    private final String TAG = this.getClass().getName();

    //UTILS
    private static BluetoothUtils mBtUtils   = null;

    private BluetoothAdapter    mBtAdapter  = null;
    private BluetoothChatService mBluetoothChatService;

    private BTConnectListener mConnectListener;



    /**
     * init settings
     */
    private BluetoothUtils() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothUtils getDefaultBluetoothUtils() {
        if (mBtUtils == null) {
            mBtUtils = new BluetoothUtils();
        }
        return mBtUtils;
    }


    public boolean isBleutoothEnabled()
    {
        return mBtAdapter.isEnabled();
    }

    /**
     * 打开蓝牙
     */
    public void enableBluetooth() {
        if (!mBtAdapter.isEnabled()) {
            mBtAdapter.enable();
        }
    }

    /**
     * 关闭蓝牙
     */
    public void disenableBluetooth() {
        if (mBtAdapter.isEnabled()) {
            mBtAdapter.disable();
        }
    }

    /**
     * 扫描蓝牙设备
     * @param b
     */
    public void startScan(boolean b) {
        if(b){
            mConnectListener.onStartScan();
            mBtAdapter.startDiscovery();
        }
        else{
            mBtAdapter.cancelDiscovery();
            mConnectListener.onStopScan();
        }
    }

    //GATT
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case Const.MESSAGE_BLUETOOTH_STATE_CHANGE:
                {
                    switch(msg.arg1)
                    {
                        case BluetoothChatService.STATE_CONNECTING:
                            Log.i(TAG, "handleMessage: Connecting");
                            break;
                        case BluetoothChatService.STATE_CONNECTED:
                            Log.i(TAG, "handleMessage: Connected");
                            mBtAdapter.cancelDiscovery();
                            mConnectListener.onConnected();
                            break;
                        case BluetoothChatService.STATE_NONE:
                            mConnectListener.onDisconnected();
                            break;
                        default:
                            break;
                    }
                }break;
                case Const.MESSAGE_BLUETOOTH_DATA:
                    mConnectListener.onReceiveData((byte[])msg.obj);
                    break;

            }
        }
    };

    /**
     * 连接蓝牙设备
     * @param context
     * @param device
     */
    public void connect(Context context, final BluetoothDevice device) {
        mBluetoothChatService = new BluetoothChatService(context,mHandler);
        mBluetoothChatService.connect(device, true);
    }

    /**
     * 断开蓝牙设备
     */
    public void disconnect()
    {
        mBluetoothChatService.stop();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        return intentFilter;
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mConnectListener.onFoundDevice(device);
                Log.i(TAG, "onReceive: GATT:"+ device.getName());
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                mConnectListener.onStopScan();
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                mConnectListener.onStartScan();
            }

        }
    };

    public void registerBroadcastReceiver(Context context)
    {
        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    public void unregisterBroadcastReceiver(Context context)
    {
        context.unregisterReceiver(mGattUpdateReceiver);
    }

    public void setConnectListener(BTConnectListener listener)
    {
        mConnectListener = listener;
    }


    public interface BTConnectListener
    {
        void onFoundDevice(BluetoothDevice device);
        void onStopScan();
        void onStartScan();

        void onConnected();
        void onDisconnected();
        void onReceiveData(byte[] dat);
    }
}
