package info.ejava.examples.svc.aop.items.proxyfactory;

import info.ejava.examples.svc.aop.items.dto.ItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This programmatic implementation of a Introduction artificially breaks the solution into
 * two separate advice: 1) decorate the POJO with extra access field(s) and 2) determine the
 * caller accesses and assigning markings to the POJO. The advice had to be separated into
 * two separate Aspects in order to define a deterministic order.
 */
@Slf4j
public class MyAccessAspects {
    /**
     * Match all getItem methods of the targeted package that return any type of object.
     */
    @Pointcut("execution(* info.ejava.examples.svc.aop.items.services.*.getItem(..))") //expression
    public void getter() {} //signature

    /**
     * This advice runs first to invoke the business method and then augment the response object with
     * an implementation of the MyAccessIntroduction interface. No access info is assigned, just the extra type
     * augmentation is performed here. This is our choice for demonstration only.
     */
    @Component
    @Aspect
    @Order(1) //run this before value assignment aspect
    static class MyAccessDecorationAspect {
        @Around(value = "getter()")
        public Object decorateWithAccesses(ProceedingJoinPoint pjp) throws Throwable {
            ItemDTO advisedObject = (ItemDTO) pjp.proceed(pjp.getArgs());

            //build the proxy with the target data
            ProxyFactory proxyFactory = new ProxyFactory(advisedObject);
            //directly support an interface for the target object
            proxyFactory.setProxyTargetClass(true);

            //assign the mixin
            proxyFactory.addInterface(MyAccessIntroduction.class);
            DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(new MyAccessIntroductionImpl<>(advisedObject));
            proxyFactory.addAdvice(dii);

            //return the advised object
            ItemDTO proxyObject = (ItemDTO) proxyFactory.getProxy();
            return proxyObject;
        }
    }

    /**
     * This advice runs second/last to identify the caller accesses and annotate the Introduction with
     * those values. The advice can run as an @AfterReturning since it only inspects and modifies the
     * existing returned POJO versus replacing it.
     */
    @Component
    @Aspect
    @Order(0) //run this after decoration aspect
    static class MyAccessAssignmentAspect {
        @AfterReturning(value = "getter()", returning = "protectedObject")
        public void assignAccess(ItemDTO protectedObject) throws Throwable {
            log.info("determining access for {}", protectedObject);
            MyAccessIntroduction.Access access = deriveAccess(protectedObject.getId());

            //assigning roles
            ((MyAccessIntroduction<?>) protectedObject).setUserRoles(List.of(access));

            log.info("augmented item {} with accesses {}", protectedObject, ((MyAccessIntroduction<?>) protectedObject).getUserRoles());
        }

        //simply make up one of the available accesses for each call based on value of id
        private MyAccessIntroduction.Access deriveAccess(int id) {
            int index = id % MyAccessIntroduction.Access.values().length;
            return MyAccessIntroduction.Access.values()[index];
        }
    }
}
