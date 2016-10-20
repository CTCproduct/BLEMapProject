package com.example.ctc.blemapapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import java.util.ArrayList;
import java.util.List;

public class IBeaconScanCallback extends ScanCallback {
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);

        if (result != null && result.getDevice() != null) {
            if (!isAdded(result.getDevice())) {
                saveDevice(result.getDevice());
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
    private void saveDevice(BluetoothDevice device) {
        if (deviceList == null) {
            deviceList = new ArrayList<>();
        }

        deviceList.add(device);
    }

    // スキャンしたデバイスがリストに追加済みかどうかの確認
    private boolean isAdded(BluetoothDevice device) {
        if (deviceList != null && deviceList.size() > 0) {
            return deviceList.contains(device);
        } else {
            return false;
        }
    }
}
