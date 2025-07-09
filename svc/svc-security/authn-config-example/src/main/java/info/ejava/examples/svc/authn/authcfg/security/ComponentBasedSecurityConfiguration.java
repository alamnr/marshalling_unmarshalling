package info.ejava.examples.svc.authn.authcfg.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration(proxyBeanMethods = false)
public class ComponentBasedSecurityConfiguration {

    @Bean
    public WebSecurityCustomizer apiStaticResources(){
        return web -> web.ignoring().requestMatchers("/content/**");
    }

    // @Bean
    // @Order(0)
    // public SecurityFilterChain apiSecurityFilter(HttpSecurity http) throws Exception {
    //     http.securityMatchers(m->m.requestMatchers("/api/anonymous/**","/api/authn/**"));
    //     return http.build();
    // }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain apiSecurityFilter(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {

        http.securityMatchers(m -> m.requestMatchers(mvc.pattern("/api/anonymous/**"),mvc.pattern("/api/authn/**")));
        return http.build();
    }

    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector){
        return new MvcRequestMatcher.Builder(introspector);
    }
}
