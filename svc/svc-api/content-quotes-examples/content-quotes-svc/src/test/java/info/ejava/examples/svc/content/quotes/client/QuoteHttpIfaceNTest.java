package info.ejava.examples.svc.content.quotes.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientResponseException;

import info.ejava.examples.content.quotes.QuotesApplication;
import info.ejava.examples.content.quotes.client.QuoteHttpIfaceImpl;
import info.ejava.examples.content.quotes.dto.MessageDTO;
import info.ejava.examples.content.quotes.dto.QuoteDTO;

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
        
        //ResponseEntity<QuoteDTO> respons = quoteHttpIfaceAPIRestTemplate.getQuote(1);
        ResponseEntity<QuoteDTO> respons = quoteHttpIfaceAPIRestClient.getQuote(1);
        log.info("Quote using httpIface - {}", respons);
        //ResponseEntity<Void> resp =  quoteHttpIfaceAPIRestTemplate.deleteAllQuotes();
        //ResponseEntity<Void> resp =  quoteHttpIfaceAPIRestClient.deleteAllQuotes();


    }

    @ParameterizedTest
    @MethodSource("mediaTypes")
    void add_valid_quote_for_type(MediaType contentType, MediaType accept) {
        // given a valid quote
        QuoteDTO validQuote  =  quoteDTOFactory.make();

        // when making request using different request and accept payload types
        
        // when - making request using different request and accept payload types
        //log.info("contentType- {}, accept - {}", contentType, accept);
        log.info("validQuote - {}", validQuote);
        ResponseEntity<QuoteDTO> response =null ;
        if(contentType.toString().equals(MediaType.APPLICATION_JSON_VALUE)){

            response = quoteHttpIfaceAPIRestClient.createQuoteJson(validQuote);
            
        }
         
        if(contentType.toString().equals(MediaType.APPLICATION_XML_VALUE)){

            response = quoteHttpIfaceAPIRestClient.createQuoteXml(validQuote);
            
        }

        log.info("response -{}", response);
        // log.info("resp. status - {} - {}", response.getStatusCode(), HttpStatus.valueOf(response.getStatusCode().value()));
        // log.info("resp. body - {}", response.getBody());
        // log.info("resp. header Content Type- {}", response.getHeaders().getContentType());

    }
     
}