package info.ejava.examples.content.quotes.api;

import org.springframework.http.ResponseEntity;

import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.dto.QuoteListDTO;
import reactor.core.publisher.Mono;

/**
 * This interface is an example of an HTTP-base API that performs content
 * negotiation between client and server. The "consumes" property indicates
 * which types the server is willing to accept. The "produces" property
 * indicates which types the service is willing to return. The client will
 * express a list of supported types, in priority order when calling the service.
 */

public interface QuotesAPIWebClient {

     public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx";
     public static final String QUOTES_PATH = "api/quotes";
     public static final String  QUOTE_PATH = QUOTES_PATH + "/{id}";
     public static final String RANDOM_QUOTE_PATH = QUOTES_PATH + "/random";

     public Mono<ResponseEntity<QuoteDTO>> createQuote(QuoteDTO quote);
     public Mono<ResponseEntity<Void>> updateQuote(int id, QuoteDTO quote);
     public Mono<ResponseEntity<Void>> containsQuote(int id);
     public Mono<ResponseEntity<QuoteDTO>> getQuote(int id);
     public Mono<ResponseEntity<QuoteDTO>> randomQuote();
     public Mono<ResponseEntity<Void>> deleteQuote(int id);
     public Mono<ResponseEntity<Void>> deleteAllQuotes();
     public Mono<ResponseEntity<QuoteListDTO>> getQuotes(Integer offset, Integer limit);
     
    
}
