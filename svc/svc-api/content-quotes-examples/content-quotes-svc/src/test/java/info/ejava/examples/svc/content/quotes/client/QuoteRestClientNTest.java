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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.examples.content.quotes.QuotesApplication;
import info.ejava.examples.content.quotes.api.QuotesAPI;
import info.ejava.examples.content.quotes.dto.MessageDTO;
import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.util.JsonUtil;
import info.ejava.examples.content.quotes.util.QuoteDTOFactory;
import lombok.extern.slf4j.Slf4j;

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
        restClient.delete().uri(quotesUrl).retrieve();
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

}
