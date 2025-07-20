package info.ejava.examples.svc.authz.authorities.security;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.NullRoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorityAuthorizationDecision;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

// @Configuration(proxyBeanMethods=false)
// @EnableMethodSecurity(
//     prePostEnabled = true,   // @PreAuthorize("hasAuthority('ROLE_ADMIN')"), @PreAuthorize("hasRole('ADMIN')")
//     jsr250Enabled = true,   // @RolesAllowed({"MANAGER"}) - spring security - 6 @RolesAllowed({"ROLE_MANAGER"}) - Spring Security-5
//     securedEnabled = true  // @Secured("ROLE_ADMIN") @SeCured({"ROLE_ADMIN", "ROLE_MANAGER"})
// )
// @RequiredArgsConstructor
public class ComponentBasedSecurityConfiguration {

    @Bean
    public WebSecurityCustomizer authzStaticResources()  {
        return web -> web.ignoring().requestMatchers("/content/**");
    }

    @Bean
    @Order(0)
    //public SecurityFilterChain authzSecurityFilterChain(HttpSecurity http) throws Exception {
    public SecurityFilterChain authzSecurityFilterChain(HttpSecurity http, RoleHierarchy roleHierarchy) throws Exception {
        http.securityMatchers(cfg-> cfg.requestMatchers("/api/**"));
        http.authorizeHttpRequests(cfg-> cfg.requestMatchers("/api/whoAmI","/api/authorities/paths/anonymous/**").permitAll());
        http.authorizeHttpRequests(cfg->cfg.requestMatchers("/api/authorities/paths/admin/**").hasRole("ADMIN"));
        http.authorizeHttpRequests(cfg->cfg.requestMatchers("/api/authorities/paths/clerk/**").hasAnyRole("ADMIN","CLERK")); // explicit ADMIN not needed with inheritance
        http.authorizeHttpRequests(cfg->cfg.requestMatchers("/api/authorities/paths/customer/**").hasAnyRole("CUSTOMER")) ;
        http.authorizeHttpRequests(cfg->cfg.requestMatchers(HttpMethod.GET,"/api/authorities/paths/price")
                        // .access(AuthorizationManagers.anyOf(
                        //     AuthorityAuthorizationManager.hasAuthority("PRICE_CHECK"),
                        //     AuthorityAuthorizationManager.hasRole("ADMIN"),
                        //     AuthorityAuthorizationManager.hasRole("CLERK")
                        // )));
                    .hasAnyAuthority("PRICE_CHECK","ROLE_ADMIN","ROLE_CLERK"));
        
        http.authorizeHttpRequests(cfg->cfg.requestMatchers("/api/authorities/paths/nobody/**").denyAll());
        http.authorizeHttpRequests(cfg->cfg.requestMatchers("/api/authorities/paths/authn/**")
        
                                    //.access(new AuthenticatedAuthorizationManager<>()) // using ctor (constructor)
                                    //.access(AuthenticatedAuthorizationManager.authenticated()) // thru builder
                                    .authenticated()  // thru customizer.builder
                                ); 

        // these requests are handaled by class/method annotations
        http.authorizeHttpRequests(cfg->cfg.requestMatchers("/api/authorities/secured/**",
                                                            "/api/authorities/jsr250/**",
                                                            "/api/authorities/expressions/**").permitAll());
        
        http.httpBasic(cfg->cfg.realmName("AuthzExample"));
        http.formLogin(cfg->cfg.disable());
        http.headers(cfg->cfg.disable()); // disable all security headers
        // disabling individually
        // http.headers(cfg-> {
        //     cfg.frameOptions(fo->fo.disable());
        //     cfg.xssProtection(xss->xss.disable());
        //     cfg.cacheControl(cache->cache.disable());
        //     cfg.contentTypeOptions(content->content.disable())
        // });

        http.csrf(csrf->csrf.disable());
        http.cors(cfg->cfg.configurationSource(req-> new CorsConfiguration().applyPermitDefaultValues()));
        http.sessionManagement(cfg->cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();

    }
    
    @Bean
    @Order(500)
    public SecurityFilterChain h2SecurityFilterChain(HttpSecurity http, AuthenticationManager  authenticationManager) throws Exception {
        
        http.securityMatchers(cfg->cfg.requestMatchers("/h2-console*","/h2-console/**")
        .requestMatchers("/login","/logout","/error"));
        
        http.authorizeHttpRequests(cfg->cfg
        // error page must be explicitly permitted for favicon.ico 404 errors)           
        .requestMatchers("/error").permitAll()
        .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET,".+(.css|.jsp|.gif)$")).permitAll()
        .anyRequest().authenticated());
        
        http.formLogin(cfg->cfg
        .permitAll() // enables access to login and logout
        .successForwardUrl("/h2-console"));
        
        http.csrf(cfg-> cfg.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")));
        
        http.headers(cfg -> {
            cfg.frameOptions(fo->fo.sameOrigin());
        });
        
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.parentAuthenticationManager(authenticationManager);
        return http.build();
        
    }
    
    protected static class AuthzCorsConfigurationSource implements CorsConfigurationSource {
        
        @Override
        public CorsConfiguration getCorsConfiguration(HttpServletRequest arg0) {
            return new CorsConfiguration().applyPermitDefaultValues();
        }
        
    }
    
    
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UserDetailsService jdbUserDetailsService) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(jdbUserDetailsService);
        return builder.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService jdbcUserDetailsService(DataSource userDataSource){
        JdbcDaoImpl jdbcUds = new JdbcDaoImpl();
        jdbcUds.setDataSource(userDataSource);
        return jdbcUds;
    }

    //needed mid-way thru lecture
    @Bean
    @Profile("roleInheritance")
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("CLERK")
                .role("CLERK").implies("CUSTOMER")
                .build();
//legacy
//        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
//        roleHierarchy.setHierarchy(StringUtils.join(List.of(
//                "ROLE_ADMIN > ROLE_CLERK",
//                "ROLE_CLERK > ROLE_CUSTOMER"
//        ),System.lineSeparator()));
//        return roleHierarchy;
    }

    /**
     * Creates a default RoleHierachy when the examples want straight roles.
     */
    @Bean
    @Profile("!roleInheritance")
    static RoleHierarchy nullHierarchy() {
        return new NullRoleHierarchy();
    }

    /**
     * Creates a custom MethodExpressionHandler that will be picked up by
     * Expression-based security to support RoleInheritance.
     * This is required until the
     * <a href="github.com/spring-projects/spring-security/issues/12783">the following</a>
     * is resolved.
     */
    //we are using Spring 6.1.11 with ***Spring Security 6.3.1***
    //@Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy, ApplicationContext context) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        expressionHandler.setApplicationContext(context);
        return expressionHandler;
    }    
}
