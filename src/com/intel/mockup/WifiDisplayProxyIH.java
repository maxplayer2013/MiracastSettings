package com.intel.mockup;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.util.Log;

/**
 * 
 * @author Jinmiao
 * @version 0.5
 * 
 */
public class WifiDisplayProxyIH extends ProxyIH {

    public WifiDisplayProxyIH(Object target, String className) {
        super(target, className);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = new Integer(0);
        try {
            Method m = getMethod(method, args);
            Log.i("MyWifiDisplayIH  ", "invoke m =" + m.getName());
            m.setAccessible(true);
            if (m.getName().equals("equals")) {
                IWifiDisplayProxy obj = (IWifiDisplayProxy) args[0];
                WifiDisplayProxyIH ih = (WifiDisplayProxyIH) Proxy.getInvocationHandler(obj);
                Object target = ih.mTargetObj;
                // Object target = mTargetObj;

                args = new Object[] { target };
            }
            ret = m.invoke(mTargetObj, args);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
