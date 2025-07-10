package info.ejava.examples.svc.authn.authcfg.controllers;

import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

//@CrossOrigin // Default to all origin
@Tag(name = "hello-controller", description = "demonstrate sample calls with security constraints")
@RestController
public class HelloController {

    @Operation(description = "sample anonymous GET")
    @RequestMapping(path = "/api/anonymous/hello", method = RequestMethod.GET)
    public String getHello(@RequestParam(name = "name", defaultValue = "you", required = false) String name,
                            @Parameter(hidden = true) // exclude from OpenAPI
                            @AuthenticationPrincipal UserDetails user){
        return "hello " + name + " :caller = "+ (user==null?"(null)":user.getUsername());
    }

    @Operation(description = "sample anonymous POST")
    @RequestMapping(path = "/api/anonymous/hello", method = RequestMethod.POST, 
                    consumes = MediaType.TEXT_PLAIN_VALUE, produces= MediaType.TEXT_PLAIN_VALUE )
    public String postHello(@RequestBody String name,
                            @Parameter(hidden = true)
                            @AuthenticationPrincipal UserDetails user){
        return "hello " + name + " :caller =" + (user==null ? "(null)":user.getUsername() );
    }

    @Operation(description = "sample authenticated GET", security = @SecurityRequirement(name = "basicAuth"))
    @RequestMapping(path = "/api/authn/hello", method = RequestMethod.GET)
    public String getHelloAuthn(@RequestParam(name = "name", defaultValue = "you", required= false)String name,
                                @Parameter(hidden = true)
                                @AuthenticationPrincipal UserDetails user){
        return "hello " + name + " :caller = " + user.getUsername();
    }

    @Operation(description = "sample authenticated POST", security = @SecurityRequirement(name = "basicAuth"))
    @RequestMapping(path = "/api/authn/hello", method = RequestMethod.POST,
                    consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String postHelloAuthn(@RequestBody String name,
                                 @Parameter(hidden = true) 
                                 @AuthenticationPrincipal UserDetails user){
        return "hello " + name + " :caller = " + user.getUsername();
    }

    @Operation(description = "sample authenticated alt GET", security = @SecurityRequirement(name = "basicAuth"))
    @RequestMapping(path = "/api/alt/hello", method = RequestMethod.GET)
    public String getHelloAlt(@RequestParam(name = "name",defaultValue = "you",required = false)String name){
        Object principal = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                            .map(authn -> authn.getPrincipal())
                            .orElse("(null)");
        String username = principal instanceof UserDetails ? ((UserDetails)principal).getUsername() : principal.toString();

        return "hello " + name + " :caller = " + username;
    }

    @Operation(description = "sample authenticated alt POST", security = @SecurityRequirement(name = "basicAuth"))
    @RequestMapping(path = "/api/alt/hello", method = RequestMethod.POST,
                    consumes=MediaType.TEXT_PLAIN_VALUE, produces=MediaType.TEXT_PLAIN_VALUE)
    public String postHelloAlt(@RequestBody String name){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = null != authentication ? authentication.getPrincipal() : "(null)";
        String username = principal instanceof UserDetails ? ((UserDetails)principal).getUsername() : principal.toString();
        return "hello " + name + " :caller = "+ username;
    }
}
