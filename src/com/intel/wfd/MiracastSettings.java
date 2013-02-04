package com.intel.wfd;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.intel.mockup.IDisplayManagerProxy;
import com.intel.mockup.IWifiDisplayProxy;
import com.intel.mockup.IWifiDisplayStatusProxy;
import com.intel.mockup.ProxyFactory;
import com.intel.ui.ProgressCategory;
import com.intel.wfd.R;

public class MiracastSettings extends PreferenceActivity {

    final static String TAG = "WiFiDisplaySettings";

    private static final int MENU_ID_SCAN = Menu.FIRST;

    private Switch mActionBarSwitch;

    private boolean mWifiDisplayOnSetting;

    private DisplayManager mDisplayManager;

    private IDisplayManagerProxy mMyDisplayManager;
    private IWifiDisplayStatusProxy mMyWifiDisplayStatus;
    private IWifiDisplayProxy[] mPairedDisplays;

    private PreferenceGroup mPairedDevicesCategory;
    private ProgressCategory mAvailableDevicesCategory;

    private TextView mEmptyView;

    private Context mContext;

    public MiracastSettings() {
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mContext = getApplicationContext();

        mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);

        addPreferencesFromResource(R.xml.wifi_display_settings);
        // onCreateOptionsMenu(true);

        // reflect
//        InvocationHandler ih = new DisplayManagerProxyIH(mDisplayManager, IDisplayManagerProxy.PACKAGE);
//        Class[] faces = new Class[] { IDisplayManagerProxy.class };
//        mMyDisplayManager = (IDisplayManagerProxy) Proxy.newProxyInstance(IDisplayManagerProxy.class.getClassLoader(),
//                faces, ih);
        mMyDisplayManager = ProxyFactory.newManagerProxyInstance(mDisplayManager);
    }

    @Override
    public void onStart() {
        super.onStart();

        Activity activity = this;
        mActionBarSwitch = new Switch(activity);
        if (activity instanceof PreferenceActivity) {
            PreferenceActivity preferenceActivity = (PreferenceActivity) activity;
            if (preferenceActivity.onIsHidingHeaders() || !preferenceActivity.onIsMultiPane()) {
                final int padding = activity.getResources().getDimensionPixelSize(R.dimen.action_bar_switch_padding);
                mActionBarSwitch.setPadding(0, 0, padding, 0);
                activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
                activity.getActionBar().setCustomView(
                        mActionBarSwitch,
                        new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.END));
            }
        }

        mActionBarSwitch.setOnCheckedChangeListener(mSwitchOnCheckedChangedListener);

        mEmptyView = (TextView) findViewById(android.R.id.empty);
        // getListView().setEmptyView(mEmptyView);

        update();

        if (mMyWifiDisplayStatus.getFeatureState() == IWifiDisplayStatusProxy.FEATURE_STATE_UNAVAILABLE) {
            activity.finish();
        }
    }

    private final CompoundButton.OnCheckedChangeListener mSwitchOnCheckedChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mWifiDisplayOnSetting = isChecked;
            try {
                Settings.Global.putInt(getContentResolver(), IDisplayManagerProxy.WIFI_DISPLAY_ON, isChecked ? 1 : 0);
            } catch (SecurityException e) {
                Log.i(TAG, "SecurityException e 111 : " + e.getMessage());
                mWifiDisplayOnSetting = !isChecked;
                mActionBarSwitch.setOnCheckedChangeListener(null);
                mActionBarSwitch.setChecked(mWifiDisplayOnSetting);
                mActionBarSwitch.setOnCheckedChangeListener(mSwitchOnCheckedChangedListener);

                openDialog(e.getMessage());
            }
        }
    };

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        Context context = this;
        IntentFilter filter = new IntentFilter();
        filter.addAction(IDisplayManagerProxy.ACTION_WIFI_DISPLAY_STATUS_CHANGED);
        context.registerReceiver(mReceiver, filter);

        try {
            getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor(IDisplayManagerProxy.WIFI_DISPLAY_ON), false, mSettingsObserver);
        } catch (SecurityException e) {
            Log.i(TAG, "SecurityException e 333 : " + e.getMessage());
        }

        mMyDisplayManager.scanWifiDisplays();

        update();
    }

    private final ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            update();
        }
    };

    @Override
    public void onPause() {
        super.onPause();

        Context context = this;
        context.unregisterReceiver(mReceiver);

        getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(IDisplayManagerProxy.ACTION_WIFI_DISPLAY_STATUS_CHANGED)) {
                // WifiDisplayStatus status =
                // (WifiDisplayStatus)intent.getParcelableExtra(
                // DisplayManager.EXTRA_WIFI_DISPLAY_STATUS);

                Object statusObj = intent.getParcelableExtra(IDisplayManagerProxy.EXTRA_WIFI_DISPLAY_STATUS);
//                WifiDisplayStatusProxyIH ih = new WifiDisplayStatusProxyIH(statusObj, IWifiDisplayStatusProxy.PACKAGE);
//                Class[] faces = new Class[] { IWifiDisplayStatusProxy.class };
//                IWifiDisplayStatusProxy status = (IWifiDisplayStatusProxy) Proxy.newProxyInstance(
//                        IWifiDisplayStatusProxy.class.getClassLoader(), faces, ih);
                IWifiDisplayStatusProxy status = ProxyFactory.newStatusProxyInstance(statusObj);

                mMyWifiDisplayStatus = status;

                applyState();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // getMenuInflater().inflate(R.menu.options_menu, menu);

        MenuItem item = menu
                .add(Menu.NONE,
                        MENU_ID_SCAN,
                        0,
                        mMyWifiDisplayStatus.getScanState() == IWifiDisplayStatusProxy.SCAN_STATE_SCANNING ? R.string.wifi_display_searching_for_devices
                                : R.string.wifi_display_search_for_devices);
        item.setEnabled(mMyWifiDisplayStatus.getFeatureState() == IWifiDisplayStatusProxy.FEATURE_STATE_ON
                && mMyWifiDisplayStatus.getScanState() == IWifiDisplayStatusProxy.SCAN_STATE_NOT_SCANNING);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ID_SCAN:
            if (mMyWifiDisplayStatus.getFeatureState() == IWifiDisplayStatusProxy.FEATURE_STATE_ON) {
                mMyDisplayManager.scanWifiDisplays();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference instanceof WifiDisplayPreference) {
            WifiDisplayPreference p = (WifiDisplayPreference) preference;
            IWifiDisplayProxy display = p.getDisplay();

            if (display.equals(mMyWifiDisplayStatus.getActiveDisplay())) {
                Log.i(TAG, "display.equals(mMyWifiDisplayStatus.getActiveDisplay())");
                showDisconnectDialog(display);
            } else {
                Log.i(TAG, "connectWifiDisplay::::");
                Log.i(TAG, "!contains(mPairedDisplays, display.getDeviceAddress()) = "+!contains(mPairedDisplays, display.getDeviceAddress()));
                if (!contains(mPairedDisplays, display.getDeviceAddress())) {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setCancelable(true)
                            .setTitle(R.string.wifi_display_connection_erro_title)
                            .setMessage(
                                    Html.fromHtml(getResources().getString(R.string.wifi_display_connection_erro_text,
                                            display.getFriendlyDisplayName())))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub
                                    dialog.dismiss();
                                    return;
                                }

                            }).create();
                    dialog.show();
                }
                Log.i(TAG, "--->>connectWifiDisplay");
                mMyDisplayManager.connectWifiDisplay(display.getDeviceAddress());
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void update() {
        try {
            mWifiDisplayOnSetting = Settings.Global.getInt(getContentResolver(), IDisplayManagerProxy.WIFI_DISPLAY_ON,
                    0) != 0;
        } catch (SecurityException e) {
            Log.i(TAG, "SecurityException e 222 : " + e.getMessage());
        }
        mMyWifiDisplayStatus = mMyDisplayManager.getWifiDisplayStatus();

        Log.i(TAG, "update() mMyWifiDisplayStatus: " + mMyWifiDisplayStatus);
        applyState();
    }

    private void applyState() {
        final int featureState = mMyWifiDisplayStatus.getFeatureState();
        Log.i(TAG, "applyState, featureState:" + featureState);
        mActionBarSwitch.setOnCheckedChangeListener(null);
        Log.i(TAG, "applyState, featureState != MyWifiDisplayStatus.FEATURE_STATE_DISABLED:"
                + (featureState != IWifiDisplayStatusProxy.FEATURE_STATE_DISABLED));
        Log.i(TAG, "applyState, mWifiDisplayOnSetting:" + mWifiDisplayOnSetting);
        mActionBarSwitch.setEnabled(featureState != IWifiDisplayStatusProxy.FEATURE_STATE_DISABLED);
        mActionBarSwitch.setChecked(mWifiDisplayOnSetting);
        mActionBarSwitch.setOnCheckedChangeListener(mSwitchOnCheckedChangedListener);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removeAll();

        if (featureState == mMyWifiDisplayStatus.FEATURE_STATE_ON) {
            final IWifiDisplayProxy[] pairedDisplays = mMyWifiDisplayStatus.getRememberedDisplays();
            final IWifiDisplayProxy[] availableDisplays = mMyWifiDisplayStatus.getAvailableDisplays();
            mPairedDisplays = pairedDisplays;

            if (mPairedDevicesCategory == null) {
                mPairedDevicesCategory = new PreferenceCategory(this);
                mPairedDevicesCategory.setTitle(R.string.wifi_display_paired_devices);
            } else {
                mPairedDevicesCategory.removeAll();
            }
            preferenceScreen.addPreference(mPairedDevicesCategory);

            for (IWifiDisplayProxy d : pairedDisplays) {
                mPairedDevicesCategory.addPreference(createWifiDisplayPreference(d, true));
            }
            if (mPairedDevicesCategory.getPreferenceCount() == 0) {
                preferenceScreen.removePreference(mPairedDevicesCategory);
            }

            if (mAvailableDevicesCategory == null) {
                mAvailableDevicesCategory = new ProgressCategory(this, null, R.string.wifi_display_no_devices_found);
                mAvailableDevicesCategory.setTitle(R.string.wifi_display_available_devices);
            } else {
                mAvailableDevicesCategory.removeAll();
            }
            preferenceScreen.addPreference(mAvailableDevicesCategory);

            for (IWifiDisplayProxy d : availableDisplays) {
                if (!contains(pairedDisplays, d.getDeviceAddress())) {
                    mAvailableDevicesCategory.addPreference(createWifiDisplayPreference(d, false));
                }
            }
            if (mMyWifiDisplayStatus.getScanState() == IWifiDisplayStatusProxy.SCAN_STATE_SCANNING) {
                mAvailableDevicesCategory.setProgress(true);
            } else {
                mAvailableDevicesCategory.setProgress(false);
            }
        } else {
            // mEmptyView
            // .setText(featureState == MyWifiDisplayStatus.FEATURE_STATE_OFF ?
            // R.string.wifi_display_settings_empty_list_wifi_display_off
            // :
            // R.string.wifi_display_settings_empty_list_wifi_display_disabled);
        }

        this.invalidateOptionsMenu();
    }

    private Preference createWifiDisplayPreference(final IWifiDisplayProxy d, boolean paired) {
        Log.i(TAG, "createWifiDisplayPreference");
        WifiDisplayPreference p = new WifiDisplayPreference(this, d);
        if (d.equals(mMyWifiDisplayStatus.getActiveDisplay())) {
            switch (mMyWifiDisplayStatus.getActiveDisplayState()) {
            case IWifiDisplayStatusProxy.DISPLAY_STATE_CONNECTED:
                p.setSummary(R.string.wifi_display_status_connected);
                break;
            case IWifiDisplayStatusProxy.DISPLAY_STATE_CONNECTING:
                p.setSummary(R.string.wifi_display_status_connecting);
                break;
            }
        } else if (paired && contains(mMyWifiDisplayStatus.getAvailableDisplays(), d.getDeviceAddress())) {
            Log.i(TAG, "wifi_display_status_available");
            p.setSummary(R.string.wifi_display_status_available);
        }
        if (paired) {
            Log.i(TAG, "wifi_display_preference");
            p.setWidgetLayoutResource(R.layout.wifi_display_preference);
        }
        return p;
    }

    private final class WifiDisplayPreference extends Preference implements View.OnClickListener {
        private final IWifiDisplayProxy mDisplay;

        public WifiDisplayPreference(Context context, IWifiDisplayProxy display) {
            super(context);

            mDisplay = display;
            setTitle(display.getFriendlyDisplayName());
        }

        public IWifiDisplayProxy getDisplay() {
            return mDisplay;
        }

        @Override
        protected void onBindView(View view) {
            super.onBindView(view);

            ImageView deviceDetails = (ImageView) view.findViewById(R.id.deviceDetails);
            if (deviceDetails != null) {
                deviceDetails.setOnClickListener(this);

                if (!isEnabled()) {
                    TypedValue value = new TypedValue();
                    getContext().getTheme().resolveAttribute(android.R.attr.disabledAlpha, value, true);
                    deviceDetails.setImageAlpha((int) (value.getFloat() * 255));
                }
            }
        }

        @Override
        public void onClick(View v) {
            showOptionsDialog(mDisplay);
        }
    }

    private static boolean contains(IWifiDisplayProxy[] displays, String address) {
        for (IWifiDisplayProxy d : displays) {
            if (d.getDeviceAddress().equals(address)) {
                return true;
            }
        }
        return false;
    }

    private void showDisconnectDialog(final IWifiDisplayProxy display) {
        DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (display.equals(mMyWifiDisplayStatus.getActiveDisplay())) {
                    mMyDisplayManager.disconnectWifiDisplay();
                }
            }
        };

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.wifi_display_disconnect_title)
                .setMessage(
                        Html.fromHtml(getResources().getString(R.string.wifi_display_disconnect_text,
                                display.getFriendlyDisplayName()))).setPositiveButton(android.R.string.ok, ok)
                .setNegativeButton(android.R.string.cancel, null).create();
        dialog.show();
    }

    private void showOptionsDialog(final IWifiDisplayProxy display) {
        View view = this.getLayoutInflater().inflate(R.layout.wifi_display_options, null);
        final EditText nameEditText = (EditText) view.findViewById(R.id.name);
        nameEditText.setText(display.getFriendlyDisplayName());

        DialogInterface.OnClickListener done = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameEditText.getText().toString().trim();
                if (name.isEmpty() || name.equals(display.getDeviceName())) {
                    name = null;
                }
                mMyDisplayManager.renameWifiDisplay(display.getDeviceAddress(), name);
            }
        };
        DialogInterface.OnClickListener forget = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMyDisplayManager.forgetWifiDisplay(display.getDeviceAddress());
            }
        };

        AlertDialog dialog = new AlertDialog.Builder(this).setCancelable(true)
                .setTitle(R.string.wifi_display_options_title).setView(view)
                .setPositiveButton(R.string.wifi_display_options_done, done)
                .setNegativeButton(R.string.wifi_display_options_forget, forget).create();
        dialog.show();
    }

    private void openDialog(String str) {
        Log.d(TAG, "openDialog");
        Dialog dialog = new AlertDialog.Builder(this).setTitle("WiFi Display").setMessage(str)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }

                }).create();
        dialog.show();
    }
}
