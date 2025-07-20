package info.ejava.examples.svc.authz.authorities.security;

import java.util.function.UnaryOperator;

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
import org.springframework.security.authorization.AuthorityAuthorizationManager;
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
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity(
    prePostEnabled = true, // @PreAuthorize("hasAuthority('ROLE_ADMIN')"), @PreAuthorize("hasRole('ADMIN')")
    jsr250Enabled = true, // @RolesAllowed({"MANAGER"})
    securedEnabled = true // @Secured({"ROLE_MEMBER"})

)
@RequiredArgsConstructor
public class ComponentBasedSecurityConfigurationFix {

    /**
     * https://github.com/jzheaux/cve-2023-34035-mitigations
     * An explicit MvcRequestMatcher.Builder is necessary when mixing SpringMvc with
     * non-SpringMvc Servlets. Enabling the H2 console puts us in that position.
     * Dissabling (spring.h2.console.enabled=false) or being explicit as to which URI
     * apply to SpringMvc avoids the problem.
     * @param introspector
     * @return
     */

    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector){
       return new MvcRequestMatcher.Builder(introspector);
    }

    @Bean
    public WebSecurityCustomizer authzStaticResources(MvcRequestMatcher.Builder mvc) {

        return web -> web.ignoring().requestMatchers(mvc.pattern("/content/**"));
    }    

    @Bean
    @Order(0)
    public SecurityFilterChain authzSecurityFilterChain(HttpSecurity http, 
                                    MvcRequestMatcher.Builder mvc,RoleHierarchy roleHierarchy)  throws Exception {
                                        
        http.securityMatchers(cfg->cfg.requestMatchers(mvc.pattern("/api/**")));

        // builder for with custom access example
        UnaryOperator<AuthorityAuthorizationManager> withRoleHierarchy = autho -> {
            autho.setRoleHierarchy(roleHierarchy);
            return autho;
        };

        http.authorizeHttpRequests(cfg -> cfg.requestMatchers(mvc.pattern("/api/whoAmI"),
                                        mvc.pattern("/api/authorities/paths/anonymous/**"))
                                        .permitAll());
        
        http.authorizeHttpRequests(cfg -> cfg.requestMatchers(mvc.pattern("/api/authorities/paths/admin/**"))
                                        .hasRole("ADMIN"));
        
        http.authorizeHttpRequests(cfg -> cfg.requestMatchers(mvc.pattern("/api/authorities/paths/clerk/**"))
                                            .hasAnyRole("ADMIN","CLERK"));
        
        http.authorizeHttpRequests(cfg -> cfg.requestMatchers(mvc.pattern("/api/authorities/paths/customer/**"))
                                        //.access(AuthorityAuthorizationManager.hasAnyRole("CUSTOMER"))
                                        //.access(withRoleHierarchy.apply(AuthorityAuthorizationManager.hasAnyAuthority("ROLE_CUSTOMER"))) // + RoleHierArchy
                                        .access(withRoleHierarchy.apply(AuthorityAuthorizationManager.hasAnyRole("CUSTOMER"))) // + RoleHierArchy
                                        //.hasAnyRole("CUSTOMER")
                                        );
        http.authorizeHttpRequests(cfg-> cfg.requestMatchers(mvc.pattern(HttpMethod.GET, "/api/authorities/paths/price"))
                                // .access(AuthorizationManagers.anyOf( //not using RoleHierachy
                                //         AuthorityAuthorizationManager.hasAuthority("PRICE_CHECK"),
                                //         AuthorityAuthorizationManager.hasRole("ADMIN"),
                                //         AuthorityAuthorizationManager.hasRole("CLERK")
                                //  ))
                                    
                                .hasAnyAuthority("ROLE_ADMIN","ROLE_CLERK","PRICE_CHECK"));
        http.authorizeHttpRequests(cfg->cfg.requestMatchers(mvc.pattern("/api/authorities/paths/nobody/**"))
                                                            .denyAll());
        http.authorizeHttpRequests(cfg->cfg.requestMatchers(mvc.pattern("/api/authorities/paths/authn/**"))
                                    //                .access(new AuthenticatedAuthorizationManager<>()) //using ctor
                                    //                .access(AuthenticatedAuthorizationManager.authenticated()) //thru builder
                                    .authenticated()); //thru customizer.builder
        // These are requests are handled by class / method annotations
        http.authorizeHttpRequests(cfg->cfg.requestMatchers(mvc.pattern("/api/authorities/secured/**"),
                                    mvc.pattern("/api/authorities/jsr250/**"),
                                    mvc.pattern("/api/authorities/expressions/**"))
                                    .permitAll());
        
        http.httpBasic(cfg->cfg.realmName("AuthzExample"));
        http.formLogin(cfg->cfg.disable());
         http.headers(cfg->cfg.disable()); //disabling all security headers
//        http.headers(cfg->{ //disabling individually
//            cfg.frameOptions(fo->fo.disable());
//            cfg.xssProtection(xss->xss.disable());
//            cfg.cacheControl(cache-> cache.disable());
//            cfg.contentTypeOptions(content->content.disable());
//        });
        http.csrf(cfg->cfg.disable());
        http.cors(cfg-> cfg.configurationSource(req -> new CorsConfiguration().applyPermitDefaultValues()));
        http.sessionManagement(cfg->cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

     @Bean
    @Order(500)
    public SecurityFilterChain h2SecurityFilters(HttpSecurity http,
             MvcRequestMatcher.Builder mvc, AuthenticationManager authenticationManager) throws Exception {
        http.securityMatchers(cfg->cfg
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher("/h2-console*"),
                    AntPathRequestMatcher.antMatcher("/h2-console/**"))
                .requestMatchers(
                    mvc.pattern("/login"),
                    mvc.pattern("/logout"),
                    mvc.pattern("/error"))
        );
        http.authorizeHttpRequests(cfg->cfg
                 //error page must be explicitly permitted for favicon.ico 404 errors
                .requestMatchers(mvc.pattern("/error")).permitAll()
                .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET,".+(.css|.jsp|.gif)$")).permitAll()
                .anyRequest().authenticated()
        );
        http.formLogin(cfg->cfg
                .permitAll() //enables access to login and logout
                .successForwardUrl("/h2-console")
        );
        http.csrf(cfg->cfg.ignoringRequestMatchers(
                AntPathRequestMatcher.antMatcher("/h2-console/**")
        ));
        http.headers(cfg->{
            cfg.frameOptions(fo-> fo.sameOrigin());
        });

        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.parentAuthenticationManager(authenticationManager);
        return http.build();
    }

     @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http, UserDetailsService jdbcUserDetailsService) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(jdbcUserDetailsService);
        return builder.build();
    }
     protected static class AuthzCorsConfigurationSource implements CorsConfigurationSource {
        @Override
        public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
            return new CorsConfiguration().applyPermitDefaultValues();
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService jdbcUserDetailsService(DataSource userDataSource) {
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
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy, ApplicationContext context) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        expressionHandler.setApplicationContext(context);
        return expressionHandler;
    }



}
