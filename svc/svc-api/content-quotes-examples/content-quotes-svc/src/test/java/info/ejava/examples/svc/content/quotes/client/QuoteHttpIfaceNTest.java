package info.ejava.examples.svc.content.quotes.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.examples.content.quotes.QuotesApplication;
import info.ejava.examples.content.quotes.api.QuotesAPI;
import info.ejava.examples.content.quotes.client.QuoteHttpIfaceImpl;
import info.ejava.examples.content.quotes.dto.MessageDTO;
import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.dto.QuoteListDTO;
import info.ejava.examples.content.quotes.util.JsonUtil;
import info.ejava.examples.content.quotes.util.QuoteDTOFactory;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {ClientTestConfiguration.class,QuotesApplication.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = "test=true")
@ActiveProfiles("test")
@Tag("springboot")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class QuoteHttpIfaceNTest {

    @Autowired
    private QuoteDTOFactory quoteDTOFactory;
    // @Autowired @Qualifier("restTemplateHttpIface")
    // private QuoteHttpIfaceAPI quoteHttpIfaceAPIRestTemplate;
    // @Autowired @Qualifier("webClientHttpIface")
    // private QuoteHttpIfaceAPI quoteHttpIfaceAPIWebClient;
     @Autowired 
     //@Qualifier("restClientHttpIface")
     @Qualifier("restClientHttpIface_1")
     private QuoteHttpIfaceImpl quoteHttpIfaceAPIRestClient;

    @Autowired
    private URI baseUrl;

    @Autowired
    private URI quotesUrl;


    
    private static final MediaType[] MEDIA_TYPES = new MediaType[] {
           MediaType.APPLICATION_JSON,
           MediaType.APPLICATION_XML
    };

    public static Stream<Arguments> mediaTypes() {
        List<Arguments> params = new ArrayList<>();
        for (MediaType  contentType  : MEDIA_TYPES) {            
            for (MediaType acceptType : MEDIA_TYPES) {
                params.add(Arguments.of(contentType,acceptType));
            }
        }
        return params.stream();
    }

    public MessageDTO getErrorResponse(RestClientResponseException ex){
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


    @BeforeEach
    public void init(){
        
        
        //ResponseEntity<QuoteDTO> respons = quoteHttpIfaceAPIRestClient.getQuote(1);
        //log.info("Quote using httpIface - {}", respons);
        ResponseEntity<Void> resp =  quoteHttpIfaceAPIRestClient.deleteAllQuotes();
        


    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    void add_valid_quote_for_type(MediaType contentType, MediaType accept) {
        // given a valid quote
        QuoteDTO validQuote  =  quoteDTOFactory.make();

        // when making request using different request and accept payload types
        
        // when - making request using different request and accept payload types
        
        ResponseEntity<QuoteDTO> response =null ;
        if(contentType.toString().equals(MediaType.APPLICATION_JSON_VALUE)){

            response = quoteHttpIfaceAPIRestClient.createQuoteJson(validQuote);
            
        }
         
        if(contentType.toString().equals(MediaType.APPLICATION_XML_VALUE)){

            response = quoteHttpIfaceAPIRestClient.createQuoteXml(validQuote);
            
        }

        
        log.info("resp. status - {} - {}", response.getStatusCode(), HttpStatus.valueOf(response.getStatusCode().value()));
        log.info("resp. body - {}", response.getBody());
        log.info("resp. header Content Type- {}", response.getHeaders().getContentType());

        // then / evaluate-assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        

        QuoteDTO createdQuote = response.getBody();
        BDDAssertions.then(createdQuote).isEqualTo(validQuote.withId(createdQuote.getId()));
        URI location = UriComponentsBuilder.fromUri(baseUrl).replacePath(QuotesAPI.QUOTE_PATH).build(createdQuote.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(location.toString());
    }

    @Test
    void get_quote() {
        // given / arrange
        QuoteDTO existingQuote = quoteDTOFactory.make();
        
        // when / act
        ResponseEntity<QuoteDTO> response = quoteHttpIfaceAPIRestClient.createQuoteJson(existingQuote);

        // then / assert -evaluate
        BDDAssertions.then(response.getStatusCode().is2xxSuccessful()).isTrue();

        int requestId = response.getBody().getId();
        URI location = UriComponentsBuilder.fromUri(baseUrl).replacePath(QuotesAPI.QUOTE_PATH).build(requestId);
        ResponseEntity<QuoteDTO> getQuote = quoteHttpIfaceAPIRestClient.getQuote(requestId);

        BDDAssertions.then(getQuote.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getQuote.getBody()).isEqualTo(existingQuote.withId(requestId));
        BDDAssertions.then(response.getHeaders().getLocation()).isEqualTo(location);
    }

     @ParameterizedTest
     @ValueSource(strings = {MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE})
     void get_quotes(String mediaTypeString){
        // given / arrange
        MediaType mediaType = MediaType.valueOf(mediaTypeString);
        Map<Integer,QuoteDTO> quotesMap = new HashMap<>();
        QuoteListDTO quotes = quoteDTOFactory.listBuilder().make(3, 3);
        for (QuoteDTO quoteDTO : quotes.getQuotes()) {
            ResponseEntity<QuoteDTO> response = quoteHttpIfaceAPIRestClient.createQuoteJson(quoteDTO);
            BDDAssertions.then(response.getStatusCode().is2xxSuccessful()).isTrue();
            QuoteDTO addedQuote = response.getBody();
            quotesMap.put(addedQuote.getId(), addedQuote);
        }
        BDDAssertions.then(quotesMap).isNotEmpty();

      
        // when / act
        ResponseEntity<QuoteListDTO> response = quoteHttpIfaceAPIRestClient.getQuotes(0, 0);
        ResponseEntity<QuoteListDTO> responseWithOffsetLimit  = quoteHttpIfaceAPIRestClient.getQuotes(2, 15 );

        // then / evaluate - assert

        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(responseWithOffsetLimit.getStatusCode()).isEqualTo(HttpStatus.OK);
        QuoteListDTO quotePage = response.getBody();
        QuoteListDTO quotePageWithOffset = responseWithOffsetLimit.getBody();

        BDDAssertions.then(quotePage.getOffset()).isEqualTo(0);
        BDDAssertions.then(quotePage.getLimit()).isEqualTo(0);
        BDDAssertions.then(quotePage.getCount()).isEqualTo(quotesMap.size());

        BDDAssertions.then(quotePageWithOffset.getOffset()).isEqualTo(2);
        BDDAssertions.then(quotePageWithOffset.getLimit()).isEqualTo(15);
        BDDAssertions.then(quotePageWithOffset.getCount()).isEqualTo(quotesMap.size()-2);

        for(QuoteDTO quote: quotePage.getQuotes()){
            BDDAssertions.then(quotesMap.remove(quote.getId())).isNotNull();
        }

        BDDAssertions.then(quotesMap).isEmpty();

     }

     @ParameterizedTest
     @MethodSource("mediaTypes")
     void add_valid_quote(MediaType contentType, MediaType accept){
        // given / arrange 
        QuoteDTO valiQuote = quoteDTOFactory.make();

        // when / act

        ResponseEntity<QuoteDTO> response = quoteHttpIfaceAPIRestClient.createQuoteJson(valiQuote);

        // then / evaluate - assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        QuoteDTO createdQuote = response.getBody();
        BDDAssertions.then(createdQuote).isEqualTo(valiQuote.withId(createdQuote.getId()));
        URI location = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(createdQuote.getId());
        BDDAssertions.then(response.getHeaders().getLocation()).isEqualTo(location);
     }

     private QuoteDTO given_an_existing_quote() {
        QuoteDTO existingQuote = quoteDTOFactory.make();
        ResponseEntity<QuoteDTO> response = quoteHttpIfaceAPIRestClient.createQuoteJson(existingQuote);
        BDDAssertions.then(response.getStatusCode().is2xxSuccessful()).isTrue();
        BDDAssertions.then((response.getStatusCode())).isEqualTo(HttpStatus.CREATED);
        return response.getBody();

     }

     @Test
     void update_an_existing_quote() {
        // given - an existing quote
        QuoteDTO existingQuote = given_an_existing_quote();
        int requestId = existingQuote.getId();

        QuoteDTO updatedQuote = existingQuote.withText(existingQuote.getText()+"Updated ");

        // when / act
        ResponseEntity<Void> response = quoteHttpIfaceAPIRestClient.updateQuote(requestId, updatedQuote);

        // then / evaluate - assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<QuoteDTO> getupdatedQuote = quoteHttpIfaceAPIRestClient.getQuote(requestId);

        BDDAssertions.then(getupdatedQuote.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getupdatedQuote.getBody()).isEqualTo(updatedQuote);
        BDDAssertions.then(getupdatedQuote.getBody()).isNotEqualTo(existingQuote);

     }

     @Test
     void get_quote_1(){
        // given / arrange
        QuoteDTO existingQuote = quoteDTOFactory.make();
        ResponseEntity<QuoteDTO> response = quoteHttpIfaceAPIRestClient.createQuoteJson(existingQuote);
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        int requestId = response.getBody().getId();

        // when / act
        ResponseEntity<QuoteDTO>  getQuote = quoteHttpIfaceAPIRestClient.getQuote(requestId);

        // then
        BDDAssertions.then(getQuote.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getQuote.getBody()).isEqualTo(existingQuote.withId(requestId));
     }

     protected List<QuoteDTO> given_many_quotes(int count) {
        List<QuoteDTO> quotes = new ArrayList<>(count);
        for (QuoteDTO quoteDTO : quoteDTOFactory.listBuilder().quotes(3, 3)) {
            ResponseEntity<QuoteDTO> response = quoteHttpIfaceAPIRestClient.createQuoteJson(quoteDTO);
            BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            quotes.add(response.getBody());
        }
        return quotes;
     }

     @Test
     void get_random_quote() {
        // given 
        given_many_quotes(5);
        // when
        ResponseEntity<QuoteDTO> resp = quoteHttpIfaceAPIRestClient.randomQuote();

        // then
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        QuoteDTO returnedQuote = resp.getBody();
        
        URI location = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(returnedQuote.getId());
        BDDAssertions.then(resp.getHeaders().getFirst(HttpHeaders.CONTENT_LOCATION)).isEqualTo(location.toString());
     }

}