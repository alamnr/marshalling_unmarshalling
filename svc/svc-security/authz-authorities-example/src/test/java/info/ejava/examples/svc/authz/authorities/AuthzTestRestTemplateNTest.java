package info.ejava.examples.svc.authz.authorities;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.test.context.ActiveProfiles;

import info.ejava.examples.common.web.ServerConfig;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest(classes = {ClientTestConfiguration.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"https","ntest"})
@Slf4j
public class AuthzTestRestTemplateNTest {
    @Autowired @Qualifier("client")
    private TestRestTemplate client; //automatically tracks @LocalPort
    @Autowired
    private ServerConfig serverConfig;

    private static String whoAmIURI =   "/api/whoAmI";
    private static String pathsURI = "/api/authorities/paths";
    private static String securedURI = "/api/authorities/secured";
    private static String jsr250URI = "/api/authorities/jsr250";
    private static String expressionsURI = "/api/authorities/expressions";

    @ParameterizedTest
    @MethodSource("user_auths")
    void has_roles(String username, List<String> roles) throws URISyntaxException {
        //given 
        client = client.withBasicAuth(username, "password");
        String expectedResponse = String.format("[%s, %s]", username, roles);
        RequestEntity request = RequestEntity.get(serverConfig.getBaseUrl().toString() + whoAmIURI).build();
        //when
        ResponseEntity<String> response = client.exchange(request, String.class);
        //then
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isEqualTo(expectedResponse);
    }

    @Nested
    class paths {
        static Stream<Arguments> customers() { return user_auths().filter(data->{
                List<String> authorities = (List<String>) data.get()[1];
                return authorities.contains("ROLE_CUSTOMER");
            });
        };

        @ParameterizedTest
        @MethodSource("endpoint_rqmt_user_auth")
        void enforce_access(String endpoint, List<String> requiredAuthority, String username, List<String> auths) {
            //given
            client = client.withBasicAuth(username, "password");
            boolean canAccess = requiredAuthority.stream().filter(a->auths.contains(a)).findFirst().isPresent();
            RequestEntity request = RequestEntity.get(serverConfig.getBaseUrl().toString() + pathsURI + "/" + endpoint).build();
            //when
            ResponseEntity<String> response = client.exchange(request, String.class);
            //then
            then(response.getStatusCode()).isEqualTo(canAccess ? HttpStatus.OK : HttpStatus.FORBIDDEN);
        }

        @ParameterizedTest
        @MethodSource("customers")
        void can_protect_permissions(String username, List<String> auths) {
            //given
            client = client.withBasicAuth(username, "password");
            boolean canAccess = auths.contains("PRICE_CHECK") || auths.contains("ROLE_ADMIN") || auths.contains("ROLE_CLERK");
            RequestEntity request = RequestEntity.get(serverConfig.getBaseUrl().toString() + pathsURI + "/price").build();
            //when
            ResponseEntity<String> response = client.exchange(request, String.class);
            //then
            then(response.getStatusCode()).isEqualTo(canAccess ? HttpStatus.OK : HttpStatus.FORBIDDEN);
        }

        static Stream<Arguments> endpoint_rqmt_user_auth() {
            Stream<Arguments> admin = user_auths()
                    .map(user -> Arguments.of("admin", List.of("ROLE_ADMIN"), user.get()[0], user.get()[1]));
            Stream<Arguments> clerk = user_auths()
                    .map(user -> Arguments.of("clerk", List.of("ROLE_ADMIN", "ROLE_CLERK"), user.get()[0], user.get()[1]));
            Stream<Arguments> customer = user_auths()
                    .map(user -> Arguments.of("customer", List.of("ROLE_CUSTOMER"), user.get()[0], user.get()[1]));
            Stream<Arguments> price = user_auths() //.hasAnyAuthority("PRICE_CHECK", "ROLE_ADMIN", "ROLE_CLERK"));
                    .map(user -> Arguments.of("price", List.of("ROLE_ADMIN", "ROLE_CLERK","PRICE_CHECK"), user.get()[0], user.get()[1]));
            return Stream.concat(Stream.concat(Stream.concat(admin, clerk), customer), price);
        }
    }

    @Nested
    class secured_methods {
        static Stream<Arguments> customers() { return user_auths().filter(data->{
                List<String> authorities = (List<String>) data.get()[1];
                return authorities.contains("ROLE_CUSTOMER");
            });
        };

        @ParameterizedTest
        @MethodSource("endpoint_rqmt_user_auth")
        void enforced_access(String endpoint, List<String> requiredAuthority, String username, List<String> auths) {
            //given
            client = client.withBasicAuth(username, "password");
            boolean canAccess = requiredAuthority.stream().filter(a->auths.contains(a)).findFirst().isPresent();
            RequestEntity request = RequestEntity.get(serverConfig.getBaseUrl().toString() + securedURI + "/" + endpoint).build();
            //when
            ResponseEntity<String> response = client.exchange(request, String.class);
            //then
            then(response.getStatusCode()).isEqualTo(canAccess ? HttpStatus.OK : HttpStatus.FORBIDDEN);
        }

        @ParameterizedTest
        @MethodSource("customers")
        void can_protect_permissions(String username, List<String> auths) {
            //given
            client = client.withBasicAuth(username, "password");
            boolean canAccess = auths.contains("PRICE_CHECK") || auths.contains("ROLE_ADMIN") || auths.contains("ROLE_CLERK");
            RequestEntity request = RequestEntity.get(serverConfig.getBaseUrl().toString() + securedURI + "/price").build();
            //when
            ResponseEntity<String> response = client.exchange(request, String.class);
            //then
            then(response.getStatusCode()).isEqualTo(canAccess ? HttpStatus.OK : HttpStatus.FORBIDDEN);
        }
        static Stream<Arguments> endpoint_rqmt_user_auth() {
            Stream<Arguments> admin = user_auths()
                    .map(user -> Arguments.of("admin", List.of("ROLE_ADMIN"), user.get()[0], user.get()[1]));
            Stream<Arguments> clerk = user_auths() //@Secured does not support role inheritance
                    .map(user -> Arguments.of("clerk", List.of("ROLE_CLERK"), user.get()[0], user.get()[1]));
            Stream<Arguments> customer = user_auths()
                    .map(user -> Arguments.of("customer", List.of("ROLE_CUSTOMER"), user.get()[0], user.get()[1]));
            Stream<Arguments> price = user_auths() //@Secured now supports non-ROLEs in Spring Security 6.x
                    .map(user -> Arguments.of("price", List.of("ROLE_ADMIN", "ROLE_CLERK","PRICE_CHECK"), user.get()[0], user.get()[1]));
            return Stream.concat(Stream.concat(Stream.concat(admin, clerk), customer), price);
        }
    }

    @Nested
    class jsr250_methods {
        static Stream<Arguments> customers() { return user_auths().filter(data->{
                List<String> authorities = (List<String>) data.get()[1];
                return authorities.contains("ROLE_CUSTOMER");
            });
        };
        @ParameterizedTest
        @MethodSource("endpoint_rqmt_user_auth")
        void enforced_access(String endpoint, List<String> requiredAuthority, String username, List<String> auths) {
            //given
            client = client.withBasicAuth(username, "password");
            boolean canAccess = requiredAuthority.stream().filter(a->auths.contains(a)).findFirst().isPresent();
            RequestEntity request = RequestEntity.get(serverConfig.getBaseUrl().toString() + jsr250URI + "/" + endpoint).build();
            //when
            ResponseEntity<String> response = client.exchange(request, String.class);
            //then
            then(response.getStatusCode()).isEqualTo(canAccess ? HttpStatus.OK : HttpStatus.FORBIDDEN);
        }

        @ParameterizedTest
        @MethodSource("customers") //JSR-250 does not support permissions, only roles
        void cannot_protect_permissions(String username, List<String> auths) {
            //given
            client = client.withBasicAuth(username, "password");
            boolean canAccess = auths.contains("PRICE_CHECK") || auths.contains("ROLE_ADMIN") || auths.contains("ROLE_CLERK");
            RequestEntity request = RequestEntity.get(serverConfig.getBaseUrl().toString() + jsr250URI + "/price").build();
            //when
            ResponseEntity<String> response = client.exchange(request, String.class);
            //then
//            then(response.getStatusCode()).isEqualTo(canAccess ? HttpStatus.OK : HttpStatus.FORBIDDEN);
            then(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
        static Stream<Arguments> endpoint_rqmt_user_auth() {
            Stream<Arguments> admin = user_auths()
                    .map(user -> Arguments.of("admin", List.of("ROLE_ADMIN"), user.get()[0], user.get()[1]));
            Stream<Arguments> clerk = user_auths() //@Secured does not support role inheritance
                    .map(user -> Arguments.of("clerk", List.of("ROLE_CLERK"), user.get()[0], user.get()[1]));
            Stream<Arguments> customer = user_auths()
                    .map(user -> Arguments.of("customer", List.of("ROLE_CUSTOMER"), user.get()[0], user.get()[1]));
            Stream<Arguments> price = user_auths() //jsr250 does not support non-ROLE
                    .map(user -> Arguments.of("price", List.of("ROLE_ADMIN", "ROLE_CLERK"/*,"PRICE_CHECK"*/), user.get()[0], user.get()[1]));
            return Stream.concat(Stream.concat(Stream.concat(admin, clerk), customer), price);
        }
    }


    @Nested
    class expression_methods {
        static Stream<Arguments> customers() { return user_auths().filter(data->{
                List<String> authorities = (List<String>) data.get()[1];
                return authorities.contains("ROLE_CUSTOMER");
            });
        };

        @ParameterizedTest
        @MethodSource("endpoint_rqmt_user_auth")
        void enforced_access(String endpoint, List<String> requiredAuthority, String username, List<String> auths) {
            //given
            client = client.withBasicAuth(username, "password");
            boolean canAccess = requiredAuthority.stream().filter(a->auths.contains(a)).count()>0;
            RequestEntity request = RequestEntity.get(serverConfig.getBaseUrl().toString() + expressionsURI + "/" + endpoint).build();
            //when
            ResponseEntity<String> response = client.exchange(request, String.class);
            //then
            then(response.getStatusCode()).isEqualTo(canAccess ? HttpStatus.OK : HttpStatus.FORBIDDEN);
        }

        @ParameterizedTest
        @MethodSource("customers")
        void can_protect_permissions(String username, List<String> auths) {
            //given
            client = client.withBasicAuth(username, "password");
            boolean canAccess = auths.contains("PRICE_CHECK") || auths.contains("ROLE_ADMIN") || auths.contains("ROLE_CLERK");
            RequestEntity request = RequestEntity.get(serverConfig.getBaseUrl().toString() + expressionsURI + "/price").build();
            //when
            ResponseEntity<String> response = client.exchange(request, String.class);
            //then
            then(response.getStatusCode()).isEqualTo(canAccess ? HttpStatus.OK : HttpStatus.FORBIDDEN);
        }
        static Stream<Arguments> endpoint_rqmt_user_auth() {
            Stream<Arguments> admin = user_auths()
                    .map(user -> Arguments.of("admin", List.of("ROLE_ADMIN"), user.get()[0], user.get()[1]));
            Stream<Arguments> clerk = user_auths() //@Secured does not support role inheritance
                    .map(user -> Arguments.of("clerk", List.of("ROLE_CLERK"), user.get()[0], user.get()[1]));
            Stream<Arguments> customer = user_auths()
                    .map(user -> Arguments.of("customer", List.of("ROLE_CUSTOMER"), user.get()[0], user.get()[1]));
            Stream<Arguments> price = user_auths() //expressions do support non-ROLE authorities
                    .map(user -> Arguments.of("price", List.of("ROLE_ADMIN", "ROLE_CLERK","PRICE_CHECK"), user.get()[0], user.get()[1]));
            return Stream.concat(Stream.concat(Stream.concat(admin, clerk), customer), price);
        }
    }

    /**
     * The "roleInheritance" profile is not active during this test case, so this
     * test demonstrates that an ADMIN is not a CLERK or CUSTOMER -- this cannot
     * call price customer.
     */
    @ParameterizedTest
    @MethodSource("user_auths")
    void admin_is_not_customer(String username, List<String> auths) {
        //given
        client = client.withBasicAuth(username, "password");
        boolean canAccess = auths.contains("ROLE_CUSTOMER");
        RequestEntity request = RequestEntity.get(serverConfig.getBaseUrl().toString() + pathsURI + "/customer").build();
        //when
        ResponseEntity<String> response = client.exchange(request, String.class);
        //then
        then(response.getStatusCode()).isEqualTo(canAccess ? HttpStatus.OK : HttpStatus.FORBIDDEN);
    }

    static Stream<Arguments> user_auths() {
        return Stream.of(
                Arguments.of("sam", List.of("ROLE_ADMIN")),
                Arguments.of("rebecca", List.of("ROLE_ADMIN")),
                Arguments.of("woody", List.of("ROLE_CLERK")),
                Arguments.of("carla", List.of("ROLE_CLERK")),
                Arguments.of("cliff", List.of("ROLE_CUSTOMER")),
                Arguments.of("norm", List.of("ROLE_CUSTOMER")),
                Arguments.of("frasier", List.of("PRICE_CHECK", "ROLE_CUSTOMER"))
        );
    }
}
