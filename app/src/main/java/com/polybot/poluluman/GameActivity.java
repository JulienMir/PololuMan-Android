package com.polybot.poluluman;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static com.polybot.poluluman.MainActivity.INTENT_GRID_SETUP;
import static com.polybot.poluluman.MainActivity.INTENT_MACS;
import static com.polybot.poluluman.MainActivity.INTENT_NB_POLOLU;

public class GameActivity extends AppCompatActivity {

    final static String TAG = "GameActivity";
    final int REQUEST_ENABLE_BT = 986420;

    Button connectButton;
    TextView statusText;

    BluetoothAdapter mBluetoothAdapter;
    UUID mUUID = UUID.fromString("a88d73db-62cd-4b43-8dce-1f01e7b3ac87");
    boolean enabledBT = false;

    BluetoothDevice[] pololus;
    int foundDevices = 0;

    private String[] macs;
    private int nbPololu;
    private String serializedGrid;

    // Ecoute pour des connexions BT
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Pololuman", mUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    socket.getRemoteDevice()
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                for(int i=0 ; i<nbPololu ; i++) {
                    if(device.getAddress().equals(macs[i])) {
                        Log.e(TAG, "Found a device!");

                        pololus[i] = device;
                        foundDevices++;

                        // Tout les pololus ont été trouvés
                        if(foundDevices == nbPololu) {
                            mBluetoothAdapter.cancelDiscovery();
                            device.connectGatt()
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = getIntent();
        nbPololu = i.getIntExtra(INTENT_NB_POLOLU, 2);
        macs = i.getStringArrayExtra(INTENT_MACS);
        serializedGrid = i.getStringExtra(INTENT_GRID_SETUP);

        pololus = new BluetoothDevice[nbPololu];

        connectButton = (Button)findViewById(R.id.connectButton);
        statusText = (TextView)findViewById(R.id.statusText);

        connectButton.setOnClickListener(this);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // Setting up Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_CANCELED) {
                toast("Activez le Bluetooth!");
            }
            else {
                enabledBT = true;
                mBluetoothAdapter.startDiscovery();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.connectButton:
                if (!mBluetoothAdapter.isEnabled()) {
                    enabledBT = false;
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                else
                {

                }
                break;
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket sock) {
    }

    public void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}
