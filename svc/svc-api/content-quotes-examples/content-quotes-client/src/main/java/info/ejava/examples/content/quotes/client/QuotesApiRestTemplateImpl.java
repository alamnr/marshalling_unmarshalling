package info.ejava.examples.content.quotes.client;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.examples.common.web.ServerConfig;
import info.ejava.examples.content.quotes.api.QuotesAPIRestClient;
import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.dto.QuoteListDTO;
import reactor.core.publisher.Mono;

public class QuotesApiRestTemplateImpl implements QuotesAPIRestClient {
    private final URI baseUrl;
    private final RestTemplate restTemplate;
    private final MediaType mediaType;

    public QuotesApiRestTemplateImpl(RestTemplate restTemplate, ServerConfig serverConfig, String mediaType)
    {
        this.baseUrl = serverConfig.getBaseUrl();
        this.restTemplate = restTemplate;
        this.mediaType = MediaType.valueOf(mediaType);
    }

    public QuotesApiRestTemplateImpl(RestTemplate restTemplate, ServerConfig serverConfig){
        this(restTemplate, serverConfig, MediaType.APPLICATION_JSON_VALUE);
    }

    @Override
    public ResponseEntity<QuoteDTO> createQuote(QuoteDTO quote) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(QUOTES_PATH).build().toUri();

        RequestEntity<QuoteDTO> request = RequestEntity.post(url).contentType(mediaType)
                                                    .accept(mediaType).body(quote);
        
        ResponseEntity<QuoteDTO> response = restTemplate.exchange(request, QuoteDTO.class);

        return response;
    }

    @Override
    public ResponseEntity<Void> updateQuote(int id, QuoteDTO quote) {
        URI uri = UriComponentsBuilder.fromUri(baseUrl).path(QUOTE_PATH).build(id);
        RequestEntity<QuoteDTO> request = RequestEntity.put(uri).contentType(mediaType).body(quote);
        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
        return response;

    }

    @Override
    public ResponseEntity<Void> containsQuote(int id) {
        URI uri = UriComponentsBuilder.fromUri(baseUrl).path(QUOTE_PATH).build(id);

        RequestEntity<Void> request = RequestEntity.head(uri).build();
        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
        return response;
    }

    @Override
    public ResponseEntity<QuoteDTO> getQuote(int id) {
        URI uri = UriComponentsBuilder.fromUri(baseUrl).path(QUOTE_PATH).build(id);
        RequestEntity<Void> request = RequestEntity.get(uri).accept(mediaType).build();
        ResponseEntity<QuoteDTO> response = restTemplate.exchange(request, QuoteDTO.class);
        return response;

    }

    @Override
    public ResponseEntity<QuoteDTO> randomQuote() {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(RANDOM_QUOTE_PATH).build().toUri();

        RequestEntity<?> request = RequestEntity.get(url).build();
        ResponseEntity<QuoteDTO> response = restTemplate.exchange(request, QuoteDTO.class);
        return response;
    }

    @Override
    public ResponseEntity<Void> deleteQuote(int id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(QUOTES_PATH).build(id);

        RequestEntity<?> request = RequestEntity.delete(url).build();
        ResponseEntity<Void>  response = restTemplate.exchange(request, Void.class);
        return response;
    }

    @Override
    public ResponseEntity<Void> deleteAllQuotes() {
        URI uri  = UriComponentsBuilder.fromUri(baseUrl).path(QUOTES_PATH).build().toUri();
        RequestEntity<?> request = RequestEntity.delete(uri).build();
        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
        return response;
    }

    @Override
    public ResponseEntity<QuoteListDTO> getQuotes(Integer offset, Integer limit) {
         UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(QUOTES_PATH);
        if (offset!=null) {
            urlBuilder = urlBuilder.queryParam("offset", offset);
        }
        if (limit!=null) {
            urlBuilder = urlBuilder.queryParam("limit", limit);
        }

         URI url = urlBuilder.build().toUri();

        RequestEntity<?> request = RequestEntity.get(url).build();
        ResponseEntity<QuoteListDTO> response = restTemplate.exchange(request, QuoteListDTO.class);
        return response;

    }   
}
