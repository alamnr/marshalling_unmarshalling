package info.ejava.examples.svc.authz.authorities.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/authorities/expressions")
@RequiredArgsConstructor
public class ExpressionAuthoritiesController {

    private final WhoAmIController whoAmI;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "admin", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doAdmin(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }

    @PreAuthorize("hasAuthority('ROLE_CLERK')")
    @GetMapping(path = "clerk" , produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doClerk(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user) {
        return whoAmI.getCallerInfo(user);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping(path = "customer", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doCustomer(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLERK') or hasAuthority('PRICE_CHECK')")
    @GetMapping(path = "price", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> checkPrice(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "authn",produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doAuthentiCate(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }

    @PreAuthorize("permitAll")
    @GetMapping(path = "anonymous", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doAnonymous(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user) {
        return whoAmI.getCallerInfo(user);
    }

    @PreAuthorize("denyAll")
    @GetMapping(path="nobody", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doNobody(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }
    
}