package info.ejava.examples.content.quotes.services;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import info.ejava.examples.content.quotes.util.QuoteDTOFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "test", havingValue = "true", matchIfMissing = true)
@Slf4j
public class Populate  implements CommandLineRunner {

    private final QuoteService quotesService;
    private QuoteDTOFactory quoteDTOFactory = new QuoteDTOFactory();


    @Override
    public void run(String... args) throws Exception {

        int count = 20;
        log.info("populating {} quotes", count);

        quoteDTOFactory.listBuilder().make(count, count)
                        .getQuotes()
                        .stream()
                        .forEach(quote -> quotesService.createQuote(quote));
        
    }
    
}
