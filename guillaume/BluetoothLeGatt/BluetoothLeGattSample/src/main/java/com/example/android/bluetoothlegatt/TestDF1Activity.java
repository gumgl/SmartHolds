package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class TestDF1Activity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    Uri notification;
    Ringtone r;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                Log.d("test", "on se connecte !");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                startNotify(null);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Toast.makeText(getApplicationContext(), intent.getStringExtra(BluetoothLeService.EXTRA_DATA), Toast.LENGTH_SHORT).show();
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                try {
                    r.stop();
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
            };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_df1);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        try {
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                System.out.print("heyyo2"+mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }
// Loops through available GATT Services.

    /*for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
        Log.d("svc", "Service UUID: "+gattService.getUuid().toString());
        for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
            Log.d("chr", "   Charac UUID: "+characteristic.getUuid().toString());
            //final int charaProp = characteristic.getProperties();
           // if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
           //     mBluetoothLeService.readCharacteristic(characteristic);
           //     Log.d("val", "     val: " + characteristic.getStringValue(0));
           // }
        }
    }*/
    public void colorOff(View view) {
        for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
            if (gattService.getUuid().equals(SampleGattAttributes.SERVICE_TEST)) {
                BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(SampleGattAttributes.TST_COLOR_UUID);
                characteristic.setValue("0");
                mBluetoothLeService.writeCharacteristic(characteristic);
            }
        }
    }
    public void colorRed(View view) {
        for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
            if (gattService.getUuid().equals(SampleGattAttributes.SERVICE_TEST)) {
                BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(SampleGattAttributes.TST_COLOR_UUID);
                characteristic.setValue("1");
                mBluetoothLeService.writeCharacteristic(characteristic);
            }
        }
    }
    public void colorGreen(View view) {
        for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
            if (gattService.getUuid().equals(SampleGattAttributes.SERVICE_TEST)) {
                BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(SampleGattAttributes.TST_COLOR_UUID);
                characteristic.setValue("2");
                mBluetoothLeService.writeCharacteristic(characteristic);
            }
        }
    }
    public void colorBlue(View view) {
        for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
            if (gattService.getUuid().equals(SampleGattAttributes.SERVICE_TEST)) {
                BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(SampleGattAttributes.TST_COLOR_UUID);
                characteristic.setValue("4");
                mBluetoothLeService.writeCharacteristic(characteristic);
            }
        }
    }
    public void startNotify(View view) {
        for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
            // Write to device, enabling tap notifications
            if (gattService.getUuid().equals(SampleGattAttributes.SERVICE_ACC)) {
                Log.d("test", "Inside Service");
                BluetoothGattCharacteristic characteristicENABLE = gattService.getCharacteristic(SampleGattAttributes.ACC_ENABLE_UUID);
                if (characteristicENABLE != null) {
                    Log.d("test", "Before setu^char");
                    byte[] value= {(byte) 0x04};
                    characteristicENABLE.setValue(value);
                    mBluetoothLeService.writeCharacteristic(characteristicENABLE);
                }
                BluetoothGattCharacteristic characteristicTAP = gattService.getCharacteristic(SampleGattAttributes.ACC_TAP_DATA_UUID);
                if (characteristicTAP != null) {
                    Log.d("ntf", "at least we setup the notifications...");
                    mBluetoothLeService.setCharacteristicNotification(characteristicTAP, true);
                }
            }
        }
    }
    public void stopNotify(View view) {
        // Write to device, disabling tap notifications
        for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
            if (gattService.getUuid().equals(SampleGattAttributes.SERVICE_ACC)) {
                BluetoothGattCharacteristic characteristicENABLE = gattService.getCharacteristic(SampleGattAttributes.ACC_ENABLE_UUID);
                if (characteristicENABLE != null) {
                    byte[] value= {(byte) 0x00};
                    characteristicENABLE.setValue(value);
                    mBluetoothLeService.writeCharacteristic(characteristicENABLE);
                }
                BluetoothGattCharacteristic characteristicTAP = gattService.getCharacteristic(SampleGattAttributes.ACC_TAP_DATA_UUID);
                if (characteristicTAP != null) {
                    Log.d("ntf", "at least we unsetup the notifications...");
                    mBluetoothLeService.setCharacteristicNotification(characteristicTAP, false);
                }
            }
        }
    }
    public void write01(View view) {
        for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
            if (gattService.getUuid().equals(SampleGattAttributes.ACC_ENABLE_UUID)) {
                BluetoothGattCharacteristic characteristicENABLE = gattService.getCharacteristic(SampleGattAttributes.ACC_TAP_DATA_UUID);
                if (characteristicENABLE != null) {
                    byte[] value= {(byte) 0x04};
                    characteristicENABLE.setValue(value);
                    mBluetoothLeService.writeCharacteristic(characteristicENABLE);
                }
            }
        }
    }
    public void write00(View view) {
        for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
            if (gattService.getUuid().equals(SampleGattAttributes.ACC_ENABLE_UUID)) {
                BluetoothGattCharacteristic characteristicENABLE = gattService.getCharacteristic(SampleGattAttributes.ACC_TAP_DATA_UUID);
                if (characteristicENABLE != null) {
                    byte[] value= {(byte) 0x00};
                    characteristicENABLE.setValue(value);
                    mBluetoothLeService.writeCharacteristic(characteristicENABLE);
                }
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
