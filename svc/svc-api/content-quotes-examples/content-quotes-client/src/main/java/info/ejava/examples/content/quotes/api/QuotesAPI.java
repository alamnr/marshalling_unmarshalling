package info.ejava.examples.content.quotes;

public interface QuotesAPI {

     public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx";
     public static final String QUOTES_PATH = "api/quotes";
     public static final String  QUOTE_PATH = QUOTES_PATH + "/{id}";
     public static final String RANDOM_QUOTE_PATH = QUOTES_PATH + "/random";

     //public Mono<ResponseEntity< >
    
}
