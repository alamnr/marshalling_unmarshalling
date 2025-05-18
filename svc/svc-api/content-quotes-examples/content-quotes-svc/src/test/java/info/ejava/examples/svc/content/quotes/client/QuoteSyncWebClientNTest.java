package info.ejava.examples.svc.content.quotes.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.examples.content.quotes.QuotesApplication;
import info.ejava.examples.content.quotes.api.QuotesAPI;
import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.util.QuoteDTOFactory;
import lombok.extern.slf4j.Slf4j;

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
        webClient.delete().uri(quotesUrl).retrieve();
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


}
