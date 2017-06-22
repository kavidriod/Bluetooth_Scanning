package com.apps.bluetooth_scanning;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();
    ToggleButton scanToggleButton;
    BluetoothAdapter bluetoothAdapter;
    public final static int REQUEST_BLUETOOTH = 1;
    private AbsListView listView;
List<ScannedDevices> scannedDevices;

    private final int  PERMISSION_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        scanToggleButton = (ToggleButton) findViewById(R.id.scanToggleButton);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        listView = (AbsListView) findViewById(android.R.id.list);
        scannedDevices = new ArrayList<>();


        scanToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Log.i("Device Version", "  "+ Build.VERSION.SDK_INT);
                if (Build.VERSION.SDK_INT >= 23) {  //Build.VERSION_CODES.M
                    //Grant Permission in runtime
                    requestPermissionAtRuntime();
                } else {
                    //User already granted permission before Installation
                }

                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
                    intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

                    Log.i(TAG," isChecked ? "+isChecked);

                    if (isChecked){
                        scannedDevices.clear();
                        getApplicationContext().registerReceiver(scanBroadcastReceiver,intentFilter);
                        Log.i(TAG," bluetoothAdapters.isDiscovering() ? "+bluetoothAdapter.isDiscovering());
                        if (bluetoothAdapter.isDiscovering()){
                            bluetoothAdapter.cancelDiscovery();
                        }
                        bluetoothAdapter.startDiscovery();

                    }else {
                        getApplicationContext().unregisterReceiver(scanBroadcastReceiver);
                        bluetoothAdapter.cancelDiscovery();
                    }


                }else {
                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBTIntent,REQUEST_BLUETOOTH);
                }

            }
        });

    }


    private boolean requestPermissionAtRuntime() {

        String[] permissionsToRequest = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        List<String> listPermissionsNeeded = new ArrayList<String>();
        int result;
        for (String p : permissionsToRequest) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_REQUEST_CODE);
            return false;
        }return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                }else {
                    requestPermissionAtRuntime();
                }
                break;

            default:
                break;
        }
    }

    BroadcastReceiver scanBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG," action "+intent.getAction());
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.i(TAG," action "+bluetoothDevice.getAddress());
                Log.i(TAG," action "+bluetoothDevice.getName());
                String name = "Unknown",macaddress = "Unknown";

                if (bluetoothDevice.getName() != null)
                    name = bluetoothDevice.getName();

                if (bluetoothDevice.getAddress() != null)
                    macaddress = bluetoothDevice.getAddress();

                ScannedDevices eachscannedDevices = new ScannedDevices(name,macaddress);


                if (!scannedDevices.contains(eachscannedDevices)){
                    scannedDevices.add(eachscannedDevices);
                    DeviceListAdapter  deviceListAdapter = new DeviceListAdapter(getApplicationContext(),scannedDevices);
                    listView.setAdapter(deviceListAdapter);
                }



            }else  if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                Log.i(TAG," ACTION_DISCOVERY_STARTED ?"+intent.getAction());
                scanToggleButton.setChecked(true);
            }else  if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                Log.i(TAG," ACTION_DISCOVERY_FINISHED ?"+intent.getAction());
                scanToggleButton.setChecked(false);
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_BLUETOOTH:
                if (resultCode == RESULT_OK){
                    showToast("BT Enabled");
                }else {
                    showToast("BT is not Enabled");
                }
                break;
        }
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    class DeviceListAdapter extends BaseAdapter {

        Context context;
        List<ScannedDevices> pairedDevices;
        LayoutInflater layoutInflater;

        public  DeviceListAdapter(Context context,List<ScannedDevices> pairedDevices){
            this.context = context;
            this.pairedDevices =  pairedDevices;
            layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);



        }

        @Override
        public int getCount() {
            return pairedDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return pairedDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;

            ScannedDevices eachPairedDevices = pairedDevices.get(position);

            if (convertView == null){
                convertView = layoutInflater.inflate(R.layout.scanned_items,null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) convertView.findViewById(R.id.titleTextView);
                viewHolder.macAddress = (TextView) convertView.findViewById(R.id.macAddress);

                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }




            viewHolder.deviceName.setText(eachPairedDevices.getName());
            viewHolder.macAddress.setText(eachPairedDevices.getMacAddress());

            return convertView;
        }
    }


    private  class ViewHolder{
        TextView deviceName,macAddress;
    }
}
