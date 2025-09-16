package info.ejava.examples.svc.aop.items;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import info.ejava.examples.svc.aop.items.dto.MowerDTO;
import info.ejava.examples.svc.aop.items.proxyfactory.SampleAdvice1;
import info.ejava.examples.svc.aop.items.services.ItemsService;
import info.ejava.examples.svc.aop.items.services.MowersServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * This example creates a proxy using a Spring ProxyFactory. This consists of the
 * target object, advice implementing one or more callback interfaces, with an
 * optional advisor wrapper to make matching decisions.
 */
@Component
@Slf4j
public class ProgrammaticAOPAdvice implements CommandLineRunner {@Override
    public void run(String... args) throws Exception {
        
        MowerDTO mower1 = MowerDTO.mowerBuilder().name("John Deer").build();
        MowerDTO mower2 = MowerDTO.mowerBuilder().name("Husqvarna").build();

        ItemsService<MowerDTO> mowerService = new MowersServiceImpl();

        mowerService.createItem(mower1);
        log.info("************************* without aop - {}", mowerService.getItem(1));

        SampleAdvice1 advice1 = new SampleAdvice1();
        ProxyFactory proxyFactory = new ProxyFactory(mowerService);

        proxyFactory.addAdvice(advice1);

        ItemsService<MowerDTO> proxiedMowerService = (ItemsService<MowerDTO>)proxyFactory.getProxy();
        proxiedMowerService.createItem(mower2);

        log.info("******************** aop proxy class = {}", proxiedMowerService.getClass());
    }
    
}
