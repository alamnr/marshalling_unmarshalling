package info.ejava.examples.svc.authz.authorities.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/authorities/paths")
@RequiredArgsConstructor
public class PathAuthoritiesController {
    
    private final WhoAmIController whoAmI;

    @GetMapping(path = "admin", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doAdmin(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails user) {
        return whoAmI.getCallerInfo(user);
    }

    @GetMapping(path = "clerk" , produces =  {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doClerk(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails user) {
        return whoAmI.getCallerInfo(user);
    }

    @GetMapping(path = "customer", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doCustomer(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user) {
        return whoAmI.getCallerInfo(user);
    }

    @GetMapping(path = "price", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> checkPrice(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }

    @GetMapping(path = "authn", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doAuthenticated(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }

    @GetMapping(path = "anonymous", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doAnonymous(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }

    @GetMapping(path = "nobody", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> doNobody(@Parameter(hidden = true)@AuthenticationPrincipal UserDetails user){
        return whoAmI.getCallerInfo(user);
    }
}
