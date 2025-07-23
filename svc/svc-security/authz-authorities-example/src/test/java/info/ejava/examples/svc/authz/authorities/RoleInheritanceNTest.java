package info.ejava.examples.svc.authz.authorities;

import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import info.ejava.examples.common.web.ServerConfig;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {ClientTestConfiguration.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("roleInheritance")  // without https 
@ActiveProfiles({"roleInheritance","https","ntest"}) // with https
@Slf4j
public class RoleInheritanceNTest {

    @Autowired @Qualifier("client")
    private TestRestTemplate client; //automatically tracks @LocalPort
    @Autowired
    private ServerConfig serverConfig;


    void can_access_customer(String baseUri, String username, List<String> auths){
        // given 
        client = client.withBasicAuth(username, "password");
        boolean canAccess =  true;  // auths.contains("ROLE_ADMIN") , auths.contains("ROLE_CLERK") , auths.aontains("ROLE_CUSTOMER")
        RequestEntity request = RequestEntity.get(baseUri+"/customer").build();
        // when
        ResponseEntity<String> response = client.exchange(request, String.class);
        // then
        BDDAssertions.then(response.getStatusCode()).isEqualTo(canAccess ? HttpStatus.OK : HttpStatus.FORBIDDEN);
    }

    void cannot_access_customer(String baseUri, String username, List<String> auths) {
        client = client.withBasicAuth(username, "password");
        boolean canAccess = false; // auths.contain("ROLE_CUSTOMER")
        RequestEntity request = RequestEntity.get(baseUri+"/customer").build();
        // when
        ResponseEntity<String> response = client.exchange(request, String.class);
        // then
        BDDAssertions.then(response.getStatusCode()).isEqualTo(canAccess ? HttpStatus.OK : HttpStatus.FORBIDDEN);
    }

    void can_access_clerk(String baseUri, String username, List<String> auths){
        // given 
        client = client.withBasicAuth(username, "password");
        boolean canAccess = true; // auths.contains("ROLE_ADMIN") || auths.contains("ROLE_CLERK")
        RequestEntity request = RequestEntity.get(baseUri+"/clerk").build();

        // when 
        ResponseEntity<String> response = client.exchange(request, String.class);
        // then
        BDDAssertions.then(response.getStatusCode()).isEqualTo(canAccess ? HttpStatus.OK : HttpStatus.FORBIDDEN);

    }

    void cannot_access_clerk(String baseUri, String username, List<String> auths){
        // given 
        client = client.withBasicAuth(username, "password");
        boolean canAccess = false; // auths.contains("ROLE_CLERK")
        RequestEntity request  = RequestEntity.get(baseUri+"/clerk").build();
        // when
        ResponseEntity<String> response = client.exchange(request, String.class);
        // then
        BDDAssertions.then(response.getStatusCode()).isEqualTo(canAccess? HttpStatus.OK : HttpStatus.FORBIDDEN);
    }

    // allows @MethodSource to avoid expression with a full package name
    class MethodSources {
        static Stream<Arguments> user_auths() {
            return AuthzTestRestTemplateNTest.user_auths();
        }
        static Stream<Arguments> admin_clerk_auths() {
            return user_auths().filter(data -> {
                List<String> authoeities = ((List<String>)data.get()[1]);
                return authoeities.contains("ROLE_ADMIN") || authoeities.contains("ROLE_CLERK");
            });

        }
        static Stream<Arguments> non_clerk_auths( ) {
            return user_auths().filter(data -> {
                List<String> authorities = (List<String>)data.get()[1];
                return !authorities.contains("ROLE_ADMIN") && !authorities.contains("ROLE_CLERK");
            });
        }
    }

    @Nested 
    class path_constraint_test  extends MethodSources {
        final String uri = serverConfig.getBaseUrl().toString() + "/api/authorities/paths";

        @ParameterizedTest
        @MethodSource("user_auths")
        void admin_clerk_can_inherit_from_customer (String username, List<String> auths){
            can_access_customer(uri, username, auths);
        }

        @ParameterizedTest
        @MethodSource("admin_clerk_auths")
        void admin_can_inherit_from_clerk(String username, List<String> auths) {
            can_access_clerk(uri, username, auths);
        }

        @ParameterizedTest
        @MethodSource("non_clerk_auths")
        void customer_cannot_caccess_clerk(String username, List<String> auths){
            cannot_access_clerk(uri, username, auths);
        }
    }

    @Nested
    // @Secured now supports RoleHierArchy (verified in 3.3.2)
    // https://github.com/spring-projects/spring-security/issues/12783
    class secured extends MethodSources {
        final String uri = serverConfig.getBaseUrl().toString() + "/api/authorities/secured";

        @ParameterizedTest
        @MethodSource("user_auths")
        void admin_clerk_can_inherit_from_customer(String username, List<String> auths){
            can_access_customer(uri, username, auths);
        }

        @ParameterizedTest
        @MethodSource("admin_clerk_auths")
        void admin_can_inherit_from_clerk(String username, List<String> auths){
            can_access_clerk(uri, username, auths);
        }

        @ParameterizedTest
        @MethodSource("non_clerk_auths")
        void customer_cannot_access_clerk(String username, List<String> auths) {
            cannot_access_clerk(uri, username, auths);
        }
    }

    @Nested
    //Jsr250 now supports RoleHierarchy (verified in 3.3.2)
    //https://github.com/spring-projects/spring-security/issues/12782
    class jsr250 extends MethodSources {
        final String uri = serverConfig.getBaseUrl().toString() + "/api/authorities/jsr250";

        @ParameterizedTest
        @MethodSource("user_auths")
        void admin_clerk_can_inherit_from_customer(String username, List<String> auths) {
            can_access_customer(uri, username, auths);
        }
        @ParameterizedTest
        @MethodSource("admin_clerk_auths")
        void admin_can_inherit_from_clerk(String username, List<String> auths) {
            can_access_clerk(uri, username, auths);
        }
        @ParameterizedTest
        @MethodSource("non_clerk_auths")
        void customer_cannot_access_clerk(String username, List<String> auths) {
            cannot_access_clerk(uri, username, auths);
        }
    }

     @Nested
    class expression extends MethodSources {
        final String uri = serverConfig.getBaseUrl().toString() + "/api/authorities/expressions";

        @ParameterizedTest
        @MethodSource("user_auths")
        void admin_clerk_can_inherit_from_customer(String username, List<String> auths) {
            can_access_customer(uri, username, auths);
        }
        @ParameterizedTest
        @MethodSource("admin_clerk_auths")
        void admin_can_inherit_from_clerk(String username, List<String> auths) {
            can_access_clerk(uri, username, auths);
        }
        @ParameterizedTest
        @MethodSource("non_clerk_auths")
        void customer_cannot_access_clerk(String username, List<String> auths) {
            cannot_access_clerk(uri, username, auths);
        }
    }

    
}
