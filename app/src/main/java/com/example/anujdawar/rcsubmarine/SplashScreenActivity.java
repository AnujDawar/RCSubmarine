package com.example.anujdawar.rcsubmarine;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class SplashScreenActivity extends AppCompatActivity
{
    private int REQUEST_ENABLE_BLUETOOTH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        setUpBluetoothConnection();
    }

    private void setUpBluetoothConnection()
    {
        //  get device's only bluetooth adapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //  if bluetoothAdapter is null, the device does not support bluetooth functionality
        if(bluetoothAdapter == null)
        {
            Toast.makeText(SplashScreenActivity.this, "The device does not support bluetooth feature", Toast.LENGTH_SHORT).show();

            //  don't proceed
        }

        else
        {
            //  bluetooth functionality is supported, enable bluetooth
            if(! bluetoothAdapter.isEnabled())
            {
                //  bluetooth is disabled.
                //  launch intent to enable the bluetooth

                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            //  user accepted connection
            Toast.makeText(this, "selected OK", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "selected CANCEL", Toast.LENGTH_SHORT).show();
        }
    }
}