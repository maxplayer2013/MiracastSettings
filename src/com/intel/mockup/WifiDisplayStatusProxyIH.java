package com.intel.mockup;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 
 * @author Jinmiao
 * @version 0.5
 * 
 */
public class WifiDisplayStatusProxyIH extends ProxyIH {

    public WifiDisplayStatusProxyIH(Object target, String className) {
        super(target, className);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = new Integer(0);
        try {
            Method m = getMethod(method, args);
            m.setAccessible(true);
            ret = m.invoke(mTargetObj, args);

            if (method.getName().equals("getActiveDisplay")) {
                WifiDisplayProxyIH ih = new WifiDisplayProxyIH(ret, IWifiDisplayProxy.PACKAGE);
                @SuppressWarnings("rawtypes")
                Class[] faces = new Class[] { IWifiDisplayProxy.class };

                IWifiDisplayProxy mywd = (IWifiDisplayProxy) Proxy.newProxyInstance(
                        IWifiDisplayProxy.class.getClassLoader(), faces, ih);
                ret = mywd;
            } else if (method.getName().equals("getRememberedDisplays")
                    || method.getName().equals("getAvailableDisplays")) {
                Object[] obj = (Object[]) ret;
                IWifiDisplayProxy[] mywd = new IWifiDisplayProxy[obj.length];

                int count = mywd.length;

                Class[] faces = new Class[] { IWifiDisplayProxy.class };
                for (int i = 0; i < count; i++) {
                    WifiDisplayProxyIH ih = new WifiDisplayProxyIH(obj[i], IWifiDisplayProxy.PACKAGE);
                    mywd[i] = (IWifiDisplayProxy) Proxy.newProxyInstance(IWifiDisplayProxy.class.getClassLoader(),
                            faces, ih);
                }
                ret = mywd;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

}
