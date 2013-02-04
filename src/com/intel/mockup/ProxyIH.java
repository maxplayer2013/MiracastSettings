package com.intel.mockup;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author TIAN Yu
 * @version 0.5
 * 
 */
@SuppressWarnings("rawtypes")
public abstract class ProxyIH implements InvocationHandler {

    protected Object mTargetObj;
    protected String mTargetClassName;

    public ProxyIH(Object target, String className) {
        mTargetObj = target;
        mTargetClassName = className;
    }

    protected Method getMethod(Method method, Object[] args) {
        Method ret = null;
        try {
            String methodName = method.getName();
            Class clz = Class.forName(mTargetClassName);
            Method[] declaredMethods = clz.getDeclaredMethods();

            for (Method m : declaredMethods) {
                if (m.getName().equals(methodName)) {
                    if (matchArgs(m.getParameterTypes(), args)) {
                        ret = m;
                        break;
                    }
                }
            }
            
            if(ret == null){
                declaredMethods = Object.class.getDeclaredMethods();
                for (Method m : declaredMethods) {
                    if (m.getName().equals(methodName)) {
                        if (matchArgs(m.getParameterTypes(), args)) {
                            ret = m;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    protected Method getMethod(Method method, Method[] argTypes) {
        //we can do cache in this method
        Method ret = null;
        try {
            String methodName = method.getName();
            Class clz = Class.forName(mTargetClassName);
            Method[] declaredMethods = clz.getMethods();//.getDeclaredMethods();

            for (Method m : declaredMethods) {
                if (m.getName().equals(methodName)) {
                    if (matchArgs(m.getParameterTypes(), argTypes)) {
                        ret = m;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    //not accurate
    protected boolean matchArgs(Class[] types, Object[] args) {
        boolean ret = false;
        int n1 = types != null ? types.length : 0;
        int n2 = args != null ? args.length : 0;
        if (n1 == n2) {
            boolean match = true;
            boolean accurate = true;
            for (int i = 0; i < n1; i++) {
                Class upType = upgradePrimaryType(types[i]);
                if (!(upType).isInstance(args[i])) {
                    match = false;
                    break;
                }else if (accurate && !args[i].getClass().equals(upType)) {
                    accurate = false;
                }
            }
            //not accurate
            ret = match;
        }
        return ret;
    }

    //not accurate
    @SuppressWarnings({ "unchecked"})
    protected boolean matchArgs(Class[] types, Class[] argTypes) {
        boolean ret = false;
        int n1 = types != null ? types.length : 0;
        int n2 = argTypes != null ? argTypes.length : 0;
        if (n1 == n2) {
            boolean match = true;
            boolean accurate = true;
            for (int i = 0; i < n1; i++) {
                Class upType = upgradePrimaryType(types[i]);
                if (!(upType).isAssignableFrom(argTypes[i])) {
                    match = false;
                    break;
                }else if(accurate && !argTypes[i].getClass().equals(upType)){
                    accurate = false;
                }
            }
            //not accurate
            ret = match;
        }
        return ret;
    }

    protected Class[] getTypes(Object[] args) {
        Class[] ret = null;
        int length;

        if (args != null && (length = args.length) > 0) {
            ret = new Class[length];
            for (int i = 0; i < length; i++) {
                ret[i] = args[i].getClass();
            }
        }
        return ret;

    }

    static Map<Class, Class> sPrimitiveUpgradeMatrix = new HashMap<Class, Class>();
    static{
        sPrimitiveUpgradeMatrix.put(byte.class, Byte.class);
        sPrimitiveUpgradeMatrix.put(short.class, Short.class);
        sPrimitiveUpgradeMatrix.put(int.class, Integer.class);
        sPrimitiveUpgradeMatrix.put(long.class, Long.class);
        sPrimitiveUpgradeMatrix.put(float.class, Float.class);
        sPrimitiveUpgradeMatrix.put(double.class, Double.class);
        sPrimitiveUpgradeMatrix.put(char.class, Character.class);
        sPrimitiveUpgradeMatrix.put(boolean.class, Boolean.class);
    }
    protected Class upgradePrimaryType(Class cls) {
        if (!cls.isPrimitive()) {
            return cls;
        }

        Class ret = sPrimitiveUpgradeMatrix.get(cls);
        if(ret != null){
            return ret;
        }        
        
        return cls;
    }

}