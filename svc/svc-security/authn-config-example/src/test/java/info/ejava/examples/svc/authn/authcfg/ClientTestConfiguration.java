package info.ejava.examples.svc.authn.authcfg;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import info.ejava.examples.common.web.RestTemplateLoggingFilter;
import info.ejava.examples.common.web.ServerConfig;

/*
 * A test configuration used by remote IT test clients
 */
@TestConfiguration
public class ClientTestConfiguration {
    
    @Bean
    @ConfigurationProperties("it.server")
    public ServerConfig itServerConfig(){
        return new ServerConfig();
    }

    @Bean
    public ClientHttpRequestFactory requestFactory(){
        return new SimpleClientHttpRequestFactory();
    }


    @Bean
    public RestTemplate anonymousUser(RestTemplateBuilder builder, ClientHttpRequestFactory requestFactory){
        RestTemplate restTemplate = builder.requestFactory(
            // used to read the streams twice -- so we can use the logging filter below
            () -> new BufferingClientHttpRequestFactory(requestFactory))
        .interceptors(new RestTemplateLoggingFilter())
        .build();
        return restTemplate;
    }

    @Bean
    public RestTemplate authnUser(RestTemplateBuilder builder, ClientHttpRequestFactory requestFactory){
        RestTemplate restTemplate = builder.requestFactory(
         // used to read stream twice -- so we can use the logging filter below
         () -> new BufferingClientHttpRequestFactory(requestFactory))
            .interceptors(new BasicAuthenticationInterceptor("user", "password"), new RestTemplateLoggingFilter())
            .build();
        return restTemplate;
    }
}
