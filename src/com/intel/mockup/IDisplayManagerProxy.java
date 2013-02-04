package com.intel.mockup;

/**
 * 
 * @author Jinmiao
 * @version 0.5
 * 
 */
public interface IDisplayManagerProxy {

    void scanWifiDisplays();

    void connectWifiDisplay(String deviceAddress);

    void disconnectWifiDisplay();

    void renameWifiDisplay(String deviceAddress, String alias);

    void forgetWifiDisplay(String deviceAddress);

    IWifiDisplayStatusProxy getWifiDisplayStatus();

    static final String WIFI_DISPLAY_ON = "wifi_display_on";

    static final String PACKAGE = "android.hardware.display.DisplayManager";

    static final String ACTION_WIFI_DISPLAY_STATUS_CHANGED = "android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED";

    static final String EXTRA_WIFI_DISPLAY_STATUS = "android.hardware.display.extra.WIFI_DISPLAY_STATUS";

}
