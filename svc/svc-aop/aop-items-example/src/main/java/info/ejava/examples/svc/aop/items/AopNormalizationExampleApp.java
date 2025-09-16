package info.ejava.examples.svc.aop.items;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class AopNormalizationExampleApp {
    
    public static void main(String[] args){
        SpringApplication.run(AopNormalizationExampleApp.class, args);
    }
}
