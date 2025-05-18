package info.ejava.examples.svc.content.quotes.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.examples.content.quotes.QuotesApplication;
import info.ejava.examples.content.quotes.api.QuotesAPI;
import info.ejava.examples.content.quotes.dto.MessageDTO;
import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.dto.QuoteListDTO;
import info.ejava.examples.content.quotes.util.JsonUtil;
import info.ejava.examples.content.quotes.util.QuoteDTOFactory;
import lombok.extern.slf4j.Slf4j;

/*
 * This test was put in place with RestTemplate so that we could leverage the 
 * ability for restTemplate filters to log pay load bodies in debug mode
 */

@SpringBootTest(classes = {ClientTestConfiguration.class, QuotesApplication.class},
                 webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                 properties = "test=true")
@ActiveProfiles("test")
@Tag("springboot")
@Slf4j
public class QuotesRestTemplateNTest {

    @Autowired
    private QuoteDTOFactory quoteDTOFactory;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private URI baseUrl;
    @Autowired
    private URI quotesUrl;
    
    private static final MediaType[] MEDIA_TYPES = new MediaType[] {
        MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML
    };
    
    @BeforeEach
    public void init() {
        log.info("clearing all quotes");
        restTemplate.delete(quotesUrl);
    }

    public static Stream<Arguments> mediaTypes() {
        List<Arguments> params = new ArrayList<>();
        for (MediaType  contentType  : MEDIA_TYPES) {            
            for (MediaType acceptType : MEDIA_TYPES) {
                params.add(Arguments.of(contentType,acceptType));
            }
        }
        return params.stream();
    }

    public MessageDTO getErrorResponse(HttpClientErrorException ex){
        final String contentTypeValue = ex.getResponseHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        final MediaType contentType = MediaType.valueOf(contentTypeValue);
        final byte[] bytes = ex.getResponseBodyAsByteArray();
        if (MediaType.APPLICATION_JSON.equals(contentType)) {
            return JsonUtil.instance().unmarshal(bytes, MessageDTO.class);
        } else if (MediaType.APPLICATION_XML.equals(contentType)) {
            return JsonUtil.instance().unmarshal(bytes, MessageDTO.class);
        } else {
            throw new IllegalArgumentException("unknown contentType: " + contentTypeValue);
        }


    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    public void add_valid_quote_for_type(MediaType contentType, MediaType accept){

        // given - a valid quote
      
        QuoteDTO validQuote = quoteDTOFactory.make();
        log.info("Content-Type-{}, Accept-Type-{}, quote -{}", contentType, accept, validQuote);

        // when - making a request with different content and accept payload types
        RequestEntity<QuoteDTO> request = RequestEntity.post(quotesUrl)
                                                    .contentType(contentType)
                                                    .accept(accept)
                                                    .body(validQuote);
        log.info("req. body - {}", request.getBody());
        log.info("req. http method - {}", request.getMethod());
        log.info("req. content type - {}", request.getHeaders().getContentType());
        log.info("req. accept type - {}", request.getHeaders().getAccept());
        ResponseEntity<QuoteDTO> response = restTemplate.exchange(request, QuoteDTO.class);

        log.info("resp. body - {}", response.getBody());
        log.info("resp. content type - {}", response.getHeaders().getContentType());
        
        // then - the service will accept the format we supplied
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BDDAssertions.then(response.getHeaders().getContentType()).isEqualTo(accept);
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo(accept.toString());

        // that equals what we sent and plus an ID generated
        QuoteDTO createdQuote = response.getBody();
        BDDAssertions.then(createdQuote).isEqualTo(validQuote.withId(createdQuote.getId()));
        // with a location reponse header referencing the URI of the created quote
        URI location = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(createdQuote.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(location.toString());

    }

    @Test
    void get_quote(){
        // given/ arrange - an existing quote
        QuoteDTO existingQuote = quoteDTOFactory.make();
        ResponseEntity<QuoteDTO> quoteResponse = restTemplate.postForEntity(quotesUrl, existingQuote, QuoteDTO.class);
        BDDAssertions.assertThat(quoteResponse.getStatusCode().is2xxSuccessful()).isTrue();

        int requestId = quoteResponse.getBody().getId();
        URI quoteUrl = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId);
        RequestEntity<Void> request = RequestEntity.get(quoteUrl).build();

        // when / act - requesting quote get by id
        ResponseEntity<QuoteDTO> response  = restTemplate.exchange(request, QuoteDTO.class);

        // then
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(response.getBody()).isEqualTo(existingQuote.withId(requestId));

    }

    @ParameterizedTest
    @ValueSource(strings = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    void get_quotes(String mediaTypeString){
        // given / arrange
        MediaType mediaType = MediaType.valueOf(mediaTypeString);
        Map<Integer,QuoteDTO> existingQuotes = new HashMap<>();
        QuoteListDTO quotes = quoteDTOFactory.listBuilder().make(3, 3);
        for (QuoteDTO quote : quotes.getQuotes()) {
            RequestEntity<QuoteDTO> request = RequestEntity.post(quotesUrl).body(quote);
            ResponseEntity<QuoteDTO> response = restTemplate.exchange(request, QuoteDTO.class);
            BDDAssertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            QuoteDTO addedQuote = response.getBody();
            existingQuotes.put(addedQuote.getId(), addedQuote);
        }
        BDDAssertions.assertThat(existingQuotes).isNotEmpty();
        URI quotesUri = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri();
        URI quotesUriWithOffsetAndLimit = UriComponentsBuilder.fromUri(baseUrl)
                                        .path(QuotesAPI.QUOTES_PATH)
                                        .queryParam("offset", 1)
                                        .queryParam("limit", 20)
                                        .build().toUri();

        // when / act
        
        ResponseEntity<QuoteListDTO> response = restTemplate.exchange(
                                                    RequestEntity.get(quotesUri).accept(mediaType).build(), 
                                                    QuoteListDTO.class) ;
        ResponseEntity<QuoteListDTO> responseWithOffsetAndLimit = restTemplate.exchange(
                                                    RequestEntity.get(quotesUriWithOffsetAndLimit).accept(mediaType).build()
                                                                    , QuoteListDTO.class) ;

        // then / evaluate and assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(responseWithOffsetAndLimit.getStatusCode()).isEqualTo(HttpStatus.OK);
        QuoteListDTO quotePageWithoutOffsetAndLimit = response.getBody();
        QuoteListDTO quotePageWithOffsetAndLimit = responseWithOffsetAndLimit.getBody();

        BDDAssertions.then(quotePageWithoutOffsetAndLimit.getOffset()).isEqualTo(0);
        BDDAssertions.then(quotePageWithoutOffsetAndLimit.getLimit()).isEqualTo(0);
        BDDAssertions.then(quotePageWithOffsetAndLimit.getOffset()).isEqualTo(1);
        BDDAssertions.then(quotePageWithOffsetAndLimit.getLimit()).isEqualTo(20);

        BDDAssertions.then(quotePageWithoutOffsetAndLimit.getCount()).isEqualTo(existingQuotes.size());
        BDDAssertions.then(quotePageWithOffsetAndLimit.getCount()).isEqualTo(existingQuotes.size()-1);
        
         for (QuoteDTO q: quotePageWithoutOffsetAndLimit.getQuotes()) {
            BDDAssertions.then(existingQuotes.remove(q.getId())).isNotNull();
        }
        BDDAssertions.then(existingQuotes).isEmpty();


    }

}
