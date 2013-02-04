package com.intel.mockup;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 
 * @author TIAN Yu
 * @version 0.5
 */
@SuppressWarnings("rawtypes")
public class ProxyFactory {

    static ClassLoader sClassLoader = ProxyFactory.class.getClassLoader();

    static final String sManagerClassName = IDisplayManagerProxy.PACKAGE;
    static final Class[] sManagerFaces = new Class[] { IDisplayManagerProxy.class };

    static final String sStatusClassName = IWifiDisplayStatusProxy.PACKAGE;
    static final Class[] sStatusFaces = new Class[] { IWifiDisplayStatusProxy.class };

    public static IDisplayManagerProxy newManagerProxyInstance(Object target) {
        InvocationHandler ih = new DisplayManagerProxyIH(target, sManagerClassName);
        IDisplayManagerProxy proxy;

        Object cached = findCachedProxyByTarget(target);
        if (cached != null) {
            proxy = (IDisplayManagerProxy) cached;
        } else {
            proxy = (IDisplayManagerProxy) Proxy.newProxyInstance(sClassLoader, sManagerFaces, ih);
            cacheProxyTargetPair(proxy, target);
        }

        return proxy;
    }

    public static IWifiDisplayStatusProxy newStatusProxyInstance(Object target) {
        InvocationHandler ih = new WifiDisplayStatusProxyIH(target, sStatusClassName);
        IWifiDisplayStatusProxy proxy;

        Object cached = findCachedProxyByTarget(target);
        if (cached != null) {
            proxy = (IWifiDisplayStatusProxy) cached;
        } else {
            proxy = (IWifiDisplayStatusProxy) Proxy.newProxyInstance(sClassLoader, sStatusFaces, ih);
            cacheProxyTargetPair(proxy, target);
        }

        return proxy;
    }

    // advanced features
    static Map<Object, Object> sProxy2TargetMatrix = new WeakHashMap<Object, Object>();

    static Object findCachedProxyByTarget(Object target) {
        Object ret = null;
        if (sProxy2TargetMatrix.containsValue(target)) {
            for (Map.Entry<Object, Object> e : sProxy2TargetMatrix.entrySet()) {
                if (e.getValue() == target) {
                    ret = e.getKey();
                    break;
                }
            }
        }

        return ret;
    }

    static void cacheProxyTargetPair(Object proxy, Object target) {
        System.out.println(String.format("proxy is null? %b, target is null? %b", proxy == null, target == null));
        sProxy2TargetMatrix.put(proxy, target);
    }

}
