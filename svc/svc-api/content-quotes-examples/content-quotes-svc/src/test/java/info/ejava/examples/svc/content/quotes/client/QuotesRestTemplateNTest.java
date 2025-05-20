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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientResponseException;
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
import io.micrometer.core.ipc.http.HttpSender.Request;
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

    @ParameterizedTest
    @MethodSource("mediaTypes")
    void add_valid_quote(MediaType contentType, MediaType accept) {
        // given / arrange - a valid quote
        QuoteDTO validQuote = quoteDTOFactory.make();

       RequestEntity request =  RequestEntity.post(quotesUrl).accept(accept).contentType(contentType)
                                                            .body(validQuote);
                                                            
        // when / act 
        ResponseEntity<QuoteDTO> response = restTemplate.exchange(request, QuoteDTO.class);
                                                            
        
        // then 
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // that equals what we sent plus an id generated
        
        QuoteDTO createdQuote = response.getBody();
        BDDAssertions.then(createdQuote).isEqualTo(validQuote.withId(createdQuote.getId()));
        // a lcation response header referencing the URL for the created resources
        URI location = UriComponentsBuilder.fromUri(baseUrl) .path(QuotesAPI.QUOTE_PATH).build(createdQuote.getId());
        BDDAssertions.then(response.getHeaders().getFirst(HttpHeaders.LOCATION)).as("location error").isEqualTo(location.toString());
    }

    
    private QuoteDTO given_an_existing_quote(){
        QuoteDTO existingQuote = quoteDTOFactory.make();
        ResponseEntity<QuoteDTO> response =  restTemplate.exchange(RequestEntity.post(quotesUrl).accept(MediaType.APPLICATION_JSON)
                                                            .contentType(MediaType.APPLICATION_XML)
                                                            .body(existingQuote),QuoteDTO.class);
        BDDAssertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    @Test
    void update_existing_quote( ){
        // given - an existing quote
        QuoteDTO existingQuote = given_an_existing_quote();
        int requestId = existingQuote.getId();

        // and an update 
        QuoteDTO updatedQuote = existingQuote.withText(existingQuote.getText() + "Updated ");

        URI updateUri = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(existingQuote.getId());

        // when - updating existing quote
        ResponseEntity<Void> response = restTemplate.exchange(RequestEntity.put(updateUri).contentType(MediaType.APPLICATION_JSON)
                                                        .body(updatedQuote), Void.class);
                                                        
        // then - evaluate / assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        URI getUri = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId);
        ResponseEntity<QuoteDTO> getUpdatedQuote = restTemplate.exchange(RequestEntity.get(getUri).build(),QuoteDTO.class);

        BDDAssertions.then(getUpdatedQuote.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getUpdatedQuote.getBody()).isEqualTo(updatedQuote);
        BDDAssertions.then(getUpdatedQuote.getBody()).isNotEqualTo(existingQuote);

    }

    
    @Test
    void get_quote_1() {
        // given / arrange
        QuoteDTO existingQuote = quoteDTOFactory.make();
        ResponseEntity<QuoteDTO> response =restTemplate.exchange(
                            RequestEntity.post(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri())
                                    .body(existingQuote),QuoteDTO.class);
                                    
        
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        int requestId = response.getBody().getId();

        // when / act

        ResponseEntity<QuoteDTO> getQuote = restTemplate.exchange( 
                RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId)).build(),QuoteDTO.class);

        // then
        BDDAssertions.then(getQuote.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getQuote.getBody()).isEqualTo(existingQuote.withId(requestId));
    }

        protected List<QuoteDTO> given_many_quotes(int count) {
        List<QuoteDTO> quotes = new ArrayList<>(count);
        for (QuoteDTO quoteDTO : quoteDTOFactory.listBuilder().quotes(count,count)) {
                ResponseEntity<QuoteDTO> response = restTemplate.exchange(RequestEntity
                                    .post(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri())
                                    .body(quoteDTO),QuoteDTO.class);

                BDDAssertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                quotes.add(response.getBody());
        }
        return quotes;
    }

    @Test 
    void get_random_quote() {
        // given / arrange - many quotes
        given_many_quotes(5);

        // When
        ResponseEntity<QuoteDTO> resp = restTemplate.exchange(RequestEntity.get(
                                        UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.RANDOM_QUOTE_PATH).build().toUri()).build()
                                        ,QuoteDTO.class);
                                                
        // then
        BDDAssertions.assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        QuoteDTO returnedQuote = resp.getBody();
        String expectedLocation = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(returnedQuote.getId()).toString();
        BDDAssertions.then(resp.getHeaders().getFirst(HttpHeaders.CONTENT_LOCATION)).isEqualTo(expectedLocation);
    }

    @Test
    void remove_quote(){
        // given 
        List<QuoteDTO> quotes = given_many_quotes(5);
        int requestId = quotes.get(1).getId();
        BDDAssertions.assertThat(restTemplate.exchange(
            RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId)).build()
                                    ,QuoteDTO.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        // when requested to remove
        ResponseEntity<Void> resp = restTemplate.exchange(
                    RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId)).build()
                                    ,Void.class);
        // then

        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
                                            () ->restTemplate.exchange(
                                             RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl)
                                            .path(QuotesAPI.QUOTE_PATH).build(requestId)).build(),QuoteDTO.class) ,
                                             RestClientResponseException.class) ;  
        BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

        @Test
        void remove_all_quotes() {
        // given  / arrange
        List<QuoteDTO> quotes = given_many_quotes(3);

        // when / act- requested to remove all quotes
        ResponseEntity<Void> resp = restTemplate.exchange(
                        RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri()).build()
                                    ,Void.class);
                                    
        // then
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        for (QuoteDTO quoteDTO : quotes) {
               RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
                        ()->restTemplate.exchange(RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH)
                        .build(quoteDTO.getId())).build(),QuoteDTO.class),
                         RestClientResponseException.class) ;
                BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }

    @Test
    void remove_unknown_quote() {
        // given 
        int requestId = 13;

        // when - requested to remove will not report that does not exist i.e. idempotent
        ResponseEntity<Void> resp = restTemplate.exchange(
                        RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId)).build()
                                  ,Void.class);
                                    
        // then
        BDDAssertions.then(resp.getStatusCode().is2xxSuccessful()).isTrue();

    }

    @Test
    void get_random_quote_no_quotes(){
        // given
        BDDAssertions.assertThat(restTemplate.exchange(
            RequestEntity.delete(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH)
                        .build().toUri()).build(),Void.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // then
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> restTemplate.exchange(
            RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.RANDOM_QUOTE_PATH).build().toUri()).build()
                        ,QuoteDTO.class)
                                    , RestClientResponseException.class);
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getText()).contains("no quotes");
        log.info("random quote -  {}", errMsg);
    }

    @Test
    void get_unknown_quote(){
        // given
        int unknownId =13;

        // when - requesting quote by id

        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () ->restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(unknownId)).build()
                        ,QuoteDTO.class)        
                            , RestClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getText()).contains(String.format("quote-[%s]", unknownId));

    }

    @Test
    void update_unknown_quote() {
        // given

        int unknownId = 13;
        QuoteDTO updateQuote = quoteDTOFactory.make();

        // verify that updating existing quote
        RestClientResponseException ex =  BDDAssertions.catchThrowableOfType(
            ()-> restTemplate.exchange(
                RequestEntity.put(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(unknownId))
                    .body(updateQuote),Void.class)
            , RestClientResponseException.class);

        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        MessageDTO errMsg = getErrorResponse(ex);
        BDDAssertions.then(errMsg.getText()).contains(String.format("quote-[%d]", unknownId));
    }

    @Test
    @Disabled
    void update_known_quote_with_bad_quote() {
        // given
        List<QuoteDTO> quotes = given_many_quotes(3);
        log.info("quotes - {}", quotes);
        int knownId = 22;
        QuoteDTO badQuoteMissingText = new QuoteDTO();
        ResponseEntity<QuoteDTO> resp = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(knownId)).build()
                    ,QuoteDTO.class);
        log.info("resp - {}", resp.getBody());

        // when
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
         () -> restTemplate.exchange(
                RequestEntity.put(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(knownId))
                    .body(badQuoteMissingText),Void.class)
                    , RestClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        BDDAssertions.then(getErrorResponse(ex).getText()).contains(String.format("missing required text", knownId));
    }

    @Test
    @Disabled
    void add_bad_quote_rejected() {
        // given
        
        QuoteDTO badQuoteMissingText = new QuoteDTO();
        // ResponseEntity<QuoteDTO> resp =   webClient.post()
        //             .uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri())
        //             .bodyValue(badQuoteMissingText)
        //             .retrieve().toEntity(QuoteDTO.class).block();
        // log.info("resp - {} ", resp.getBody());

        
        // when
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
        //  () -> webClient.post().uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri())
        //             .bodyValue(badQuoteMissingText).retrieve().toEntity(QuoteDTO.class).block()
        () -> restTemplate.exchange(
            RequestEntity.post(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri())
                    .body(badQuoteMissingText) ,QuoteDTO.class)
                    , RestClientResponseException.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        BDDAssertions.then(getErrorResponse(ex).getText()).contains(String.format("missing required text", ""));
    }

     public static class IntegerConverter implements ArgumentConverter {
        @Override
        public Object convert(Object o, ParameterContext parameterContext) throws ArgumentConversionException {
            return o.equals("null") ? null : Integer.parseInt((String)o);
        }
    }

    @Disabled
    @ParameterizedTest
    @CsvSource({"-1,null", "null,-5"})
    void get_invalid_offset_limit(@ConvertWith(IntegerConverter.class)Integer offset,
                                    @ConvertWith(IntegerConverter.class)Integer limit){
        // when - requesting invalid offset
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH);
        if (offset!=null) {
            urlBuilder = urlBuilder.queryParam("offset", offset);
        }
        if (limit!=null) {
            urlBuilder = urlBuilder.queryParam("limit", limit);
        }
        URI url = urlBuilder.build().toUri();

        
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            ()-> restTemplate.exchange(RequestEntity.get(url).build(), QuoteListDTO.class)
     
                , RestClientResponseException.class);
        // then - error was reported

        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void get_empty_quotes(){
        // given - we have no quotes
        Integer offset = 0;
        Integer limit = 100;
         UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH);
        if (offset!=null) {
            urlBuilder = urlBuilder.queryParam("offset", offset);
        }
        if (limit!=null) {
            urlBuilder = urlBuilder.queryParam("limit", limit);
        }
        URI url = urlBuilder.build().toUri();

        
         //when - asked for amounts we do not have
        ResponseEntity<QuoteListDTO> response = restTemplate.exchange(RequestEntity.get(url).accept(MediaType.APPLICATION_JSON).build()
                                                ,QuoteListDTO.class);
        log.debug("{}", response);

        //then - the response will be empty
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        QuoteListDTO returnedQuotes = response.getBody();
        BDDAssertions.then(returnedQuotes.getCount()).isEqualTo(0);
        //and descriptive attributes filed in
        BDDAssertions.then(returnedQuotes.getOffset()).isEqualTo(0);
        BDDAssertions.then(returnedQuotes.getLimit()).isEqualTo(100);
        BDDAssertions.then(returnedQuotes.getTotal()).isEqualTo(0);       

    }

    @Test
    void get_many_quotes() {
        // given many quotes
        given_many_quotes(100);

        //when asking for a page of quotes
         Integer offset = 10;
        Integer limit = 10;
         UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH);
        if (offset!=null) {
            urlBuilder = urlBuilder.queryParam("offset", offset);
        }
        if (limit!=null) {
            urlBuilder = urlBuilder.queryParam("limit", limit);
        }
        URI url = urlBuilder.build().toUri();

        ResponseEntity<QuoteListDTO> response = restTemplate.exchange(RequestEntity.get(url).build(), QuoteListDTO.class);

        //then - page of results returned
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        QuoteListDTO returnedQuotes = response.getBody();
        log.debug("{}", returnedQuotes);
        BDDAssertions.then(returnedQuotes.getCount()).isEqualTo(10);
        QuoteDTO quote0 = returnedQuotes.getQuotes().get(0);
        BDDAssertions.then(quote0.getId()).isGreaterThan(1);
        BDDAssertions.then(returnedQuotes.getQuotes().get(9).getId()).isEqualTo(quote0.getId()+9);

        //and descriptive attributes filed in
        BDDAssertions.then(returnedQuotes.getOffset()).isEqualTo(10);
        BDDAssertions.then(returnedQuotes.getLimit()).isEqualTo(10);
        BDDAssertions.then(returnedQuotes.getTotal()).isEqualTo(100);
    }  
}
