package info.ejava.examples.common.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class is used to represent a configuration of an external
 * connection that defaults to http://localhost:8080
 */
@Data  // getter , setter
@With  // return an object of ServerConfig with new value of properties
@NoArgsConstructor 
@AllArgsConstructor
public class ServerConfig {
    private String scheme;
    private String host;
    private int port;
    private URI baseUrl;
    private String trustStore;
    private char[] trustStorePassword;

    @PostConstruct
    public ServerConfig build() {
        if (baseUrl==null) {
            scheme = scheme == null ? "http" : scheme.toLowerCase();
            host = host == null ? "localhost" : host;
            if (port == 0) {
                port = scheme.equals("http") ? 8080 : 8443;
            }
            baseUrl = buildBaseURI();
        }
        scheme = baseUrl.getScheme();
        host = baseUrl.getHost();
        port = baseUrl.getPort();
        return this;
    }

    private URI buildBaseURI() {
        String uriString = String.format("%s://%s:%d", scheme, host, port);
        try {
            return new URI(uriString);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid URI:" + uriString,e);
        }
    }

    public URI getBaseUrl() {
        if (baseUrl==null) {
            build();
        }
        return baseUrl;
    }

    public boolean isHttps() {
        return "https".equalsIgnoreCase(scheme);
    }
}
