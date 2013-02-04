package com.intel.mockup;

/**
 * 
 * @author Jinmiao
 * @version 0.5
 * 
 */
public interface IWifiDisplayStatusProxy {

    /**
     * Returns the state of the Wifi display feature on this device.
     * <p>
     * The value of this property reflects whether the device supports the Wifi
     * display, whether it has been enabled by the user and whether the
     * prerequisites for connecting to displays have been met.
     * </p>
     */
    int getFeatureState();

    /**
     * Returns the current state of the Wifi display scan.
     * 
     * @return One of: {@link #SCAN_STATE_NOT_SCANNING} or
     *         {@link #SCAN_STATE_SCANNING}.
     */
    int getScanState();

    /**
     * Get the state of the currently active display.
     * 
     * @return One of: {@link #DISPLAY_STATE_NOT_CONNECTED},
     *         {@link #DISPLAY_STATE_CONNECTING}, or
     *         {@link #DISPLAY_STATE_CONNECTED}.
     */
    int getActiveDisplayState();

    /**
     * Gets the Wifi display that is currently active. It may be connecting or
     * connected.
     */
    IWifiDisplayProxy getActiveDisplay();

    /**
     * Gets the list of all available Wifi displays as reported by the most
     * recent scan, never null.
     * <p>
     * Some of these displays may already be remembered, others may be unknown.
     * </p>
     */
    IWifiDisplayProxy[] getAvailableDisplays();

    /**
     * Gets the list of all remembered Wifi displays, never null.
     * <p>
     * Not all remembered displays will necessarily be available.
     * </p>
     */
    IWifiDisplayProxy[] getRememberedDisplays();

    static final String PACKAGE = "android.hardware.display.WifiDisplayStatus";

    /** Feature state: Wifi display is not available on this device. */
    static final int FEATURE_STATE_UNAVAILABLE = 0;
    /**
     * Feature state: Wifi display is disabled, probably because Wifi is
     * disabled.
     */
    static final int FEATURE_STATE_DISABLED = 1;
    /** Feature state: Wifi display is turned off in settings. */
    static final int FEATURE_STATE_OFF = 2;
    /** Feature state: Wifi display is turned on in settings. */
    static final int FEATURE_STATE_ON = 3;

    /** Scan state: Not currently scanning. */
    static final int SCAN_STATE_NOT_SCANNING = 0;
    /** Scan state: Currently scanning. */
    static final int SCAN_STATE_SCANNING = 1;

    /** Display state: Not connected. */
    static final int DISPLAY_STATE_NOT_CONNECTED = 0;
    /** Display state: Connecting to active display. */
    static final int DISPLAY_STATE_CONNECTING = 1;
    /** Display state: Connected to active display. */
    static final int DISPLAY_STATE_CONNECTED = 2;

}
