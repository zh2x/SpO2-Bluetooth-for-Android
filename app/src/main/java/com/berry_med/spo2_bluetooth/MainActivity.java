package com.berry_med.spo2_bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.berry_med.OximeterData.DataParser;
import com.berry_med.OximeterData.PackageParser;
import com.berry_med.waveform.WaveForm;
import com.berry_med.waveform.WaveFormParams;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements PackageParser.OnDataChangeListener{

    private final static String TAG = MainActivity.class.getSimpleName();

    //UI
    private Button btnBluetoothToggle;
    private Button btnSearchOximeters;
    private TextView tvStatusBar;
    private TextView tvParamsBar;
    private EditText edBluetoothName;

    //data and waveform
    private DataParser mDataParser;
    private PackageParser mPackageParser;
    private WaveForm mSpO2WaveDraw;

    //bluetooth
    private BluetoothUtils mBtUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edBluetoothName = (EditText) findViewById(R.id.edBluetoothName);

        btnBluetoothToggle = (Button) findViewById(R.id.btnBluetoothToggle);
        btnSearchOximeters = (Button) findViewById(R.id.btnSearchOximeters);
        tvStatusBar        = (TextView) findViewById(R.id.tvStatusBar);
        tvParamsBar        = (TextView) findViewById(R.id.tvParamsBar);

        //BLUETOOTH
        mBtUtils = BluetoothUtils.getDefaultBluetoothUtils();
        if(mBtUtils.isBleutoothEnabled())
        {
            btnBluetoothToggle.setText(getString(R.string.turn_off_bluetooth));
            btnSearchOximeters.setEnabled(true);
        }
        mBtUtils.setConnectListener(new BluetoothUtils.BTConnectListener() {
            @Override
            public void onFoundDevice(BluetoothDevice device) {
                if(device.getName().equals(edBluetoothName.getText().toString())
                        && device.getAddress().toLowerCase().startsWith("8c:de")){

                    mBtUtils.connect(MainActivity.this,device);
                }
            }

            @Override
            public void onStopScan() {
                tvStatusBar.setText("stop scan");
            }

            @Override
            public void onStartScan() {
                tvStatusBar.setText("start scan");
            }

            @Override
            public void onConnected() {
                tvStatusBar.setText("Device Connected");
                btnSearchOximeters.setEnabled(false);
            }

            @Override
            public void onDisconnected() {
                tvStatusBar.setText("Device Disconnected");
                btnSearchOximeters.setEnabled(true);
            }

            @Override
            public void onReceiveData(byte[] dat) {
                mDataParser.add(dat);
            }
        });



        //******************************** package parse******************************
        mDataParser = new DataParser(DataParser.Protocol.BCI, new DataParser.onPackageReceivedListener() {
            @Override
            public void onPackageReceived(int[] dat) {
                Log.i(TAG, "onPackageReceived: " + Arrays.toString(dat));
                if(mPackageParser == null) {
                    mPackageParser = new PackageParser(MainActivity.this);
                }

                mPackageParser.parse(dat);
            }
        });

        mDataParser.start();
        //*******************************************************************************


        //WaveForm
        SurfaceView sfvSpO2 = (SurfaceView) findViewById(R.id.sfvSpO2);
        WaveFormParams mSpO2WaveParas = new WaveFormParams(3,2,new int[]{0,100});
        mSpO2WaveDraw = new WaveForm(this, sfvSpO2,mSpO2WaveParas);

        //Source Code
        TextView tvGetSource = (TextView) findViewById(R.id.tvGetSource);
        tvGetSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Const.GITHUB_SITE)));
            }
        });

    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case Const.MESSAGE_OXIMETER_PARAMS:
                    tvParamsBar.setText("SpO2: "+ msg.arg1 + "   Pulse Rate:"+msg.arg2);
                    break;
                case Const.MESSAGE_OXIMETER_WAVE:
                    mSpO2WaveDraw.add(msg.arg1);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mBtUtils.registerBroadcastReceiver(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mBtUtils.unregisterBroadcastReceiver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDataParser.stop();
    }



    public void onClick(View v)
    {

        switch(v.getId())
        {
            case R.id.btnBluetoothToggle:
                if(btnBluetoothToggle.getText().toString().equals(getString(R.string.turn_on_bluetooth)))
                {
                    btnBluetoothToggle.setText(R.string.turn_off_bluetooth);
                    //turn on bluetooth
                    if(!mBtUtils.isBleutoothEnabled())
                    {
                        mBtUtils.enableBluetooth();
                    }
                    btnSearchOximeters.setEnabled(true);
                    edBluetoothName.setFocusable(false);
                    edBluetoothName.setFocusableInTouchMode(false);
                }
                else
                {
                    btnBluetoothToggle.setText(R.string.turn_on_bluetooth);

                    mBtUtils.disconnect();

                    //turn off bluetooth
                    if(mBtUtils.isBleutoothEnabled())
                    {
                        mBtUtils.disenableBluetooth();
                    }
                    btnSearchOximeters.setEnabled(false);
                    edBluetoothName.setFocusableInTouchMode(true);
                    edBluetoothName.setFocusable(true);
                    edBluetoothName.requestFocus();
                }
                break;

            case R.id.btnSearchOximeters:
                mBtUtils.startScan(true);
                break;
        }
    }



    @Override
    public void onSpO2ParamsChanged() {
        PackageParser.OxiParams params = mPackageParser.getOxiParams();
        mHandler.obtainMessage(Const.MESSAGE_OXIMETER_PARAMS,params.getSpo2(),params.getPulseRate()).sendToTarget();
    }

    @Override
    public void onSpO2WaveChanged(int wave) {
         mHandler.obtainMessage(Const.MESSAGE_OXIMETER_WAVE,wave,0).sendToTarget();
    }
}
