package objenome.util.bean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public   enum BeanProxy {
    ;

    public static boolean isProxyClass(Object o) {
        return Proxy.isProxyClass(o.getClass()) && getInvocationHandler(o) != null;
    }

    private static ProxyInvocationHandler getInvocationHandler(Object o) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(o);
        return invocationHandler instanceof ProxyInvocationHandler ? (ProxyInvocationHandler) invocationHandler
                : null;
    }

    public static Class<?> getProxiedClass(Object o) {
        ProxyInvocationHandler invocationHandler = getInvocationHandler(o);
        return invocationHandler == null ? null : invocationHandler.getProxiedIface();
    }

}
