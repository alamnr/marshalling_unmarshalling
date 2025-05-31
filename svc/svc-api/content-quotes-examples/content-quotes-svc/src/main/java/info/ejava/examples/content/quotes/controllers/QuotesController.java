package info.ejava.examples.content.quotes.controllers;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import info.ejava.examples.content.quotes.api.QuotesAPI;
import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.dto.QuoteListDTO;
import info.ejava.examples.content.quotes.services.QuoteService;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
public class QuotesController {
    
    private final QuoteService quoteService;

    public QuotesController(QuoteService quoteService){
        this.quoteService = quoteService;
    }

    /*
     * This method provides 2 example method signatures. Use the @RequestBody from when
     * headers are of no interest. Use RequestEntity when headers are of interest
     */

     /*
      Idempotence describes a characteristic where a repeated event produces the same
      outcome every time executed. This is a very important concept in distributed systems that commonly have to 
      implement eventual consistency — where failure recovery can cause unacknowledged commands to be executed multiple times.
      The idempotent characteristic is independent of method safety. Idempotence only 
      requires that the same result state be achieved each time called.
      */
    // Non idempotent http method - POST, PATCH, Connect
    /*
     * The standard convention of Internet protocol is that most methods except for POST 
     * are assumed to be idempotent. That means a page refresh for a page obtained from 
     * a GET (GET, PUT, DELETE, HEAD, OPTIONS) gets immediately refreshed and a warning dialogue is displayed if it was the result of a POST.
     */
     @RequestMapping(path = QuotesAPI.QUOTES_PATH,
                     method = RequestMethod.POST,
                     consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                     produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE} )
    public ResponseEntity<QuoteDTO> createQuote(@RequestBody QuoteDTO quote){
    // public ResponseEntity<QuoteDTO> createQuote(RequestEntity<QuoteDTO> request){
            
    //     QuoteDTO quote = request.getBody();
    //     log.info("CONTENT_TYPE= {}", request.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    //     log.info("ACCEPT= {}", request.getHeaders().get(HttpHeaders.ACCEPT));
        QuoteDTO result = quoteService.createQuote(quote);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                                            .replacePath(QuotesAPI.QUOTE_PATH)
                                            .build(result.getId());

        //ResponseEntity<QuoteDTO> response = ResponseEntity.status(HttpStatus.CREATED).location(uri).body(result);
        ResponseEntity<QuoteDTO> response = ResponseEntity.created(uri).body(result);
        
        return response;
    }

    // Idempotent http method - GET, PUT, DELETE, HEAD, OPTIONS
    /*
     * The standard convention of Internet protocol is that most methods except for POST 
     * are assumed to be idempotent. That means a page refresh for a page obtained from 
     * a GET (GET, PUT, DELETE, HEAD, OPTIONS) gets immediately refreshed and a warning dialogue is displayed if it was the result of a POST.
     */
    
    @RequestMapping(path = QuotesAPI.QUOTES_PATH,
                    method = RequestMethod.GET,
                    produces = {MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<QuoteListDTO> getQuotes(@RequestParam(name = "offset", defaultValue = "0") int offset,
                                                @RequestParam(name = "limit", defaultValue = "0") int limit){

            QuoteListDTO quotes = quoteService.getQuotes(offset, limit);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
            ResponseEntity<QuoteListDTO> responses = ResponseEntity.ok().header(HttpHeaders.CONTENT_LOCATION, uri.toString()).body(quotes);
            return responses;
                                            
    }

    // Idempotent http method - GET, PUT, DELETE, HEAD, OPTIONS
    /*
     * The standard convention of Internet protocol is that most methods except for POST 
     * are assumed to be idempotent. That means a page refresh for a page obtained from 
     * a GET (GET, PUT, DELETE, HEAD, OPTIONS) gets immediately refreshed and a warning dialogue is displayed if it was the result of a POST.
     */
    
    @RequestMapping(path = QuotesAPI.QUOTE_PATH,
                    method = RequestMethod.PUT,
                    consumes = {MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> updateQuote(@PathVariable("id") int id, @RequestBody QuoteDTO quote){
        quoteService.updateQuote(id, quote);
        ResponseEntity<Void> response = ResponseEntity.ok().build();
        return response;
    }

    // Idempotent http method - GET, PUT, DELETE, HEAD, OPTIONS
    /*
     * The standard convention of Internet protocol is that most methods except for POST 
     * are assumed to be idempotent. That means a page refresh for a page obtained from 
     * a GET (GET, PUT, DELETE, HEAD, OPTIONS) gets immediately refreshed and a warning dialogue is displayed if it was the result of a POST.
     */
    
    @RequestMapping(path = QuotesAPI.QUOTE_PATH, method = RequestMethod.HEAD)
    public ResponseEntity<Void> containsQuote(@PathVariable("id") int id){

        boolean exist = quoteService.containsQuote(id);
        ResponseEntity<Void> response = exist ? ResponseEntity.ok().build():ResponseEntity.notFound().build();
        return response;
    }

    // Idempotent http method - GET, PUT, DELETE, HEAD, OPTIONS
    /*
     * The standard convention of Internet protocol is that most methods except for POST 
     * are assumed to be idempotent. That means a page refresh for a page obtained from 
     * a GET (GET, PUT, DELETE, HEAD, OPTIONS) gets immediately refreshed and a warning dialogue is displayed if it was the result of a POST.
     */
    
    @RequestMapping(path=QuotesAPI.QUOTE_PATH, method = RequestMethod.GET,
                        produces= {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public  ResponseEntity<QuoteDTO> getQuote(@PathVariable("id") int id) {

        QuoteDTO quote = quoteService.getQuote(id);

        // URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
        //                                     .replacePath(QuotesAPI.QUOTE_PATH)
        //                                     .build(quote.getId());
        //  ResponseEntity<QuoteDTO> response = ResponseEntity.ok().header(HttpHeaders.CONTENT_LOCATION, uri.toString())
        //                                       .body(quote);
        ResponseEntity<QuoteDTO> response = ResponseEntity.ok(quote);
        return response;            
    }

    // Idempotent http method - GET, PUT, DELETE, HEAD, OPTIONS
    /*
     * The standard convention of Internet protocol is that most methods except for POST 
     * are assumed to be idempotent. That means a page refresh for a page obtained from 
     * a GET (GET, PUT, DELETE, HEAD, OPTIONS) gets immediately refreshed and a warning dialogue is displayed if it was the result of a POST.
     */
    

    @RequestMapping(path = QuotesAPI.RANDOM_QUOTE_PATH, method = RequestMethod.GET, 
                        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE} )
    public ResponseEntity<QuoteDTO> randomQuote(){
        QuoteDTO randomQuote = quoteService.randomQuote();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().replacePath(QuotesAPI.QUOTE_PATH).build(randomQuote.getId());
        ResponseEntity<QuoteDTO> response = ResponseEntity.ok().header(HttpHeaders.CONTENT_LOCATION, uri.toString()).body(randomQuote);
        return response;
    }

    // Idempotent http method - GET, PUT, DELETE, HEAD, OPTIONS
    /*
     * The standard convention of Internet protocol is that most methods except for POST 
     * are assumed to be idempotent. That means a page refresh for a page obtained from 
     * a GET (GET, PUT, DELETE, HEAD, OPTIONS) gets immediately refreshed and a warning dialogue is displayed if it was the result of a POST.
     */
    
    @RequestMapping(path = QuotesAPI.QUOTE_PATH, method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteQuote(@PathVariable("id") int id) {
        quoteService.deleteQuote(id);

        ResponseEntity<Void> response = ResponseEntity.noContent().build();
        return response;
    }

    // Idempotent http method - GET, PUT, DELETE, HEAD, OPTIONS
    /*
     * The standard convention of Internet protocol is that most methods except for POST 
     * are assumed to be idempotent. That means a page refresh for a page obtained from 
     * a GET (GET, PUT, DELETE, HEAD, OPTIONS) gets immediately refreshed and a warning dialogue is displayed if it was the result of a POST.
     */
    
    @RequestMapping(path = QuotesAPI.QUOTES_PATH, method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteAllQuotes() {
        quoteService.deleteAllQuotes();
        ResponseEntity<Void> response = ResponseEntity.noContent().build();
        return response;
    }
}
