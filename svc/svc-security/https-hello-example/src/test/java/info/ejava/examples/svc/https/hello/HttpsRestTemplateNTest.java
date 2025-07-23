package info.ejava.examples.svc.https.hello;

import java.net.URI;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {ClientTestConfiguration.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"https","ntest"}) // for running test with http sceme just comment the @ActiveProfile
@Slf4j
public class HttpsRestTemplateNTest {
 
    @Autowired
    private RestTemplate authnUser;
    @Autowired
    private URI authnUrl;

    @BeforeEach
    public void  setUp() {
        log.info("baseUrl = {}", authnUrl);
    }

    @Test
    public void user_can_call_authenticated() {
        // given / arrange a URL to an endpoint that accepts only authenticated calls
        URI url  = UriComponentsBuilder.fromUri(authnUrl).queryParam("name", "jim").build().toUri();

        // when / act called with an authenticated identity
        ResponseEntity<String> response = authnUser.getForEntity(url, String.class);

        // then / verify / assert
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(response.getBody()).isEqualTo("hello, jim");
    }
}
