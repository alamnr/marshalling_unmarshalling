package info.ejava.examples.content.quotes.client;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.examples.common.web.ServerConfig;
import info.ejava.examples.content.quotes.api.QuotesAPIRestClient;
import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.dto.QuoteListDTO;

public class QuotesApiRestClientImpl  implements QuotesAPIRestClient{

    private final URI baseUrl;
    private final RestClient restClient;
    private final MediaType mediaType;

    public QuotesApiRestClientImpl(RestClient restClient, ServerConfig serverConfig, String mediaType)
    {
        this.baseUrl = serverConfig.getBaseUrl();
        this.restClient = restClient;
        this.mediaType = MediaType.valueOf(mediaType);
    }

    public QuotesApiRestClientImpl(RestClient restClient, ServerConfig serverConfig){
        this(restClient, serverConfig, MediaType.APPLICATION_JSON_VALUE);
    }

    @Override
    public ResponseEntity<QuoteDTO> createQuote(QuoteDTO quote) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(QUOTES_PATH).build().toUri();

        ResponseEntity<QuoteDTO> response = restClient.post().uri(url).contentType(mediaType)
                                                .accept(mediaType).body(quote).retrieve().toEntity(QuoteDTO.class);

        return response;
    }

    @Override
    public ResponseEntity<Void> updateQuote(int id, QuoteDTO quote) {
        URI uri = UriComponentsBuilder.fromUri(baseUrl).path(QUOTE_PATH).build(id);
        ResponseEntity<Void> response = restClient.put().uri(uri).contentType(mediaType).body(quote).retrieve().toEntity(Void.class);
        return response;

    }

    @Override
    public ResponseEntity<Void> containsQuote(int id) {
        URI uri = UriComponentsBuilder.fromUri(baseUrl).path(QUOTE_PATH).build(id);
        ResponseEntity<Void> response = restClient.head().uri(uri).retrieve().toEntity(Void.class);
        return response;
    }

    @Override
    public ResponseEntity<QuoteDTO> getQuote(int id) {
        URI uri = UriComponentsBuilder.fromUri(baseUrl).path(QUOTE_PATH).build(id);
        ResponseEntity<QuoteDTO> response = restClient.get().uri(uri).accept(mediaType).retrieve().toEntity(QuoteDTO.class);
        return response;

    }

    @Override
    public ResponseEntity<QuoteDTO> randomQuote() {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(RANDOM_QUOTE_PATH).build().toUri();

        RequestEntity<?> request = RequestEntity.get(url).build();
        ResponseEntity<QuoteDTO> response = restClient.get().uri(url).retrieve().toEntity(QuoteDTO.class);
        return response;
    }

    @Override
    public ResponseEntity<Void> deleteQuote(int id) {
        URI url = UriComponentsBuilder.fromUri(baseUrl).path(QUOTES_PATH).build(id);
        ResponseEntity<Void>  response = restClient.delete().uri(url).retrieve().toEntity(Void.class);
        return response;
    }

    @Override
    public ResponseEntity<Void> deleteAllQuotes() {
        URI uri  = UriComponentsBuilder.fromUri(baseUrl).path(QUOTES_PATH).build().toUri();
        ResponseEntity<Void> response = restClient.delete().uri(uri).retrieve().toEntity(Void.class);
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
        ResponseEntity<QuoteListDTO> response = restClient.get().uri(url).retrieve().toEntity(QuoteListDTO.class);
        return response;

    }   
    
}
