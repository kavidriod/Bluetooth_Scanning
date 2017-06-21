package com.apps.bluetooth_scanning;

/**
 * Created by Kavitha on 6/21/2017.
 */

public class ScannedDevices {


    public ScannedDevices(String name, String macAddress) {
        this.name = name;
        this.macAddress = macAddress;
    }

    private String name,macAddress;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
