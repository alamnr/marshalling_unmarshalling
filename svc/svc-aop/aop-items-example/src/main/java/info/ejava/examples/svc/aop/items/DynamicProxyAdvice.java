package info.ejava.examples.svc.aop.items;

import java.lang.reflect.Proxy;

import org.apache.commons.lang3.ClassUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import info.ejava.examples.svc.aop.items.dto.GrillDTO;
import info.ejava.examples.svc.aop.items.dynamicproxies.MyInvocationHandler;
import info.ejava.examples.svc.aop.items.services.GrillsServiceImpl;
import info.ejava.examples.svc.aop.items.services.ItemsService;
import lombok.extern.slf4j.Slf4j;

/**
 * This example creates a Dynamic Proxy using an interface and an
 * optional target object using java.lang.reflect mechanisms.
 */

@Component
@Slf4j
public class DynamicProxyAdvice implements CommandLineRunner {
    
    
    @Override
    public void run(String... args) throws Exception {


        ItemsService<GrillDTO> grillService = new GrillsServiceImpl();

        //the following convenience method call wraps this:
        //        ItemsService<GrillDTO> grillServiceProxy = (ItemsService<GrillDTO>)
        //            Proxy.newProxyInstance(grillService.getClass().getClassLoader(),
        //                    new Class[]{ItemsService.class},
        //                    new MyDynamicProxy(grillService));

        ItemsService<GrillDTO> grillsServiceProxy = (ItemsService<GrillDTO>)
                MyInvocationHandler.newInstance(grillService);

        log.info("**************************created proxy: {}", grillsServiceProxy.getClass());
        log.info("handler: {}", Proxy.getInvocationHandler(grillsServiceProxy).getClass());
        log.info("proxy implements interfaces: {}",
                ClassUtils.getAllInterfaces(grillsServiceProxy.getClass()));

        GrillDTO createdGrill = grillsServiceProxy.createItem(
                GrillDTO.grillBuilder().name("Broil King").build());
        log.info("created grill: {}", createdGrill);


    }
    
}
