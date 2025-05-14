package info.ejava.examples.common.web;

import java.net.URI;
import java.net.URISyntaxException;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)

public class ServerConfigTest {


    @Test
    void can_format_default() throws URISyntaxException {

        // given / arrange
        ServerConfig cfg = new ServerConfig();
        // when / act
        cfg = cfg.build();

        // then / evaluate/ assert
        BDDAssertions.then(cfg.getBaseUrl()).isEqualTo(new URI("http://localhost:8080"));
    }

     @Test
    void can_format_port() throws URISyntaxException {

        // given
        ServerConfig cfg = new ServerConfig().withPort(1234);
        // when 
        cfg = cfg.build();
        // then 
        BDDAssertions.then(cfg.getPort()).isEqualTo(1234);
        BDDAssertions.then(cfg.getBaseUrl()).isEqualTo(new URI("http://localhost:1234"));
    }

    @Test
    void can_format_https_default() throws URISyntaxException {

        // given 
        ServerConfig cfg = new ServerConfig().withScheme("https").withHost("ahost");
        // when
        cfg = cfg.build();
        // then
        BDDAssertions.then(cfg.getBaseUrl()).isEqualTo(new URI("https://ahost:8443"));
    }

}
