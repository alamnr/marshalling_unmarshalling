package info.ejava.examples.svc.authn.authcfg.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
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
    //     http.authorizeHttpRequests(cfg -> cfg.requestMatchers("api/anonymous/**").permitAll());
    //     http.authorizeHttpRequests(cfg -> cfg.anyRequest().authenticated());
    //     http.httpBasic(cfg -> cfg.realmName("AuthConfigExample"));
    //     http.formLogin(cfg -> cfg.disable()); 
    //     http.headers(cfg -> {
    //        cfg.xssProtection(xss-> xss.disable());
    //        cfg.frameOptions(fo-> fo.disable());
    //     });
    //     http.csrf(cfg -> cfg.disable());

    //     http.sessionManagement(cfg -> cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    //     return http.build();
    // }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain apiSecurityFilter(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {

        http.securityMatchers(m -> m.requestMatchers(mvc.pattern("/api/anonymous/**"),mvc.pattern("/api/authn/**")));
        
        //if specific to HttpMethod
        //http.securityMatchers(m->m.requestMatchers(HttpMethod.GET, "/api/anonymous/**","/api/authn/**"));

        //if needed RegExp support
        //        http.securityMatchers(m->m.requestMatchers(
        //                RegexRequestMatcher.regexMatcher("^/api/anonymous/{0,1}.*$"),
        //                RegexRequestMatcher.regexMatcher("^/api/authn/{0,1}.*$")
        //        ));


        http.authorizeHttpRequests(cfg -> cfg.requestMatchers("api/anonymous/**").permitAll());
        http.authorizeHttpRequests(cfg -> cfg.anyRequest().authenticated());
        
        http.httpBasic(cfg -> cfg.realmName("AuthConfigExample"));
        http.formLogin(cfg -> cfg.disable());
        http.headers(cfg -> {
            cfg.xssProtection(xss-> xss.disable());
            cfg.frameOptions(fo-> fo.disable());
        });
        http.csrf(cfg -> cfg.disable());
        // demo different CORS setting
        //  if (false) {
        //     http.cors(cfg->cfg.configurationSource(corsPermitAllConfigurationSource()));
        // } else if (false) {
        //     //cfg -- restrict calls
        //     http.cors(cfg->cfg.configurationSource(corsLimitedConfigurationSource()));
        // } else {
        //     http.cors(cfg->cfg.disable());
        // }
        //http.cors(cfg->cfg.configurationSource(corsPermitAllConfigurationSource()));
        http.cors(cfg->cfg.configurationSource(corsLimitedConfigurationSource()));

        http.sessionManagement(cfg -> cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector){
        return new MvcRequestMatcher.Builder(introspector);
    }

    

     @Bean
    @Order(1000)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        //uncomment if you want to activate BASIC Auth to exercise identity in the controller
        //        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }


    private CorsConfigurationSource corsPermitAllConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.applyPermitDefaultValues();
            return config;
        };
    }

    private CorsConfigurationSource corsLimitedConfigurationSource() {
        return (request) -> {
                CorsConfiguration config = new CorsConfiguration();
                config.addAllowedOrigin("http://acme.com");
                config.addAllowedOrigin("http://localhost:8080");
                //config.addAllowedOrigin("http://127.0.0.1:8080");
                config.setAllowedMethods(List.of("GET","POST"));
                return config;
        };
    }


}
