package info.ejava.examples.content.quotes.client;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.examples.common.web.ServerConfig;
import info.ejava.examples.content.quotes.api.QuotesAPI;
import info.ejava.examples.content.quotes.api.QuotesAPIWebClient;
import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.dto.QuoteListDTO;
import reactor.core.publisher.Mono;

public class QuotesApiWebClientImpl implements QuotesAPIWebClient{
 
    private final URI baseUrl;
    private final WebClient webClient;
    private final MediaType mediaType;

    public QuotesApiWebClientImpl(WebClient webClient, ServerConfig serverConfig, String mediaType)
    {
        this.baseUrl = serverConfig.getBaseUrl();
        this.webClient = webClient;
        this.mediaType = MediaType.valueOf(mediaType);
    }

    public QuotesApiWebClientImpl(WebClient webClient, ServerConfig serverConfig){
        this(webClient, serverConfig, MediaType.APPLICATION_JSON_VALUE);
    }

    @Override
    public Mono<ResponseEntity<QuoteDTO>> createQuote(QuoteDTO quote) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(QUOTES_PATH).build().toUri();

        WebClient.RequestHeadersSpec<?> request = webClient.post()
                                                    .uri(url).contentType(mediaType)
                                                    .accept(mediaType).body(Mono.just(quote),QuoteDTO.class);
        return request.retrieve().toEntity(QuoteDTO.class);
    }

    @Override
    public Mono<ResponseEntity<Void>> updateQuote(int id, QuoteDTO quote) {
        URI uri = UriComponentsBuilder.fromUri(baseUrl).path(QUOTE_PATH).build(id);
        WebClient.RequestHeadersSpec<?> request = webClient.put()
                                                    .uri(uri).contentType(mediaType)
                                                    .accept(mediaType).body(Mono.just(quote),QuoteDTO.class);
        return request.retrieve().toEntity(Void.class);

    }

    @Override
    public Mono<ResponseEntity<Void>> containsQuote(int id) {
        URI uri = UriComponentsBuilder.fromUri(baseUrl).path(QUOTE_PATH).build(id);

        WebClient.RequestHeadersSpec<?> request = webClient.head()
                                                    .uri(uri);
        return request.retrieve().toEntity(Void.class);
    }

    @Override
    public Mono<ResponseEntity<QuoteDTO>> getQuote(int id) {
        URI uri = UriComponentsBuilder.fromUri(baseUrl).path(QUOTE_PATH).build(id);
        WebClient.RequestHeadersSpec<?> request = webClient.get().uri(uri).accept(mediaType);
        return request.retrieve().toEntity(QuoteDTO.class);

    }

    @Override
    public Mono<ResponseEntity<QuoteDTO>> randomQuote() {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(RANDOM_QUOTE_PATH).build().toUri();

        WebClient.RequestHeadersSpec<?> request = webClient.get().uri(url);
        return request.retrieve().toEntity(QuoteDTO.class);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteQuote(int id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(QUOTES_PATH).build(id);

        WebClient.RequestHeadersSpec<?> request = webClient.delete().uri(url);
        return request.retrieve().toEntity(Void.class);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteAllQuotes() {
        URI uri  = UriComponentsBuilder.fromUri(baseUrl).path(QUOTES_PATH).build().toUri();
        WebClient.RequestHeadersSpec<?> request = webClient.delete().uri(uri);
        return request.retrieve().toEntity(Void.class);
    }

    @Override
    public Mono<ResponseEntity<QuoteListDTO>> getQuotes(Integer offset, Integer limit) {
         UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(QUOTES_PATH);
        if (offset!=null) {
            urlBuilder = urlBuilder.queryParam("offset", offset);
        }
        if (limit!=null) {
            urlBuilder = urlBuilder.queryParam("limit", limit);
        }

         URI url = urlBuilder.build().toUri();

        WebClient.RequestHeadersSpec<?> request = webClient.get().uri(url);
        WebClient.ResponseSpec responseSpec = request.retrieve();
        return responseSpec.toEntity(QuoteListDTO.class);

    }

    
    

}
