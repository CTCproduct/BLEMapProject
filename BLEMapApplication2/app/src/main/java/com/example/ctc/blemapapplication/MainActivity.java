package com.example.ctc.blemapapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends Activity implements LocationListener,View.OnClickListener,DialogInterface.OnClickListener {
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private ReceivedDeviceAdapter mReceiveDeviceAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private LocationManager mLocationManager;
    private double mLat;
    private double mLong;
    private boolean mIsDialogShown;

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIsDialogShown = false;
        ListView receiveDeviceList = (ListView) findViewById(R.id.receiveList);
        receiveDeviceList.setAdapter(mReceiveDeviceAdapter);

        final BluetoothManager manager = (BluetoothManager) this.getSystemService(this.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        // Bluetoothサポートチェック
        final boolean isBluetoothSupported = mBluetoothAdapter != null;
        if (!isBluetoothSupported) {
            Toast.makeText(this, getResources().getString(R.string.bluetooth_unsupported), Toast.LENGTH_SHORT).show();
            finish();
        }

        // Bluetoothオンかチェック
        if (!mBluetoothAdapter.isEnabled()) {
            startBluetoothSetting();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // スキャン開始
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mScanCallback = new IBeaconScanCallback();
        mBluetoothLeScanner.startScan(mScanCallback);
    }

    @Override
    protected void onPause() {
        stopScan();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopScan();
        super.onDestroy();
    }

    private void startBluetoothSetting() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int ResultCode, Intent date){
        if(requestCode == REQUEST_ENABLE_BLUETOOTH){
            if(ResultCode != Activity.RESULT_OK){
                startBluetoothSetting();
            }
        }
    }

    private void stopScan() {
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            mScanCallback = null;
        }
    }

    private void locationStart(){
        Log.d("debug","locationStart()");

        // LocationManager インスタンス生成
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            // GPSを設定するように促す
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
            Log.d("debug", "gpsEnable, startActivity");
        } else {
            Log.d("debug", "gpsEnabled");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);

            Log.d("debug", "checkSelfPermission false");
            return;
        }

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("debug","checkSelfPermission true");

                locationStart();
                return;

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // 緯度経度更新
        mLat = location.getLatitude();
        mLong = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.d("debug", "LocationProvider.AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onClick(View v) {
        if (v != null) {
            switch (v.getId()) {
                //送信ボタンを押したときの処理
                case R.id.sendButton:
                    mIsDialogShown = true;
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getResources().getString(R.string.sending_title))
                            .setMessage(getResources().getString(R.string.sending_message))
                            .setNegativeButton(getResources().getString(R.string.cancel), null)
                            .show();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
                    } else {
                        locationStart();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        mIsDialogShown = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.removeUpdates(this);
    }

    public class IBeaconScanCallback extends ScanCallback {
        private ArrayList<ReceiveDeviceItem> deviceList = new ArrayList<>();
        private ArrayList<BluetoothDevice> addedList = new ArrayList<>();

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (result != null && result.getScanRecord() != null && result.getScanRecord().getDeviceName() != null) {
                if (!isAdded(result.getDevice())) {
                    // TODO : パラメータを取得して saveDevice() を呼び出す
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        // スキャンしたデバイスのリスト保存
        private void saveDevice(BluetoothDevice device, String deviceName, double longitude, double latitude) {
            if (addedList == null) {
                addedList = new ArrayList<>();
            }
            addedList.add(device);
            deviceList.add(new ReceiveDeviceItem(deviceName, longitude, latitude));
            mReceiveDeviceAdapter = new ReceivedDeviceAdapter(MainActivity.this, deviceList);
            mReceiveDeviceAdapter.notifyDataSetChanged();
        }

        // スキャンしたデバイスがリストに追加済みかどうかの確認
        private boolean isAdded(BluetoothDevice device) {
            return addedList != null && addedList.size() > 0 && addedList.contains(device);
        }
    }
}
