package info.ejava.examples.svc.authz.authorities.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/authorities/jsr250")
@RequiredArgsConstructor
public class Jsr250AuthoritiesController {
    
    private final WhoAmIController whoAmI;

    // Spring 6 security roles cannot start with ROLE_ prefix
    //@RolesAllowed("ROLE_ADMIN") // Spring 5
    @RolesAllowed("ADMIN") // spring 6
    @GetMapping(path = "admin", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doAdmin(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user) {
        return whoAmI.getCallerInfo(user);
    }

    //@RolesAllowed("ROLE_CLERK") // Spring 5
    @RolesAllowed("CLERK") // spring 6 
    @GetMapping(path = "clerk" , produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doClerk(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }

    //@RolesAllowed("ROLE_CUSTOMER") // Spring 5
    @RolesAllowed("CUSTOMER") // spring 6
    @GetMapping(path = "customer" , produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doCustomer(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }

    //@RolesAllowed({"ROLE_ADMIN","ROLE_CLERK","PRICE_CHECK"}) // spring 5
    @RolesAllowed({"ADMIN","CLERK","PRICE_CHECK"}) // spring 6  note : point to be noted @RolesAllowed does not support permission authority
    @GetMapping(path = "price", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> checkPrice(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }

    @PermitAll
    @GetMapping(path = "authn", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doAuthenticated(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }

    @PermitAll
    @GetMapping(path="anonymous", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doAnonymous(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }

    @DenyAll
    @GetMapping(path = "nobody", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doNobody(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }
}
