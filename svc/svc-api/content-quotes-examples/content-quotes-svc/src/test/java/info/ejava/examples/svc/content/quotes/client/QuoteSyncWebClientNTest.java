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

@SpringBootTest(classes = {ClientTestConfiguration.class, QuotesApplication.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = "test=true")
@ActiveProfiles("test")
@Tag("springboot")
@Slf4j
public class QuoteSyncWebClientNTest {
    
    @Autowired
    private QuoteDTOFactory quoteDTOFactory;

    @Autowired
    private WebClient webClient;

    @Autowired
    private URI baseUrl;
    @Autowired
    private URI quotesUrl;

    private static final MediaType[] MEDIA_TYPES = new MediaType[] {
        MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML
    };

    @BeforeEach
    public void setUp() {
        log.info("clearing all gestures");
        ResponseEntity<Void> response = webClient.delete().uri(quotesUrl).retrieve().toEntity(Void.class).block();
        log.info("delete all quotes response status - {}", response.getStatusCode());
    }

    @AfterEach
    public void cleanUp() {
        //cut down on noise
        //restTemplate.delete(quotesUrl);
    }

    public static Stream<Arguments> mediaTypes() {
        List<Arguments> params = new ArrayList<>();
        for (MediaType contentType : MEDIA_TYPES) {
            for (MediaType acceptType : MEDIA_TYPES) {
                params.add(Arguments.of(contentType, acceptType));
            }
        }
        return params.stream();
    }

    public MessageDTO getErrorResponse(WebClientResponseException  ex){
        final String contentTypeValue = ex.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
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
        // given -a valid quote
        QuoteDTO validQuote = quoteDTOFactory.make();

        // when - making a request using different and accept  payload types
        RequestEntity<QuoteDTO> request = RequestEntity.post(quotesUrl)
                                                .contentType(contentType)
                                                .accept(accept)
                                                .body(validQuote);

        log.info("req. body - {}", request.getBody());
        log.info("req. method - {}", request.getMethod());
        log.info("req. header Content Type  - {}", request.getHeaders().getContentType());
        log.info("req. header accept - {}", request.getHeaders().getAccept());

        ResponseEntity<QuoteDTO> response = webClient.post().uri(quotesUrl)
                                                .contentType(contentType)
                                                .accept(accept)
                                                .bodyValue(validQuote).retrieve()
                                                .toEntity(QuoteDTO.class)
                                                .block();

        log.info("resp. status - {}", response.getStatusCode());
        log.info("resp. body - {}", response.getBody());
        log.info("resp. header Content Type- {}", response.getHeaders().getContentType());
        
        // then 
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
        // arrange / given - an existing quote
        QuoteDTO existingDto = quoteDTOFactory.make();
        ResponseEntity<QuoteDTO> quoteResponse = webClient.post().uri(quotesUrl).bodyValue(existingDto)
                                                            .retrieve().toEntity(QuoteDTO.class).block();
        BDDAssertions.assertThat(quoteResponse.getStatusCode().is2xxSuccessful()).isTrue();

        int requestedId = quoteResponse.getBody().getId();
        URI quoteUrl = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestedId);
        RequestEntity<Void> request = RequestEntity.get(quoteUrl).build();

        // when / act 
        ResponseEntity<QuoteDTO> response = webClient.get().uri(quoteUrl).retrieve().toEntity(QuoteDTO.class).block();

        // then - evaluate/assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(response.getBody()).isEqualTo(existingDto.withId(requestedId));
    }

    @ParameterizedTest
    @ValueSource(strings={MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE})
    void get_quotes(String mediatypeString){

        // given /arrange
        MediaType mediaType = MediaType.valueOf(mediatypeString);
        Map<Integer,QuoteDTO>  existingQuotes = new HashMap<>();

        QuoteListDTO quotes = quoteDTOFactory.listBuilder().make(3, 3);
        for (QuoteDTO  quoteDTO : quotes.getQuotes()) {
            
            ResponseEntity<QuoteDTO> response = webClient.post().uri(quotesUrl).accept(mediaType)
                                                        .bodyValue(quoteDTO).retrieve().toEntity(QuoteDTO.class).block();
            BDDAssertions.then(response.getStatusCode().is2xxSuccessful()).isTrue();
            QuoteDTO addedQuote = response.getBody();
            existingQuotes.put(addedQuote.getId(), addedQuote);
            
        }
        BDDAssertions.assertThat(existingQuotes).isNotEmpty();
        URI quotesUri = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri();
        URI quoteUriWithOffsetAndLimit = UriComponentsBuilder.fromUri(baseUrl)
                                                                .path(QuotesAPI.QUOTES_PATH)
                                                                .queryParam("offset", 1)
                                                                .queryParam("limit", 10)
                                                                .build().toUri();

        // when

        ResponseEntity<QuoteListDTO> response = webClient.get().uri(quotesUri).retrieve().toEntity(QuoteListDTO.class).block();

        ResponseEntity<QuoteListDTO> responseWithOffsetAndLimit = webClient.get().uri(quoteUriWithOffsetAndLimit)
                                                                            .retrieve().toEntity(QuoteListDTO.class).block();

        // then / evaluate - assert

        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(responseWithOffsetAndLimit.getStatusCode()).isEqualTo(HttpStatus.OK);

        QuoteListDTO pageWithoutOffset = response.getBody();
        QuoteListDTO pageWithOffset = responseWithOffsetAndLimit.getBody();

        BDDAssertions.then(pageWithoutOffset.getOffset()).isEqualTo(0);
        BDDAssertions.then(pageWithoutOffset.getLimit()).isEqualTo(0);
        BDDAssertions.then(pageWithOffset.getOffset()).isEqualTo(1);
        BDDAssertions.then(pageWithOffset.getLimit()).isEqualTo(10);

        BDDAssertions.then(pageWithoutOffset.getCount()).isEqualTo(existingQuotes.size());
        BDDAssertions.then(pageWithOffset.getCount()).isEqualTo(existingQuotes.size()-1);

        for(QuoteDTO q : pageWithoutOffset.getQuotes()){
            BDDAssertions.then(existingQuotes.remove(q.getId())).isNotNull();
        }

        BDDAssertions.then(existingQuotes).isEmpty();

    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    void add_valid_quote(MediaType contentType, MediaType accept) {
        // given / arrange - a valid quote
        QuoteDTO validQuote = quoteDTOFactory.make();

        // when / act 
        ResponseEntity<QuoteDTO> response = webClient.post().uri(quotesUrl).accept(accept)
                                                            .contentType(contentType)
                                                            .bodyValue(validQuote)
                                                            .retrieve().toEntity(QuoteDTO.class)
                                                            .block();
        
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
        ResponseEntity<QuoteDTO> response = webClient.post().uri(quotesUrl).accept(MediaType.APPLICATION_JSON)
                                                            .contentType(MediaType.APPLICATION_XML)
                                                            .bodyValue(existingQuote)
                                                            .retrieve().toEntity(QuoteDTO.class).block();
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
        ResponseEntity<Void> response = webClient.put().uri(updateUri).contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(updatedQuote)
                                                        .retrieve().toEntity(Void.class)
                                                        .block();

        // then - evaluate / assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        URI getUri = UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId);
        ResponseEntity<QuoteDTO> getUpdatedQuote = webClient.get().uri(getUri).retrieve().toEntity(QuoteDTO.class).block();

        BDDAssertions.then(getUpdatedQuote.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getUpdatedQuote.getBody()).isEqualTo(updatedQuote);
        BDDAssertions.then(getUpdatedQuote.getBody()).isNotEqualTo(existingQuote);

    }

    @Test
    void get_quote_1() {
        // given / arrange
        QuoteDTO existingQuote = quoteDTOFactory.make();
        ResponseEntity<QuoteDTO> response = webClient.post()
                                    .uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTES_PATH).build().toUri())
                                    .bodyValue(existingQuote)
                                    .retrieve().toEntity(QuoteDTO.class)
                                    .block();
        
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        int requestId = response.getBody().getId();

        // when / act

        ResponseEntity<QuoteDTO> getQuote = webClient.get()
                                                .uri(UriComponentsBuilder.fromUri(baseUrl).path(QuotesAPI.QUOTE_PATH).build(requestId))
                                                .retrieve().toEntity(QuoteDTO.class)
                                                .block();

        // then
        BDDAssertions.then(getQuote.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(getQuote.getBody()).isEqualTo(existingQuote.withId(requestId));
    }


}
