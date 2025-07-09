package info.ejava.examples.svc.authn.authcfg.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "hello-controller", description = "demonstrate sample calls with security constraints")
@RestController
public class HelloController {

    @Operation(description = "sample anonymous GET")
    @RequestMapping(path = "/api/anonymous/hello", method = RequestMethod.GET)
    public String getHello(@RequestParam(name = "name", defaultValue = "you", required = false) String name){
        return "hello " + name;
    }

    @Operation(description = "sample anonymous POST")
    @RequestMapping(path = "/api/anonymous/hello", method = RequestMethod.POST, 
                    consumes = MediaType.TEXT_PLAIN_VALUE, produces= MediaType.TEXT_PLAIN_VALUE )
    public String postHello(@RequestBody String name){
        return "hello " + name;
    }

    @Operation(description = "sample authenticated GET")
    @RequestMapping(path = "/api/authn/hello", method = RequestMethod.GET)
    public String getHelloAuthn(@RequestParam(name = "name", defaultValue = "you", required= false)String name){
        return "hello " + name;
    }

    @Operation(description = "sample authenticated POST")
    @RequestMapping(path = "/api/authn/hello", method = RequestMethod.POST,
                    consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String postHelloAuthn(@RequestBody String name){
        return "hello " + name;
    }

    @Operation(description = "sample authenticated alt GET")
    @RequestMapping(path = "/api/alt/hello", method = RequestMethod.GET)
    public String getHelloAlt(@RequestParam(name = "name",defaultValue = "you",required = false)String name){
        return "hello " + name;
    }

    @Operation(description = "sample authenticated alt POST")
    @RequestMapping(path = "/api/alt/hello", method = RequestMethod.POST,
                    consumes=MediaType.TEXT_PLAIN_VALUE, produces=MediaType.TEXT_PLAIN_VALUE)
    public String postHelloAlt(@RequestBody String name){
        return "hello " + name;
    }
}
