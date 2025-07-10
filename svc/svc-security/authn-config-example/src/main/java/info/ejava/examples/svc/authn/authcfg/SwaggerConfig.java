package info.ejava.examples.svc.authn.authcfg;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "test", havingValue = "false", matchIfMissing = true)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                    .components(new Components()
                                .addSecuritySchemes("basicAuth", 
                                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")));
    }

    @Bean
    @Order(100)
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatchers(cfg-> cfg.requestMatchers("/swagger-ui*","/swagger-ui/**","/v3/api-docs/**"));
        http.authorizeHttpRequests(cfg->cfg.anyRequest().permitAll());
        return http.build();
    }
    
}
