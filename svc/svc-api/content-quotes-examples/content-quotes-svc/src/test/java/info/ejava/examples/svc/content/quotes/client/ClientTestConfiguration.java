package info.ejava.examples.svc.content.quotes.client;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties.Restclient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.NoOpResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.client.support.RestTemplateAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.examples.common.web.ServerConfig;
import info.ejava.examples.content.quotes.api.QuotesAPI;
import info.ejava.examples.content.quotes.util.QuoteDTOFactory;
import info.ejava.examples.svc.content.quotes.filter.RestTemplateLoggingFilter;
import info.ejava.examples.svc.content.quotes.filter.WebClientLoggingFilter;

/*
 * A test configuration used by remote test  client
 */
@TestConfiguration
public class ClientTestConfiguration {
    
    @Bean
    ClientHttpRequestFactory requestFactory(){
        return new SimpleClientHttpRequestFactory();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, ClientHttpRequestFactory requestFactory ) {
        //return builder.build();
        // or just following will work in the simple cases like this 
        // return new RestTemplate();
        return builder.requestFactory(
              // used to read the Stream twice -- so we can use the logging filter below
              () -> new BufferingClientHttpRequestFactory(requestFactory))
              .interceptors(List.of(new RestTemplateLoggingFilter())).build();
        
    }

    @Bean 
    public RestClient restClient(RestClient.Builder builder, RestTemplate restTemplate){
        return builder.build();
        // return RestClient.create(restTemplate);
                        
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder){
        // return builder.build();
        // or just following will work in the simple cases like this
        // return WebClient.builder().build();
        return builder.filter(WebClientLoggingFilter.requestFilter())
                        .filter(WebClientLoggingFilter.responseFilter())
                            .build();
    }

    @Bean
    public QuoteDTOFactory quoteDTOFactory(){
        return new QuoteDTOFactory();
    }

    @Bean @Lazy
    public ServerConfig serverConfig(@LocalServerPort int port) {
        return new ServerConfig().withPort(port).build();
    }

    @Bean @Lazy
    public URI baseUrl(ServerConfig serverConfig){
        return serverConfig.getBaseUrl();
    }

    
    @Bean @Lazy
    public URI quotesUrl(URI baseUrl) {
        return UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri();
    }

    @Bean @Lazy @Qualifier("restTemplateHttpIface")
    // This could be done with RestTemplate, RestClient or WebClient
    //public QuoteHttpIfaceAPI quoteApiRestTemplate(RestTemplate restTemplate ){
    public QuoteHttpIfaceAPI quoteApiRestTemplate(URI baseUrl, RestTemplateBuilder builder, ClientHttpRequestFactory requestFactory ){
        /*
         RestTemplate is the only client option that allows one to bypass the exception rule and obtain an
         error ResponseEntity from the call without exception handling. The following example shows a
         NoOpResponseErrorHandler error handler being put in place and the caller is receiving the error
         ResponseEntity without using exception handling.
         */
        //configure RestTemplate to return error responses, not exceptions
         builder.requestFactory(
              // used to read the Stream twice -- so we can use the logging filter below
              () -> new BufferingClientHttpRequestFactory(requestFactory))
              .interceptors(List.of(new RestTemplateLoggingFilter())).build();
        
        RestTemplate restTemplate = builder.rootUri(baseUrl.toString()).build();
        restTemplate.setErrorHandler(new NoOpResponseErrorHandler());

        RestTemplateAdapter adapter = RestTemplateAdapter.create(restTemplate);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(QuoteHttpIfaceAPI.class);

    }


    // @Bean @Lazy @Qualifier("restClientHttpIface")
    // public QuoteHttpIfaceAPI quoteApiRestClient(RestClient restClient) {
    //     RestClientAdapter adapter = RestClientAdapter.create(restClient);
    //     HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
    //     return factory.createClient(QuoteHttpIfaceAPI.class);
    // }

    // @Bean @Lazy @Qualifier("webClientHttpIface")
    // public QuoteHttpIfaceAPI quoteApiWebClient(WebClient webClient){
    //     WebClientAdapter adapter = WebClientAdapter.create(webClient);
    //     HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
    //     return factory.createClient(QuoteHttpIfaceAPI.class);
    // }
}
