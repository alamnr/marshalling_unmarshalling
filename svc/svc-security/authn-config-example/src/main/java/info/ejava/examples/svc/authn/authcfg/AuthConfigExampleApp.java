package info.ejava.examples.svc.authn.authcfg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import info.ejava.examples.common.web.WebLoggingFilter;
import jakarta.servlet.Filter;

@SpringBootApplication
public class AuthConfigExampleApp {
    
    public static void main(String... args){
        SpringApplication.run(AuthConfigExampleApp.class,args);
    }

    @Bean
    public Filter logFilter(){
        return WebLoggingFilter.logFilter();
    }
    
}
