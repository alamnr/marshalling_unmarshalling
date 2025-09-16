package info.ejava.examples.svc.aop.items;

import java.lang.reflect.Method;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import info.ejava.examples.svc.aop.items.dto.BedDTO;
import info.ejava.examples.svc.aop.items.dto.ItemDTO;

import info.ejava.examples.svc.aop.items.services.ItemsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This example makes an simple, example method call to a target
 * object using java.lang.reflect.Method.
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class ReflectionAdvice implements CommandLineRunner {

    private final ItemsService<BedDTO> bedsService;

    @Override
    public void run(String... cmdArgs) throws Exception {
        // obtain refernce to method using name and argument types
        Method methdo = ItemsService.class.getMethod("createItem", ItemDTO.class);
        log.info("method - {}", methdo);

        // invoking method using target object and args
        Object[] args = new Object[]{BedDTO.bedBuilder().name("Bunk Bed").build()};
        log.info("invoke calling - {} ({})", methdo.getName(), args);
        Object result  = methdo.invoke(bedsService, args);
        log.info("invoke {} returned -  {}", methdo.getName(),  result);

        // obtained result from invoke() return
        BedDTO createdBed = (BedDTO)result;
        log.info("created bed - {}", createdBed);

    }
    
}
