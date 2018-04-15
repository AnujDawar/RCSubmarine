package com.example.anujdawar.rcsubmarine;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SplashScreenActivity extends AppCompatActivity
{
    private static final String TAG = "SplashScreenActivity";
    private int REQUEST_ENABLE_BLUETOOTH = 1;
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices = new HashSet<BluetoothDevice>();
    private BroadcastReceiver mReceiver;
    private IntentFilter filter;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mmSocket;
    Thread connectedThread, tempthread;
    boolean getOuttaLoop = false;
    int tempVariable = 0;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private BluetoothDevice mmDevice;

    private interface MessageConstants
    {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler1 =new Handler()
    {
        public void handleMessage(Message msgDisplay)
        {
            Toast.makeText(SplashScreenActivity.this, msgDisplay.obj.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            byte[] readBuf = (byte[])msg.obj;
            String string = new String(readBuf);

            if(string.contains("DONE"))
                string = "ThankYou";

            Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        init();

        if(bluetoothAdapter == null)
        {
            Toast.makeText(this, "No Bluetooth Detected", Toast.LENGTH_SHORT).show();
            finish();
        }

        else if(!bluetoothAdapter.isEnabled())
            turnOnBT();

        getPairedDevices();
        startDiscovery();
    }

    private void init()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        pairedDevices = bluetoothAdapter.getBondedDevices();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
                    if(bluetoothAdapter.getState() == bluetoothAdapter.STATE_OFF)
                        turnOnBT();

                else if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    Toast.makeText(context, "new Device : " + deviceName, Toast.LENGTH_SHORT).show();
                }
            }
        };

        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    private void startDiscovery()
    {
        bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.startDiscovery();
    }

    private void getPairedDevices()
    {
        pairedDevices = bluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equalsIgnoreCase("HC05"))
                {
                    Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();
                    callThread(device);
                }
            }
        }
    }

    private void callThread(BluetoothDevice device)
    {
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            Toast.makeText(this, "2", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Socket's create() method failed", e);
            Toast.makeText(SplashScreenActivity.this, "Socket's create() method failed", Toast.LENGTH_SHORT).show();
        }

        mmSocket = tmp;

        tempthread = new Thread(new Runnable() {
            @Override
            public void run() {

                bluetoothAdapter.cancelDiscovery();

                tempVariable++;
                getOuttaLoop = false;

                while (!getOuttaLoop) {
                    try {
                        mmSocket.connect();
                        Log.e("DAWAR", "\n\n\n\n\nereeeeee\n\n\n");
                    } catch (IOException connectException) {
                        try {
                            Log.e("", "trying fallback...");
                            mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
                            mmSocket.connect();
                            Log.e("", "Connected");
                            getOuttaLoop = true;
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            try {
                                getOuttaLoop = false;
                                mmSocket.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                        manageMyConnectedSocket(mmSocket);
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        tempthread.start();
    }

    private void manageMyConnectedSocket(BluetoothSocket mmSocket)
    {
        Toast.makeText(getApplicationContext(), "4", Toast.LENGTH_SHORT).show();
        if(getOuttaLoop)
            connectedThreadFunction(mmSocket);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_CANCELED)
        {
            Toast.makeText(this, "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void turnOnBT()
    {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(mReceiver);
    }

    public void connectedThreadFunction(BluetoothSocket socket)
    {
        Toast.makeText(getApplicationContext(), "5", Toast.LENGTH_SHORT).show();
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            Toast.makeText(getApplicationContext(), "6", Toast.LENGTH_SHORT).show();
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        connectedThread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                mmBuffer = new byte[1024];
                int numBytes; // bytes returned from read()

                while (true) {
                    try {
                        numBytes = mmInStream.read(mmBuffer);

                        Message readMsg = mHandler.obtainMessage(
                                MessageConstants.MESSAGE_READ, numBytes, -1,
                                mmBuffer);
                        readMsg.sendToTarget();
                    } catch (IOException e) {
                        Log.d(TAG, "Input stream was disconnected", e);
                        break;
                    }
                }
            }
        });
        connectedThread.start();
    }

    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);

//            Message writtenMsg = mHandler.obtainMessage(
//                    MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
//            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

//            Message writeErrorMsg =
//                    mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
//            Bundle bundle = new Bundle();
//            bundle.putString("toast",
//                    "Couldn't send data to the other device");
//            writeErrorMsg.setData(bundle);
//            mHandler.sendMessage(writeErrorMsg);
        }
    }
}