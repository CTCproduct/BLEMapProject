package com.example.ctc.blemapapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.os.Bundle;

public class MainActivity extends Activity {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}
