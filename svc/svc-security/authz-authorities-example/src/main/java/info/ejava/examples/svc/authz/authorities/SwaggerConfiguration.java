package info.ejava.examples.svc.authz.authorities;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "test", havingValue = "false" , matchIfMissing = true)
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenApi(){
        return new OpenAPI().components(new Components()
                            .addSecuritySchemes("basicAuth", 
                                new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")));
    }

    @Bean
    public SecurityFilterChain swaggSecurityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {

        MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);
        http.securityMatchers(cfg-> cfg.requestMatchers(
                                mvc.pattern("/swagger-ui*"),
                                mvc.pattern("/swagger-ui/**"),
                                mvc.pattern("/v3/api-docs/**")));
        
        http.authorizeHttpRequests(cfg -> cfg.anyRequest().permitAll());
        http.csrf(cfg->cfg.disable());
        return http.build();
    }
    
}
