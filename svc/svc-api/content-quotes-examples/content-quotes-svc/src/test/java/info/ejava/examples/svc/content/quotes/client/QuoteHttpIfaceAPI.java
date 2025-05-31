package info.ejava.examples.svc.content.quotes.client;

import org.springframework.http.MediaType;
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

    @PostExchange(url =  QuotesAPI.QUOTES_PATH,
                  contentType = MediaType.APPLICATION_JSON_VALUE,
                  accept = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    ResponseEntity<QuoteDTO> createQuoteJson(@RequestBody(required = true) QuoteDTO quoteDTO);

    @PostExchange(url =  QuotesAPI.QUOTES_PATH,
                  contentType = MediaType.APPLICATION_XML_VALUE,
                  accept = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    ResponseEntity<QuoteDTO> createQuoteXml(@RequestBody(required = true) QuoteDTO quoteDTO);


    @PutExchange(url = QuotesAPI.QUOTE_PATH, 
                contentType = MediaType.APPLICATION_JSON_VALUE,
                accept = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateQuote(@PathVariable(name = "id" , required = true) int id , 
                                        @RequestBody(required = true)QuoteDTO quoteToUpdate);

    
    @GetExchange(url = QuotesAPI.QUOTES_PATH,
                    accept = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<QuoteListDTO> getQuotes(@RequestParam(name = "offset", defaultValue = "0")int offset,
                                            @RequestParam(name="limit", defaultValue = "0")int limit);
    
    @GetExchange(url = QuotesAPI.QUOTE_PATH,
                    accept = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<QuoteDTO> getQuote(@PathVariable(name = "id", required = true)int id);

    @GetExchange(url = QuotesAPI.RANDOM_QUOTE_PATH, 
                    accept = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<QuoteDTO> randomQuote();

    

    @HttpExchange(method = "HEAD", url = QuotesAPI.QUOTE_PATH)
    ResponseEntity<Void> containsQuote(@PathVariable(name = "id", required = true)int id);


    @DeleteExchange(url = QuotesAPI.QUOTE_PATH)
    ResponseEntity<Void> deleteQuote(@PathVariable(name="id", required = true) int id);

    @DeleteExchange(url = QuotesAPI.QUOTES_PATH)
    ResponseEntity<Void> deleteAllQuotes();
}