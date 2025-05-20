package info.ejava.examples.svc.content.quotes.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import info.ejava.examples.content.quotes.api.QuotesAPI;
import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.dto.QuoteListDTO;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

public interface QuoteHttpIfaceAPI {

    @PostExchange(QuotesAPI.QUOTES_PATH)
    ResponseEntity<QuoteDTO> createQuote(@RequestBody(required = true) QuoteDTO quoteDTO);

    @PutExchange(QuotesAPI.QUOTE_PATH)
    ResponseEntity<Void> updateQuote(@PathVariable(name = "id" , required = true) int id , 
                                        @RequestBody(required = true)QuoteDTO quoteToUpdate);

    
    @GetExchange(QuotesAPI.QUOTES_PATH)
    ResponseEntity<QuoteListDTO> getQuotes(@RequestParam(name = "offset", defaultValue = "0")int offset,
                                            @RequestParam(name="limit", defaultValue = "0")int limit);
    
    @GetExchange(QuotesAPI.QUOTE_PATH)
    ResponseEntity<QuoteDTO> getQuote(@PathVariable(name = "id", required = true)int id);

    @GetExchange(QuotesAPI.RANDOM_QUOTE_PATH)
    ResponseEntity<QuoteDTO> randomQuote();

    

    @HttpExchange(method = "HEAD", url = QuotesAPI.QUOTE_PATH)
    ResponseEntity<Void> containsQuote(@PathVariable(name = "id", required = true)int id);


    // @HttpExchange(method = RequestMethod.HEAD, url = QuotesAPI.QUOTE_PATH)    
    //  ResponseEntity<Void> containsQuote(int id);

    @DeleteExchange(QuotesAPI.QUOTE_PATH)
    ResponseEntity<Void> deleteQuote(@PathVariable(name="id", required = true) int id);

    @DeleteExchange(QuotesAPI.QUOTES_PATH)
    ResponseEntity<Void> deleteAllQuotes();
}