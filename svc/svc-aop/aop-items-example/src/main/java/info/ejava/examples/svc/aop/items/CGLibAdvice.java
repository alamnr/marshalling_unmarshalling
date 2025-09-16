package info.ejava.examples.svc.aop.items;

import info.ejava.examples.svc.aop.items.aspects.MyMethodInterceptor;
import info.ejava.examples.svc.aop.items.dto.ChairDTO;
import info.ejava.examples.svc.aop.items.services.ChairsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.stereotype.Component;

/**
 * This example creates a CGLIB proxy for a class and an optional
 * target object. CGLIB can be used in cases when there is no interface
 * and the proxied object can either be the base class or a delegate.
 */
@Component
@Slf4j
public class CGLibAdvice implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ChairsServiceImpl.class);
        enhancer.setCallback(new MyMethodInterceptor());
        ChairsServiceImpl chairsServiceProxy = (ChairsServiceImpl)enhancer.create();

        log.info("********************************* cglib created proxy: {}", chairsServiceProxy.getClass());
        log.info("proxy implements interfaces: {}",
                ClassUtils.getAllInterfaces(chairsServiceProxy.getClass()));
        log.info("proxy superclasses: {}",
                ClassUtils.getAllSuperclasses(chairsServiceProxy.getClass()));

        ChairDTO createdChair = chairsServiceProxy.createItem(
                ChairDTO.chairBuilder().name("Recliner").build());
        log.info("created chair: {}", createdChair);
    }
}
