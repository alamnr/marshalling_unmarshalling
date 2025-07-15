package info.ejava.examples.svc.authn.authcfg;

import java.net.URI;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.examples.common.web.ServerConfig;

@SpringBootTest(classes = ClientTestConfiguration.class,
                webEnvironment =  SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = "test=true")
// test property triggers Swagger @Configuration and anything else not suitable during testing to disable
public class AuthnRestClientNTest {

    @Autowired @Qualifier("anonymousUserRestClient")
    private RestClient anonymousUserClient;

    @Autowired @Qualifier("authnUserRestClient")
    private RestClient authnUserClient;

    private URI baseUrl;
    private URI anonymousUrl;
    private URI authnUrl;

    @BeforeEach
    public void setup(@LocalServerPort int port){
        ServerConfig serverConfig = new ServerConfig().withPort(port).build();
        baseUrl  = serverConfig.getBaseUrl();
        anonymousUrl = UriComponentsBuilder.fromUri(baseUrl).path("/api/anonymous/hello").build().toUri();
        authnUrl = UriComponentsBuilder.fromUri(baseUrl).path("/api/authn/hello").build().toUri();
    }

    @Test
    void anonymous_can_access_static_content(){
        // given
        URI url = UriComponentsBuilder.fromUri(baseUrl).path("/content/hello_static.txt").build().toUri();
        // when
        ResponseEntity<String> response = anonymousUserClient.get().uri(url) .retrieve().toEntity(String.class);

        // then
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(response.getBody()).isEqualTo("Hello, static file");

    }

    @Test
    void anonymous_can_call_unauthenticated(){
        // given a url that accept anonymous / unauthenticated call
        URI url = UriComponentsBuilder.fromUri(anonymousUrl).queryParam("name", "jim").build().toUri();

        // when 
        ResponseEntity<String> response = anonymousUserClient.get().uri(url).retrieve().toEntity(String.class);

        // then
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(response.getBody()).isEqualTo("hello jim :caller = (null)");
    }

    @Test
    void anonymous_cannot_call_authenticated(){
        // given authenticated url that can not be accessed with anonymous/unauthenticated user
        URI url = UriComponentsBuilder.fromUri(authnUrl).queryParam("name", "jim").build().toUri();

        // when
        HttpClientErrorException ex = BDDAssertions.catchThrowableOfType(
                                        ()->anonymousUserClient.get().uri(url).retrieve().toEntity(String.class), 
                                        HttpClientErrorException.Unauthorized.class);
        // then
        BDDAssertions.then(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        
    }
    
    @Test
    void user_can_call_authenticated(){
        // given authenticated url thet can be called with authenticated user
        URI url = UriComponentsBuilder.fromUri(authnUrl).queryParam("name", "jim").build().toUri();

        // when 
        ResponseEntity<String> response = authnUserClient.get().uri(url).retrieve().toEntity(String.class);

        // then
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(response.getBody()).isEqualTo("hello jim :caller = user");
    }
}
