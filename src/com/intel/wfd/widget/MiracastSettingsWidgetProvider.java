package com.intel.wfd.widget;

import com.intel.mockup.IDisplayManagerProxy;
import com.intel.mockup.IWifiDisplayStatusProxy;
import com.intel.mockup.ProxyFactory;
import com.intel.wfd.MiracastSettings;
import com.intel.wfd.R;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MiracastSettingsWidgetProvider extends AppWidgetProvider {
    final static String TAG = "MiracastSettingsWidgetProvider";

    final static String MIRACASTSETTINGS_PLAY_ON = "com.intel.wfd.widget.miracast_play_on";
    final static String MIRACASTSETTINGS_DISCONNECT = "com.intel.wfd.widget.miracast_disconnect";

    private RemoteViews mRemoteViews;
    private IDisplayManagerProxy mMyDisplayManager;
    public IWifiDisplayStatusProxy mMyWifiDisplayStatus;

    private static MiracastSettingsWidgetProvider sInstance;

    public static synchronized MiracastSettingsWidgetProvider getInstance() {
        if (sInstance == null) {
            sInstance = new MiracastSettingsWidgetProvider();
        }
        return sInstance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onReceive");
        defaultAppWidget(context, null);
        
        if (mRemoteViews == null) {
            mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget_info);
        }
        if (mMyDisplayManager == null) {
            DisplayManager mDisplayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            mMyDisplayManager = ProxyFactory.newManagerProxyInstance(mDisplayManager);
        }

        if (intent.getAction().equals(MIRACASTSETTINGS_PLAY_ON)) {
            Log.i(TAG, "Toast: Turn ON / OFF");
            Toast.makeText(context, "Turn ON / OFF", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(MIRACASTSETTINGS_DISCONNECT)) {
            Log.i(TAG, "Toast: miracastsettings disconnect");
            if (true ||(mMyWifiDisplayStatus != null
                    && (mMyWifiDisplayStatus.getActiveDisplayState() == IWifiDisplayStatusProxy.DISPLAY_STATE_CONNECTED))) {
                mMyDisplayManager.disconnectWifiDisplay();
                Toast.makeText(context, "Disconnect", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Not Connect", Toast.LENGTH_SHORT).show();
            }
        } else if (intent.getAction().equals(IDisplayManagerProxy.ACTION_WIFI_DISPLAY_STATUS_CHANGED)) {
            Log.i(TAG, "ACTION_WIFI_DISPLAY_STATUS_CHANGED");
            Object statusObj = intent.getParcelableExtra(IDisplayManagerProxy.EXTRA_WIFI_DISPLAY_STATUS);
            IWifiDisplayStatusProxy status = ProxyFactory.newStatusProxyInstance(statusObj);
            
            mMyWifiDisplayStatus = status;
        }
        performUpdate(context, null);

        // AppWidgetManager appWidgetManger =
        // AppWidgetManager.getInstance(context);
        // int[] appIds = appWidgetManger.getAppWidgetIds(new
        // ComponentName(context, MiracastSettingsWidgetProvider.class));
        // appWidgetManger.updateAppWidget(appIds, mRemoteViews);
        // super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onUpdate");
//        super.onUpdate(context, appWidgetManager, appWidgetIds);
        defaultAppWidget(context, appWidgetIds);
        
        context.startService(new Intent(context, UpdateService.class));
    }

    private void defaultAppWidget(Context context, int[] appWidgetIds) {
        Log.i(TAG, "defaultAppWidget");
        Resources res = context.getResources();
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget_info);

        mRemoteViews.setTextViewText(R.id.app_name, res.getText(R.string.wifi_display_settings_title));

        // Link actions buttons to intents
        linkButtons(context, mRemoteViews);
        pushUpdate(context, appWidgetIds, mRemoteViews);
    }
    
    /**
     * Check against {@link AppWidgetManager} if there are any instances of this widget.
     */
    private boolean hasInstances(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, this.getClass()));
        return (appWidgetIds.length > 0);
    }
    
    /**
     * Handle a change notification coming over from {@link PlaybackService}
     */
    public void notifyChange(Context context) {
        Log.i(TAG,"hasInstances(service) = "+hasInstances(context));
        if (hasInstances(context)) {
            performUpdate(context, null);
        }
    }

    /**
     * Update all active widget instances by pushing changes
     */
    private void performUpdate(Context context, int[] appWidgetIds) {
        Log.i(TAG, "performUpdate");
        final Resources res = context.getResources();
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_info);
        
        Intent intent;
        PendingIntent pendingIntent;
        
        if(mMyWifiDisplayStatus != null){
            Log.i(TAG, "performUpdate state=" + mMyWifiDisplayStatus.getActiveDisplayState() + ",featureState=" + mMyWifiDisplayStatus.getFeatureState());
            views.setViewVisibility(R.id.text_separator,View.VISIBLE);
            
            switch (mMyWifiDisplayStatus.getFeatureState()) {
            case IWifiDisplayStatusProxy.FEATURE_STATE_DISABLED:
                views.setTextViewText(R.id.state, "Feature state disabled");
                break;
            case IWifiDisplayStatusProxy.FEATURE_STATE_OFF:
                views.setTextViewText(R.id.state, "Feature state off");
                break;
            case IWifiDisplayStatusProxy.FEATURE_STATE_ON:
                views.setTextViewText(R.id.state, "Feature state on");
                break;
            default:
                break;
            }

            switch (mMyWifiDisplayStatus.getActiveDisplayState()) {
            case IWifiDisplayStatusProxy.DISPLAY_STATE_NOT_CONNECTED:
                views.setTextViewText(R.id.state, "not connected");
                views.setImageViewResource(R.id.play, R.drawable.btn_play_normal_jb_dark);
                
                intent = new Intent(MIRACASTSETTINGS_DISCONNECT);
                pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                views.setOnClickPendingIntent(R.id.play, pendingIntent);
                break;
            case IWifiDisplayStatusProxy.DISPLAY_STATE_CONNECTING:
                views.setTextViewText(R.id.state, res.getText(R.string.wifi_display_status_connecting));
                views.setImageViewResource(R.id.play, R.drawable.btn_play_normal_jb_dark);
                
                intent = new Intent(MIRACASTSETTINGS_DISCONNECT);
                pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                views.setOnClickPendingIntent(R.id.play, pendingIntent);
                break;
            case IWifiDisplayStatusProxy.DISPLAY_STATE_CONNECTED:
                views.setTextViewText(R.id.state, res.getText(R.string.wifi_display_status_connected));
                views.setImageViewResource(R.id.play, R.drawable.btn_pause_normal_jb_dark);
                
                intent = new Intent(MIRACASTSETTINGS_DISCONNECT);
                pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                views.setOnClickPendingIntent(R.id.play, pendingIntent);
                break;
            default:
                break;
            }
        }
        pushUpdate(context, appWidgetIds, views);
    }

    /**
     * Link up various button actions using {@link PendingIntents}.
     * 
     * @param playerActive
     *            True if player is active in background, which means widget
     *            click will launch {@link Player},
     */
    private void linkButtons(Context context, RemoteViews views) {
        Log.i(TAG, "linkButtons");
        // Connect up various buttons and touch events
        Intent intent;
        PendingIntent pendingIntent;

        intent = new Intent(context, MiracastSettings.class);
        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.app_name, pendingIntent);
        views.setOnClickPendingIntent(R.id.albumartframe, pendingIntent);

        intent = new Intent(MIRACASTSETTINGS_DISCONNECT);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.play, pendingIntent);

        intent = new Intent(MIRACASTSETTINGS_PLAY_ON);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.stop, pendingIntent);
    }

    private void pushUpdate(Context context, int[] appWidgetIds, RemoteViews views) {
        Log.i(TAG, "pushUpdate");
        // Update specific list of appWidgetIds if given, otherwise default to
        // all
        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null) {
            gm.updateAppWidget(appWidgetIds, views);
        } else {
            gm.updateAppWidget(new ComponentName(context, this.getClass()), views);
        }
    }
    
    //Update service
    public static class UpdateService extends Service{
        static final String TAG = "UpdateService";
        MiracastSettingsWidgetProvider mMiracastSettingsWidgetProvider = MiracastSettingsWidgetProvider.getInstance();

        @Override
        public void onCreate() {
            Log.i(TAG,"onCreate");
            // TODO Auto-generated method stub
            super.onCreate();
        }

        @Override
        public IBinder onBind(Intent intent) {
            Log.i(TAG,"onBind");
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        @Deprecated
        public void onStart(Intent intent, int startId) {
            Log.i(TAG,"onStart");
            // TODO Auto-generated method stub
            super.onStart(intent, startId);
            mMiracastSettingsWidgetProvider.notifyChange(UpdateService.this);
            
        }
    }
    
}
