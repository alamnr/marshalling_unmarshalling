package info.ejava.examples.svc.authn.users.security;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import ch.qos.logback.core.joran.spi.HttpUtil.RequestMethod;

@Configuration(proxyBeanMethods = false)
public class ComponentBasedSecurityConfiguration {

    @Bean
    @Order(10000)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception{
        //http.authorizeHttpRequests(req-> req.anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer apiStaticResources() throws Exception {
        return web -> web.ignoring().requestMatchers("/content/**");
    }

    @Bean
    @Order(0)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatchers(req->req.requestMatchers("/api/anonymous/**", "/api/authn/**"));
        
        http.authorizeHttpRequests(req->req.requestMatchers("/api/anonymous/**").permitAll());
        http.authorizeHttpRequests(req->req.anyRequest().authenticated());

        http.httpBasic(cfg->cfg.realmName("AuthConfigExample"));
        http.formLogin(cfg->cfg.disable());

        http.headers(cfg -> {
            cfg.xssProtection(xss-> xss.disable());
            cfg.frameOptions(fo-> fo.disable());
        });

        http.csrf(cfg-> cfg.disable());

        http.cors(cfg -> cfg.configurationSource(corsPermitAllConfigurationSource()));
        //http.cors(cfg -> cfg.configurationSource(corsLimitedConfigurationSource()));

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
        return request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.addAllowedOrigin("http://acme.com");
                config.addAllowedOrigin("http://localhost:8080");
                config.addAllowedOrigin("http://127.0.0.1:8080");
                config.setAllowedMethods(List.of("GET","POST"));
                return config;
        };
    }

    // @Bean
    // public AuthenticationManager authnManager(HttpSecurity http) throws Exception {
    //     AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);

    //     PasswordEncoder encoder = NoOpPasswordEncoder.getInstance();
    //     builder.inMemoryAuthentication()   // added InMemory UserDetails Service
    //             .passwordEncoder(encoder)  // added password encoder to the AuthenticationProvider
    //             .withUser("user1").password(encoder.encode("password1")).roles() // while storing user into storage it requires encoding
    //             .and()
    //             .withUser("user2").password(encoder.encode("password1")).roles();

    //     builder.parentAuthenticationManager(null); // prevent from being recursive
    //     return builder.build();
    // }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                List<UserDetailsService> userDetailsServices) throws Exception {
        
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
   
        PasswordEncoder encoder = NoOpPasswordEncoder.getInstance();
        builder.inMemoryAuthentication()   // added InMemory UserDetails Service
                .passwordEncoder(encoder)  // added password encoder to the AuthenticationProvider
                .withUser("user1").password(encoder.encode("password1")).roles() // while storing user into storage it requires encoding
                .and()
                .withUser("user2").password(encoder.encode("password1")).roles();

        for (UserDetailsService userDetailsService : userDetailsServices) {
            builder.userDetailsService(userDetailsService);
        }
        builder.parentAuthenticationManager(null); // prevent from being recursive
        return builder.build();
    }

    @Bean 
    public PasswordEncoder passwordEncoder() {
        //return NoOpPasswordEncoder.getInstance();
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService sharedUserDetailsService(PasswordEncoder encoder) {
        List<UserDetails> users = List.of(
            User.withUsername("user1").passwordEncoder(encoder::encode).password("password2").roles().build(),
            //User.withUsername("user1").passwordEncoder(encoder::encode).password("password1").roles().build(), // same user twice throws exception
            
            User.withUsername("user3").passwordEncoder(encoder::encode).password("password2").roles().build()
        );
        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    public UserDetailsService jdbcUserDetailsService(DataSource dataSource) {
        JdbcDaoImpl jdbcUds = new JdbcDaoImpl();
        jdbcUds.setDataSource(dataSource);
        return jdbcUds;
    }


    /**
     * Adding h2-console to application and protecting behind a FORM login fed off the
     * application's authentication manager.
     * @param http
     * @param authMgr
     */
    // @Order(500)
    // @Bean
    // public SecurityFilterChain h2SecurityFilters(HttpSecurity http,
    //                  MvcRequestMatcher.Builder mvc, AuthenticationManager authMgr) throws Exception {
    //     http.securityMatchers(cfg->{cfg
    //             .requestMatchers( //h2-console not local SpringMVC application, must match URI
    //                 AntPathRequestMatcher.antMatcher("/h2-console*"),
    //                 AntPathRequestMatcher.antMatcher("/h2-console/**"))
    //             //can use MvcMatcher for form login pages
    //             .requestMatchers(
    //                 mvc.pattern("/login"),
    //                 mvc.pattern("/logout"),
    //                 mvc.pattern("/error"));
    //     });
    //     http.authorizeHttpRequests(cfg->cfg
    //             .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET,".+(.css|.jsp|.gif)$")).permitAll()
    //             .anyRequest().authenticated()
    //     );
    //     http.formLogin(cfg->cfg
    //         .permitAll() //applies permitAll to standard login URIs
    //         .successForwardUrl("/h2-console")
    //     );
    //     http.csrf(cfg->cfg.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")));
    //     http.headers(cfg-> cfg.frameOptions(fo->fo.disable()));

    //     http.authenticationManager(authMgr);//reuse applications authz users
    //     return http.build();
    // }

    /**
     * Adding h2-console to application and protecting behind a FORM login fed off the
     * application's authentication manager.
     * @param http
     * @param authMgr
     */
    @Order(500)
    @Bean
    public SecurityFilterChain h2SecurityFilters(HttpSecurity http, AuthenticationManager authMgr) throws Exception {
        MediaTypeRequestMatcher htmlRequestMatcher = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
        htmlRequestMatcher.setUseEquals(true);

        http.securityMatchers(cfg->cfg
                .requestMatchers("/h2-console*","/h2-console/**")
                .requestMatchers("/login", "/logout")
                .requestMatchers(RequestMatchers.allOf(
                        htmlRequestMatcher, //only want to service HTML error pages
                        AntPathRequestMatcher.antMatcher("/error")
                    ))
        );
        http.authorizeHttpRequests(cfg->cfg
                .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET,".+(.css|.jsp|.gif)$")).permitAll()
                .anyRequest().authenticated()
        );
        http.formLogin(cfg->cfg
            .permitAll() //applies permitAll to standard login URIs
            .successForwardUrl("/h2-console")
        );
        http.csrf(cfg->cfg.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")));
        http.headers(cfg-> cfg.frameOptions(fo->fo.disable()));

        http.authenticationManager(authMgr);//reuse applications authz users
        return http.build();
    }

}
