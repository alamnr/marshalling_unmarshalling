package info.ejava.examples.svc.content.quotes.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import info.ejava.examples.common.exception.ClientErrorException;
import info.ejava.examples.common.exception.ClientErrorException.BadRequestException;
import info.ejava.examples.common.exception.ClientErrorException.InvalidInputException;
import info.ejava.examples.common.exception.ClientErrorException.NotFoundException;
import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.dto.QuoteListDTO;
import info.ejava.examples.content.quotes.services.QuoteService;
import info.ejava.examples.content.quotes.services.QuotesServiceImpl;
import info.ejava.examples.content.quotes.util.QuoteDTOFactory;
import info.ejava.examples.content.quotes.util.QuoteDTOFactory.QuoteListDTOFactory;
import lombok.extern.slf4j.Slf4j;
import static info.ejava.examples.content.quotes.util.QuoteDTOFactory.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class QuotesServiceTest {

    QuoteService quoteService;

    QuoteDTOFactory quoteDTOFactory = new QuoteDTOFactory();
    @Mock
    Map<Integer, QuoteDTO> quoteMap;

    @BeforeEach
    public void init(){
        quoteService = new QuotesServiceImpl(quoteMap);
    }

    @Test
    public void add_quote_accepted() {
        // given / arrange - a valid quote
        QuoteDTO validQuote = quoteDTOFactory.make();

        // when / act - added to the service
        QuoteDTO createdQuote = quoteService.createQuote(validQuote);

        // then / evaluate-assert   - an identifier was created

        BDDAssertions.assertThat(createdQuote.getId()).isPositive();
        log.debug("{}", createdQuote);
    }

    @Test
    public void update_existing_quote() {
        // given / arrange - an existing quote
        QuoteDTO existingQuote = quoteDTOFactory.make(oneUpId);
        QuoteDTO updateQuote = quoteDTOFactory.make();
        
        BDDMockito.given(quoteMap.containsKey(existingQuote.getId())).willReturn(Boolean.TRUE);

        // when / act - updating existing quote
        quoteService.updateQuote(existingQuote.getId(), updateQuote);

        // then / evaluate-assert - no exception thrown
        BDDMockito.then(quoteMap).should(times(1)).containsKey(existingQuote.getId());
        BDDMockito.then(quoteMap).should(times(1)).put(existingQuote.getId(), updateQuote);
    }

    @Test
    public void get_quote() {
        // given - an existing quote
        QuoteDTO existingQuote = quoteDTOFactory.make(oneUpId);
        int requestId = existingQuote.getId();

        BDDMockito.given(quoteMap.get(requestId)).willReturn(existingQuote);

        // when / act - requesting a quote by id
        QuoteDTO returnedQuote = quoteService.getQuote(requestId);

        // then - evaluate/assert
        BDDMockito.then(quoteMap).should(times(1)).get(requestId);
        BDDAssertions.then(returnedQuote).isEqualTo(existingQuote);
    }

    @Test
    public void get_random_quote(){
        // given - many quotes
        BDDMockito.given(quoteMap.size()).willReturn(100);
        BDDMockito.given(quoteMap.values()).willReturn(quoteDTOFactory.listBuilder().quotes(100, 100, oneUpId));

        //  when 
        QuoteDTO returnedQuote = quoteService.randomQuote();

        // then 
        
        BDDMockito.then(quoteMap).should(times(1)).size();
        BDDMockito.then(quoteMap).should(times(1)).values();
        BDDAssertions.then(returnedQuote).isNotNull();
        log.info("random quote - {}", returnedQuote);
    }

    @Test
    public void remove_quote(){
        // given
        QuoteDTO existingQuote = quoteDTOFactory.make();
        int requestedId = existingQuote.getId();
        BDDMockito.given(quoteMap.remove(requestedId)).willReturn(existingQuote);

        // when 
        quoteService.deleteQuote(requestedId);

        // then
        BDDMockito.then(quoteMap).should(times(1)).remove(requestedId);

    }

    @Test
    public void remove_all_quotes() {
        // when - requested to remove all quotes
        quoteService.deleteAllQuotes();

        // then

        BDDMockito.then(quoteMap).should(times(1)).clear();
    }

    @Test
    public void remove_unknown_quote(){
        // given 
        int requestId = 13;
        BDDMockito.given(quoteMap.remove(requestId)).willReturn(null);

        // when - requested to remove , will not report that does not exist
        quoteService.deleteQuote(requestId);

        // then
        BDDMockito.then(quoteMap).should(times(1)).remove(requestId);
    }

    @Test
    public void get_unknown_quote(){
        // given - an unknown quoteId
        int unknownId = 13;

        // verify 
        BDDAssertions.assertThatThrownBy(() -> quoteService.getQuote(unknownId))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining(String.format("quote-[%d]",unknownId));
        
        BDDMockito.then(quoteMap).should(times(1)).get(unknownId);
    }

    @Test
    public void update_unknown_quote() {
        // given 
        int unknownId=13;
        QuoteDTO quoteDTO = quoteDTOFactory.make();

        // verify  - that updating existing quote
        BDDAssertions.assertThatThrownBy(() -> quoteService.updateQuote(unknownId, quoteDTO))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessageContaining(String.format("quote-[%d]", unknownId));

        BDDMockito.then(quoteMap).should(times(1)).containsKey(unknownId);
        verify(quoteMap, times(1)).containsKey(unknownId);

    }

    @Test
    public void update_knownQuote_with_bad_quote(){
        // given 
        int knownId =22;
        QuoteDTO badQuoteMissingTextAndUrl = new QuoteDTO();
        
        // verify
        BDDAssertions.assertThatThrownBy(() -> quoteService.updateQuote(knownId, badQuoteMissingTextAndUrl))
                                              .isInstanceOf(InvalidInputException.class)  
                                              .hasMessageContaining("missing required text");
        BDDMockito.then(quoteMap).should(times(0)).containsKey(knownId);
        verify(quoteMap, times(0)).containsKey(knownId);
    }

    @Test
    public void add_bad_quote_missingtext(){
        // given
        QuoteDTO badQuoteMissingText = new QuoteDTO();

        // verify
        BDDAssertions.assertThatThrownBy(()-> quoteService.createQuote(badQuoteMissingText))
                        .isInstanceOf(ClientErrorException.class)
                        .isInstanceOf(InvalidInputException.class)
                        .hasMessageContaining("missing required text");
    }

    @ParameterizedTest
    @CsvSource({"-1,10", "10,-5"})
    void get_invalid_offset_limit(int offset, int limit){
        // verify
        BDDAssertions.assertThatThrownBy(() -> quoteService.getQuotes(offset, limit))
                                         .isInstanceOf(BadRequestException.class)       
                                         .hasMessageContaining("offset[%d]", offset)
                                         .hasMessageContaining("limit[%d]", limit);
    }

    @Test
    void get_empty_quotes(){
        // given - no quote

        // when - ask for quotes we don't have
        QuoteListDTO response = quoteService.getQuotes(0, 100);
        log.info("{}", response);

        // then
        BDDAssertions.then(response.getCount()).isEqualTo(0);
        BDDAssertions.then(response.getOffset()).isEqualTo(0);
        BDDAssertions.then(response.getLimit()).isEqualTo(100);
        BDDAssertions.then(response.getTotal()).isEqualTo(0);

    }

     @Test
    public void get_many_quotes() {
        //given many quotes
        BDDMockito.given(quoteMap.size()).willReturn(100);
        List<QuoteDTO> quotes = quoteDTOFactory.listBuilder().quotes(100, 100, oneUpId);
        BDDMockito.given(quoteMap.values()).willReturn(quotes);

        /*
         * Purpose: Returns a paginated list of quotes from an internal collection.

            Parameters:

                offset: Number of elements to skip.

                limit: Maximum number of elements to return. If limit == 0, returns all from offset onward.

            Returns: QuoteListDTO — a data structure containing the paginated quote list.


         */
        //when asking for a page of quotes
        QuoteListDTO response = quoteService.getQuotes(10, 10);

        //then - page of results returned
        BDDAssertions.then(response.getCount()).isEqualTo(10);
        BDDAssertions.then(response.getQuotes().get(0)).isEqualTo(quotes.get(10));
        BDDAssertions.then(response.getQuotes().get(9)).isEqualTo(quotes.get(19));

        //and descriptive attributes filed in
        BDDAssertions.then(response.getOffset()).isEqualTo(10);
        BDDAssertions.then(response.getLimit()).isEqualTo(10);
        BDDAssertions.then(response.getTotal()).isEqualTo(100);
    }

}
