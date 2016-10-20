package com.example.ctc.blemapapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
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
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends Activity implements LocationListener,View.OnClickListener,DialogInterface.OnClickListener {
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

        final BluetoothManager manager = (BluetoothManager) this.getSystemService(this.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        // Bluetoothサポートチェック
        final boolean isBluetoothSupported = mBluetoothAdapter != null;
        if (!isBluetoothSupported) {
            // 非サポート時の処理　あとで書く
            return;
        }

        // Bluetoothオンかチェック
        if (!mBluetoothAdapter.isEnabled()) {
            // オフ時の処理　あとで書く
            return;
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

    public void stopScan() {
        // スキャン停止
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
}
