package info.ejava.examples.svc.https.hello;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.examples.common.web.RestTemplateLoggingFilter;
import info.ejava.examples.common.web.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.util.Apache5SslUtils;

@TestConfiguration(proxyBeanMethods = false)
@Slf4j
public class ClientTestConfiguration {

    @Value("${spring.security.user.name}")
    private String username;
    @Value("${spring.security.user.password}")
    private String password;

    @Bean @Lazy
    @ConfigurationProperties("it.server")
    public ServerConfig itServerConfig(@LocalServerPort int port){
        return new ServerConfig().withPort(port);
    }

    @Bean @Lazy
    public URI authnUrl(ServerConfig serverConfig){
        URI baseUrl = serverConfig.getBaseUrl();
        log.info("baseUrl = {}", baseUrl);
        return UriComponentsBuilder.fromUri(baseUrl).path("/api/authn/hello").build().toUri();
    }

     /*
    https://sslcontext-kickstart.com/client/apache5.html
    https://github.com/Hakky54/sslcontext-kickstart#apache-5
     */

     @Bean @Lazy
     public ClientHttpRequestFactory httpRequestFactory(@Autowired(required = false) SSLFactory sslFactory){
        PoolingHttpClientConnectionManagerBuilder builder = PoolingHttpClientConnectionManagerBuilder.create();
        PoolingHttpClientConnectionManager connectionManager = Optional.ofNullable(sslFactory)
                                            .map(sf -> builder.setSSLSocketFactory(Apache5SslUtils.toSocketFactory(sf)))
                                            .orElse(builder)
                                            .build();
                                        
        HttpClient httpsClient = HttpClients.custom()
                                    .setConnectionManager(connectionManager) 
                                    .build();
        return new HttpComponentsClientHttpRequestFactory(httpsClient);

     }          


     @Bean @Lazy
     @ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${it.server.trust-store:}')")
     public SSLFactory sslFactory(ResourceLoader resourceLoader, ServerConfig serverConfig) throws IOException {
        log.info("############# trust store - {}", serverConfig.getTrustStore());
        log.info("############# trust store password - {}", serverConfig.getTrustStorePassword());
        try (InputStream trustStoreStream = resourceLoader.getResource(serverConfig.getTrustStore()).getInputStream()) {
            return SSLFactory.builder()
                               .withProtocols("TLSv1.2") 
                               .withTrustMaterial(trustStoreStream, serverConfig.getTrustStorePassword())
                               .build();
        } catch (FileNotFoundException  ex) {
                throw new IllegalStateException("unable to locate truststore: " + serverConfig.getTrustStore(), ex);
        }
     }

     @Bean @Lazy
     public RestTemplate authnUser(RestTemplateBuilder builder, ClientHttpRequestFactory requestFactory){
        RestTemplate restTemplate = builder.requestFactory(
            // used to read the stream twice -- so we can use the logging filter below
            () -> new BufferingClientHttpRequestFactory(requestFactory))
                    .interceptors(new BasicAuthenticationInterceptor(username, password),
                    new RestTemplateLoggingFilter())
                    .build();

            return restTemplate;            
        
     }
    
}
