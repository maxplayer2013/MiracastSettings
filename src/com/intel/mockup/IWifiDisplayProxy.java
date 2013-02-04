package com.intel.mockup;

/**
 * 
 * @author Jinmiao
 * @version 0.5
 * 
 */
public interface IWifiDisplayProxy {

    /**
     * Gets the MAC address of the Wifi display device.
     */
    String getDeviceAddress();

    /**
     * Gets the name of the Wifi display device.
     */
    String getDeviceName();

    String getDeviceAlias();

    String getFriendlyDisplayName();

    boolean equals(Object o);

    static final String PACKAGE = "android.hardware.display.WifiDisplay";
}
