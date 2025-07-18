package info.ejava.examples.svc.authz.authorities.controllers;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/whoAmI")
public class WhoAmIController {

    @GetMapping(produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> getCallerInfo(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails user){
        
        List<?> values = (null != user) ? List.of(user.getUsername(), user.getAuthorities()) : List.of("null");
        String text = StringUtils.join(values);
        return ResponseEntity.ok(text);
    }
    
}
