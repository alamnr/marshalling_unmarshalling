package info.ejava.examples.svc.aop.items.dynamicproxies;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang3.ClassUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is an example Dynamic Proxy. The Proxy class implements the supplied
 * interfaces and delegates calls to the invoke() method within this class.
 */
@Slf4j
@RequiredArgsConstructor
public class MyInvocationHandler implements InvocationHandler {
    private final Object target;

    /**
     * A convenience method to build a Dynamic Proxy for the interfaces of a
     * target object of an unknown type.
     * @param target object to proxy
     * @return the proxy wrapping the target
     */
    public static Object newInstance(Object target) {
        return Proxy.newProxyInstance(target.getClass().getClassLoader(),
                ClassUtils.getAllInterfaces(target.getClass()).toArray(new Class[0]),
                new MyInvocationHandler(target));
    }

     /**
     * This method will get called for each method call to the proxy.
     * The method will simply log when called, invoke the target method, and
     * log when complete. A real handler might do things like check
     * security accesses, begin/terminate a database transaction, etc.
     *
     */

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //do work ...
        log.info("invoke calling: {}({})", method.getName(), args);

        Object result = method.invoke(target, args);

        //do work ...
        log.info("invoke {} returned: {}", method.getName(), result);

        //return result
        return result;

    }
    
}
