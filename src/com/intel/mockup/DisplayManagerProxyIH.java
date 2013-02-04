package com.intel.mockup;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.intel.wfd.MiracastSettings;

/**
 * 
 * @author Jinmiao
 * @version 0.5
 * 
 */
public class DisplayManagerProxyIH extends ProxyIH {

    public DisplayManagerProxyIH(Object target, String className) {
        super(target, className);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = new Integer(0);
        try {
            Method m = getMethod(method, args);
            m.setAccessible(true);
            ret = m.invoke(mTargetObj, args);

            if (method.getName().equals("getWifiDisplayStatus")) {

                WifiDisplayStatusProxyIH ih = new WifiDisplayStatusProxyIH(ret, IWifiDisplayStatusProxy.PACKAGE);
                @SuppressWarnings("rawtypes")
                Class[] faces = new Class[] { IWifiDisplayStatusProxy.class };
                ret = (IWifiDisplayStatusProxy) Proxy.newProxyInstance(MiracastSettings.class.getClassLoader(),
                        faces, ih);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
