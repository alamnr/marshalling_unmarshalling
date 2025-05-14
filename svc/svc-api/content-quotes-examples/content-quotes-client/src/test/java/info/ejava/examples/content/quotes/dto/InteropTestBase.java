package info.ejava.examples.content.quotes.dto;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

import info.ejava.examples.content.quotes.util.QuoteDTOFactory;
import static info.ejava.examples.content.quotes.util.QuoteDTOFactory.oneUpId;

public class InteropTestBase {
    
    protected static ZonedDateTime jul4Utc = ZonedDateTime.of(1776, 7, 4, 8, 2, 4, 123456789, ZoneOffset.UTC);

    protected static MessageDTO msg = new MessageDTO("http://testing", "testing");
    protected static QuoteDTO quote = new QuoteDTOFactory().make(oneUpId);
    protected static QuoteListDTO quotes = new QuoteDTOFactory().listBuilder().make(3, 3, oneUpId);

    protected static ADate dates = ADate.of(jul4Utc);
    protected static ADate datesNomsecs = ADate.of(jul4Utc.withNano(0));
    protected static ADate dates5micro = ADate.of(jul4Utc.withNano(123450000));
    protected static ADate datesEST = ADate.of(ZonedDateTime.of(jul4Utc.toLocalDateTime(), ZoneId.of("EST", ZoneId.SHORT_IDS)));
    protected static ADate datesEST5micro = ADate.of(ZonedDateTime.of(jul4Utc.toLocalDateTime(), ZoneId.of("EST", ZoneId.SHORT_IDS)).withNano(123450000));
    protected static ADate dates0430micro = ADate.of(ZonedDateTime.of(jul4Utc.toLocalDateTime(), ZoneOffset.ofHoursMinutes(4, 30)));

    private static Stream<Arguments> dtos() {
        return Stream.of(
                 Arguments.of(dates),
                 Arguments.of(datesNomsecs),
                 Arguments.of(dates5micro),
                 Arguments.of(datesEST),
                 Arguments.of(datesEST5micro),
                 Arguments.of(dates0430micro),
                 Arguments.of(msg),
                 Arguments.of(quote),
                 Arguments.of(quotes)
        );
    }

    protected void compareTimes(ADate request, ADate result) {
        ZonedDateTime zdtUtc = request.getZdt().withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime dateUtc = ZonedDateTime.ofInstant(result.getInstant(), ZoneOffset.UTC);
        Assertions.assertThat(zdtUtc).isEqualTo(dateUtc);
    }

}
