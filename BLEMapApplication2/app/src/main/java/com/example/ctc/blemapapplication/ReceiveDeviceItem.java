package com.example.ctc.blemapapplication;

public class ReceiveDeviceItem {
    private String name;
    private double longitude;
    private double latitude;

    public ReceiveDeviceItem(String name, double longitude, double latitude) {
        super();

        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

}
