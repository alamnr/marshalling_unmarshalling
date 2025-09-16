package info.ejava.examples.svc.aop.items.proxyfactory;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * This class provides programmatic advice definitions that can be assigned using
 * a ProxyFactory
 * <pre>
 *     ProxyFactory proxyFactory = new ProxyFactory(mowerService);
 *     proxyFactory.addAdvice(new SampleAdvice1());
 *     ItemsService<MowerDTO> proxiedMowerService = (ItemsService<MowerDTO>) proxyFactory.getProxy();
 *     proxiedMowerService.createItem(mower2);
 * </pre>
 */
@Slf4j
public class SampleAdvice1 implements MethodBeforeAdvice , AfterReturningAdvice {
    
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        log.info("before: {}.{}({})", target, method, args);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
         log.info("after: {}.{}({}) = {}", target, method, args, returnValue);
    }
    
}
