package info.ejava.examples.svc.content.quotes.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import info.ejava.examples.content.quotes.QuotesApplication;
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
    @Autowired @Qualifier("restTemplateHttpIface")
    private QuoteHttpIfaceAPI quoteHttpIfaceAPIRestTemplate;
    // @Autowired @Qualifier("webClientHttpIface")
    // private QuoteHttpIfaceAPI quoteHttpIfaceAPIWebClient;
    // @Autowired @Qualifier("restClientHttpIface")
    // private QuoteHttpIfaceAPI quoteHttpIfaceAPIRestClient;

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

    @BeforeEach
    public void init(){
        log.info("baseUrl - {}", baseUrl);
        log.info("clear / delete all quotes");
        quoteHttpIfaceAPIRestTemplate.deleteAllQuote();
    }

    
    @Test
    void add_valid_quote_for_type() {
        
    }
     
}