package com.jayanthchennamangalam.standup;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.String;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Button;
import android.os.CountDownTimer;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.media.MediaPlayer;


public class MainActivity extends Activity {

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // MAC-address of Bluetooth module (HC-06)
    private final static String address = "20:14:10:15:01:50";
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothSocket mBluetoothSocket = null;
    private TextView tv = null;
    private BufferedInputStream inStream = null;
    private Handler mHandler = null;
    private int mInterval = 1000;         /* ms */
    private boolean counterRunning = false;
    private CountDownTimer cdt = new CountDownTimer(30000, 1000) {

        public void onTick(long millisUntilFinished) {
            tv.setText("User is sitting. Seconds remaining: " + millisUntilFinished / 1000);
        }

        public void onFinish() {
            tv.setText("STAND UP!");
            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), getResources().getIdentifier("standup", "raw", getPackageName()));
            mediaPlayer.start(); // no need to call prepare(); create() does that for you
        }
    };
    private Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                int bytesAvailable = inStream.available();
                if (bytesAvailable > 0) {
                    byte[] packetBytes = new byte[bytesAvailable];
                    inStream.read(packetBytes);
                    if (packetBytes[0] == '0') {
                        tv.setText("User is standing.");
                        if (counterRunning) {
                            cdt.cancel();
                            counterRunning = false;
                        }
                    } else {
                        tv.setText("User is sitting.");
                        /* start countdown timer */
                        if (!counterRunning) {
                            cdt.start();
                            counterRunning = true;
                        }
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "nothing available", Toast.LENGTH_SHORT)
                            .show();
                }
            } catch (IOException ex) {
                Toast.makeText(getApplicationContext(), "Error foobarbaz", Toast.LENGTH_SHORT)
                        .show();
            }
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tvMessage);
        tv.setText("Waiting...");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            tv.setText("Bluetooth is not supported.");
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Bluetooth enabled.", Toast.LENGTH_SHORT)
                    .show();
        } else {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth not enabled.", Toast.LENGTH_SHORT)
                        .show();
            }
            return;
        }
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
        try {
            mBluetoothSocket = createBluetoothSocket(mBluetoothDevice);
        } catch (IOException e1) {
            Toast.makeText(getApplicationContext(), "Error bar", Toast.LENGTH_SHORT)
                    .show();
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        mBluetoothAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        //Log.d(TAG, "...Connecting...");
        try {
            mBluetoothSocket.connect();
            //Log.d(TAG, "...Connection ok...");
        } catch (IOException e) {
            try {
                mBluetoothSocket.close();
            } catch (IOException e2) {
                Toast.makeText(getApplicationContext(), "Error foo", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        // Create a data stream so we can talk to server.
        //Log.d(TAG, "...Create Socket...");
        tv.setText("Connected!");

        try {
            inStream = new BufferedInputStream(mBluetoothSocket.getInputStream());
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error baz", Toast.LENGTH_SHORT)
                    .show();
        }
        tv.setText("Waiting for status update...");

       /*Button button = (Button) findViewById(R.id.btnUpdate);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Toast.makeText(getApplicationContext(), "Button clicked.", Toast.LENGTH_SHORT)
                        .show();

                try {
                    int bytesAvailable = inStream.available();
                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        inStream.read(packetBytes);
                        if (packetBytes[0] == '0') {
                            tv.setText("User is standing.");
                        } else {
                            tv.setText("User is sitting.");
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "nothing available", Toast.LENGTH_SHORT)
                                .show();
                    }
                } catch (IOException ex) {
                    Toast.makeText(getApplicationContext(), "Error foobarbaz", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });*/

        mHandler = new Handler();
        mHandler.post(mStatusChecker);
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        //if(Build.VERSION.SDK_INT >= 10){
        try {
            final Method m = mBluetoothDevice.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
            return (BluetoothSocket) m.invoke(mBluetoothDevice, MY_UUID);
        } catch (Exception e) {
            //Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            Toast.makeText(getApplicationContext(), "Error creating insecure connection", Toast.LENGTH_SHORT)
                    .show();
        }
        //}
        return mBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
