package info.ejava.examples.content.quotes.services;

import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.dto.QuoteListDTO;

public interface QuoteService {
    QuoteDTO createQuote(QuoteDTO quote);
    void updateQuote(int id, QuoteDTO quote);
    void deleteQuote(int id);
    void deleteAllQuotes();

    boolean containsQuote(int id);
    QuoteDTO getQuote(int id);
    QuoteDTO randomQuote();

    QuoteListDTO getQuotes(int offset, int limit);
}
