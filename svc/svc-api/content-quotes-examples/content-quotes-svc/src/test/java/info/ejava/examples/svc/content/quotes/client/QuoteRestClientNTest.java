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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.examples.content.quotes.QuotesApplication;
import info.ejava.examples.content.quotes.api.QuotesAPI;
import info.ejava.examples.content.quotes.dto.MessageDTO;
import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.dto.QuoteListDTO;
import info.ejava.examples.content.quotes.util.JsonUtil;
import info.ejava.examples.content.quotes.util.QuoteDTOFactory;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@SpringBootTest(classes = {ClientTestConfiguration.class,QuotesApplication.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = "test=true"    
                )
@ActiveProfiles("test")
@Tag("springboot")
@Slf4j
public class QuoteRestClientNTest {
    
    @Autowired
    private QuoteDTOFactory quoteDTOFactory;
    @Autowired
    private RestClient restClient;
    @Autowired
    private URI baseUrl;
    @Autowired
    private URI quotesUrl;

    private static final MediaType[] MEDIA_TYPES = new MediaType[] {
        MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML
    };

    @BeforeEach
    public void init(){
        log.info("clearing all quotes");
        ResponseEntity<Void> response = restClient.delete().uri(quotesUrl).retrieve().toEntity(Void.class);
        log.info("resp status -{}", response.getStatusCode());
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @AfterEach
    public void cleanUp(){
        // clean up
    }

    public static Stream<Arguments> mediaTypes() {
            List<Arguments> params = new ArrayList<>();
            for (MediaType contenType : MEDIA_TYPES) {
                for (MediaType accept : MEDIA_TYPES) {
                    params.add(Arguments.of(contenType,accept));
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

        // when - making request using different request and accept payload types
        RequestEntity<QuoteDTO> request = RequestEntity.post(quotesUrl)
                                                    .contentType(contentType)
                                                    .accept(accept)
                                                    .body(validQuote);
        
        log.info("req. body - {}", request.getBody());
        log.info("req. method - {}", request.getMethod());
        log.info("req. header Content Type  - {}", request.getHeaders().getContentType());
        log.info("req. header accept - {}", request.getHeaders().getAccept());

        ResponseEntity<QuoteDTO> response = restClient.post().uri(quotesUrl)
                                            .contentType(contentType).accept(accept)
                                            .body(validQuote).retrieve()
                                            .toEntity(QuoteDTO.class);
        
        log.info("resp. status - {} - {}", response.getStatusCode(), HttpStatus.valueOf(response.getStatusCode().value()));
        log.info("resp. body - {}", response.getBody());
        log.info("resp. header Content Type- {}", response.getHeaders().getContentType());

        // then - service will accept the format we supplied
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
    void get_quote() {

        // given / arrange
        QuoteDTO existingQuote = quoteDTOFactory.make();
        ResponseEntity<QuoteDTO> quoteResponse = restClient.post().uri(quotesUrl).body(existingQuote).retrieve().toEntity(QuoteDTO.class);
        BDDAssertions.assertThat(quoteResponse.getStatusCode().is2xxSuccessful()).isTrue();

        int requestId = quoteResponse.getBody().getId();
        URI quoteUri = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId);
        RequestEntity<Void> request = RequestEntity.get(quoteUri).build();

        // when / act
        ResponseEntity<QuoteDTO> response = restClient.get().uri(quoteUri).retrieve().toEntity(QuoteDTO.class);

        // then / evaluate-assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(response.getBody()).isEqualTo(existingQuote.withId(requestId));

    }

    @ParameterizedTest
    @ValueSource(strings = {MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE})
    void get_quotes(String mediatypeString){
        // given / arrange
        MediaType mediaType = MediaType.valueOf(mediatypeString);
        Map<Integer,QuoteDTO> quotesMap = new HashMap<>();
        QuoteListDTO quotes = quoteDTOFactory.listBuilder().make(3,3);

        for (QuoteDTO quote : quotes.getQuotes()) {
            
            ResponseEntity<QuoteDTO> response = restClient.post().uri(quotesUrl).contentType(mediaType).body(quote).retrieve().toEntity(QuoteDTO.class);
            BDDAssertions.then(response.getStatusCode().is2xxSuccessful()).isTrue();
            QuoteDTO addedQuote = response.getBody();
            quotesMap.put(addedQuote.getId(), addedQuote);
        }
        BDDAssertions.then(quotesMap).isNotEmpty();

        URI quoteUri = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri();
        URI quoteUriWithOffset = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH)
                                                    .queryParam("offset", 2)
                                                    .queryParam("limit", 15)
                                                    .build().toUri();

        // when / act
        ResponseEntity<QuoteListDTO> response = restClient.get().uri(quoteUri)
                                                .accept(mediaType).retrieve().toEntity(QuoteListDTO.class);
        ResponseEntity<QuoteListDTO> responseWithOffset = restClient.get().uri(quoteUriWithOffset)
                                                            .accept(mediaType).retrieve().toEntity(QuoteListDTO.class);
        
        // then / evaluate-assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(responseWithOffset.getStatusCode()).isEqualTo(HttpStatus.OK);
        QuoteListDTO quotePage = response.getBody();
        QuoteListDTO quotePageWithOffset = responseWithOffset.getBody();

        BDDAssertions.then(quotePage.getOffset()).isEqualTo(0);
        BDDAssertions.then(quotePage.getLimit()).isEqualTo(0);
        BDDAssertions.then(quotePage.getCount()).isEqualTo(quotesMap.size());

        BDDAssertions.then(quotePageWithOffset.getOffset()).isEqualTo(2);
        BDDAssertions.then(quotePageWithOffset.getLimit()).isEqualTo(15);
        BDDAssertions.then(quotePageWithOffset.getCount()).isEqualTo(quotesMap.size()-2);

        for (QuoteDTO q: quotePage.getQuotes()) {
            BDDAssertions.then(quotesMap.remove(q.getId())).isNotNull();
        }
        BDDAssertions.then(quotesMap).isEmpty();

    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    void add_valid_quote(MediaType contentType, MediaType accept) {
        // given / arrange - a valid quote
        QuoteDTO validQuote = quoteDTOFactory.make();

        // when / act 
        ResponseEntity<QuoteDTO> response = restClient.post().uri(quotesUrl).accept(accept)
                                                            .contentType(contentType)
                                                            .body(validQuote)
                                                            .retrieve().toEntity(QuoteDTO.class);
                                                            
        
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
        ResponseEntity<QuoteDTO> response = restClient.post().uri(quotesUrl).accept(MediaType.APPLICATION_JSON)
                                                            .contentType(MediaType.APPLICATION_XML)
                                                            .body(existingQuote)
                                                            .retrieve().toEntity(QuoteDTO.class);
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
        ResponseEntity<Void> response = restClient.put().uri(updateUri).contentType(MediaType.APPLICATION_JSON)
                                                        .body(updatedQuote)
                                                        .retrieve().toEntity(Void.class);
                                                        
        // then - evaluate / assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        URI getUri = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId);
        ResponseEntity<QuoteDTO> getUpdatedQuote = restClient.get().uri(getUri).retrieve().toEntity(QuoteDTO.class);

        BDDAssertions.then(getUpdatedQuote.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getUpdatedQuote.getBody()).isEqualTo(updatedQuote);
        BDDAssertions.then(getUpdatedQuote.getBody()).isNotEqualTo(existingQuote);

    }

    @Test
    void get_quote_1() {
        // given / arrange
        QuoteDTO existingQuote = quoteDTOFactory.make();
        ResponseEntity<QuoteDTO> response = restClient.post()
                                    .uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri())
                                    .body(existingQuote)
                                    .retrieve().toEntity(QuoteDTO.class);
                                    
        
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        int requestId = response.getBody().getId();

        // when / act

        ResponseEntity<QuoteDTO> getQuote = restClient.get()
                                                .uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId))
                                                .retrieve().toEntity(QuoteDTO.class);

        // then
        BDDAssertions.then(getQuote.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getQuote.getBody()).isEqualTo(existingQuote.withId(requestId));
    }

        protected List<QuoteDTO> given_many_quotes(int count) {
        List<QuoteDTO> quotes = new ArrayList<>(count);
        for (QuoteDTO quoteDTO : quoteDTOFactory.listBuilder().quotes(count,count)) {
                ResponseEntity<QuoteDTO> response = restClient.post()
                                                    .uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri())
                                                    .body(quoteDTO)
                                                    .retrieve().toEntity(QuoteDTO.class);

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
        ResponseEntity<QuoteDTO> resp = restClient.get()
                                        .uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.RANDOM_QUOTE_PATH).build().toUri())
                                        .retrieve().toEntity(QuoteDTO.class);
                                                
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
        BDDAssertions.assertThat(restClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId))
                                    .retrieve().toEntity(QuoteDTO.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        // when requested to remove
        ResponseEntity<Void> resp = restClient.delete()
                                    .uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId))
                                    .retrieve().toEntity(Void.class);
        // then

        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
                                            () -> restClient.get().uri(UriComponentsBuilder.fromUri(baseUrl)
                                            .path(QuotesAPI.QUOTE_PATH).build(requestId)).retrieve().toEntity(QuoteDTO.class) ,
                                             RestClientResponseException.class) ;  
        BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

        @Test
        void remove_all_quotes() {
        // given  / arrange
        List<QuoteDTO> quotes = given_many_quotes(3);

        // when / act- requested to remove all quotes
        ResponseEntity<Void> resp = restClient.delete()
                                    .uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri())
                                    .retrieve().toEntity(Void.class);
                                    
        // then
        BDDAssertions.then(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        for (QuoteDTO quoteDTO : quotes) {
               RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
                        ()-> restClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH)
                        .build(quoteDTO.getId())).retrieve().toEntity(QuoteDTO.class),
                         RestClientResponseException.class) ;
                BDDAssertions.assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }

    @Test
    void remove_unknown_quote() {
        // given 
        int requestId = 13;

        // when - requested to remove will not report that does not exist i.e. idempotent
        ResponseEntity<Void> resp = restClient.delete()
                                  .uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId))
                                  .retrieve().toEntity(Void.class);
                                    
        // then
        BDDAssertions.then(resp.getStatusCode().is2xxSuccessful()).isTrue();

    }

    @Test
    void get_random_quote_no_quotes(){
        // given
        BDDAssertions.assertThat(restClient.delete().uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH)
                        .build().toUri()).retrieve().toEntity(Void.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // then
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
            () -> restClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.RANDOM_QUOTE_PATH).build().toUri())
                        .retrieve().toEntity(QuoteDTO.class)
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
            () -> restClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(unknownId))
                        .retrieve().toEntity(QuoteDTO.class)        
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
            ()-> restClient.put().uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(unknownId))
                    .body(updateQuote).retrieve().toEntity(Void.class)
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
        ResponseEntity<QuoteDTO> resp = restClient.get().uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(knownId))
                    .retrieve().toEntity(QuoteDTO.class);
        log.info("resp - {}", resp.getBody());

        // when
        RestClientResponseException ex = BDDAssertions.catchThrowableOfType(
         () -> restClient.put().uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(knownId))
                    .body(badQuoteMissingText).retrieve().toEntity(Void.class)
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
        () -> restClient.post()
                    .uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri())
                    .body(badQuoteMissingText)
                    .retrieve().toEntity(QuoteDTO.class)
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
            ()-> restClient.get().uri(url)
                        .retrieve().toEntity(QuoteListDTO.class)
     
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
        ResponseEntity<QuoteListDTO> response = restClient.get().uri(url).accept(MediaType.APPLICATION_JSON)
                                                .retrieve().toEntity(QuoteListDTO.class);
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

        ResponseEntity<QuoteListDTO> response = restClient.get().uri(url)
                                                .retrieve().toEntity(QuoteListDTO.class);

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
