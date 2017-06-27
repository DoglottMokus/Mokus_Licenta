package com.example.mokus.licenta;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class FirstActivity extends AppCompatActivity {
    private static final String TAG = "bluetooth2";

    TextView arduinoread1, arduinoread2, arduinoread3, szazalek;
    static Handler h;
    Button door1, door2;


    final int RECIEVE_MESSAGE = 1;      // Status  for Handler
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "98:D3:31:FC:13:73";

    /**
     * Called when the activity is first created.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        arduinoread1 = (TextView) findViewById(R.id.arduinoread1);// for display the received data from the Arduino
        arduinoread2 = (TextView) findViewById(R.id.arduinoread2);
        arduinoread3 = (TextView) findViewById(R.id.arduinoread3);
        szazalek = (TextView) findViewById(R.id.szazalek);
        door1 = (Button) findViewById(R.id.door1);
        door2 = (Button) findViewById(R.id.door2);



        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex1 = sb.indexOf("!");                              // determine the end-of-line
                        int endOfLineIndex2 = sb.indexOf("@");
                        int endOfLineIndex3 = sb.indexOf("#");
                        int endOfLineIndex4 = sb.indexOf("%");
                        if (endOfLineIndex1 != -1) {                                           // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex1);               // extract string
                            sb.delete(0, sb.length());                                       // and clear
                            arduinoread1.setText(sbprint); // update TextView
                            Log.d(TAG, "...String:" + sb.toString() + "Byte:" + msg.arg1 + "...");
                        }

                        if (endOfLineIndex2 != -1) {                                           // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex2);               // extract string
                            sb.delete(0, sb.length());                                       // and clear
                            arduinoread2.setText(sbprint); // update TextView
                        }
                        if (endOfLineIndex3 != -1) {                                           // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex3);               // extract string
                            sb.delete(0, sb.length());                                       // and clear
                            arduinoread3.setText(sbprint); // update TextView
                        }
                        if (endOfLineIndex4 != -1) {                                           // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex4);               // extract string
                            sb.delete(0, sb.length());                                       // and clear
                            szazalek.setText(sbprint); // update TextView
                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            }

            ;
        };
        door1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OpenDoorOne();      //method to turn on
            }
        });
        door2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OpenDoorTwo();      //method to turn on
            }
        });
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
        OutputStream outStream = null;

        // Set up onClick listeners for buttons to send 1 or 2


    }
    private void OpenDoorOne()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("1".toString().getBytes());
                btSocket.getOutputStream().write(":".toString().getBytes());
            }
            catch (Exception e)
            {
                Log.d(TAG, "" + e.getMessage() + "...");
            }

        }
    }
    private void OpenDoorTwo()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("2".toString().getBytes());
                btSocket.getOutputStream().write(":".toString().getBytes());
            }
            catch (Exception e)
            {
                Log.d(TAG, "" + e.getMessage() + "...");
            }

        }
    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectedThread extends Thread {
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                    Log.d(TAG, "Van adat megpedig a kovetkezo: " + bytes);
                } catch (IOException e) {
                    Log.d(TAG, "" + e.getMessage() + "...");
                    break;
                }
            }
        }

        private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

            return  device.createRfcommSocketToServiceRecord(MY_UUID);
            //creates secure outgoing connecetion with BT device using UUID
        }



    }

}



