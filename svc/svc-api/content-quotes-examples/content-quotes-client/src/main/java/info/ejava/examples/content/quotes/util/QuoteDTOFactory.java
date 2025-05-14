package info.ejava.examples.content.quotes.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import info.ejava.examples.content.quotes.dto.QuoteDTO;
import info.ejava.examples.content.quotes.dto.QuoteListDTO;
import net.datafaker.Faker;

public class QuoteDTOFactory {

    public static final AtomicInteger nextId = new AtomicInteger(1);
    public final Faker faker = new Faker();

    public int id() {
        return faker.number().numberBetween(1, 1000);
    }

    public String author(){
        return faker.hitchhikersGuideToTheGalaxy().character();
    }

    public String text(){
        return faker.hitchhikersGuideToTheGalaxy().quote();
    }

    public LocalDate date(){
        return toDate(faker.date().past(100*365, TimeUnit.DAYS).toInstant());
    }

    public static LocalDate toDate(Instant timeStamp){
        // remove time information
        return timeStamp.atOffset(ZoneOffset.UTC)
                        .toLocalDate();
    }

    public QuoteDTO make() {
        return QuoteDTO.builder()
                        .author(author())
                        .text(text())
                        .date(date())
                        .build();
    }

    @SafeVarargs
    public final QuoteDTO make(Consumer<QuoteDTO>... visitors){
        final QuoteDTO result = make();
        Stream.of(visitors).forEach(v -> v.accept(result));
        return result;
    }

    public static Consumer<QuoteDTO> oneUpId = o -> o.setId(nextId.getAndAdd(1));


    public QuoteListDTOFactory listBuilder(){
        return new QuoteListDTOFactory();
    }

    public class QuoteListDTOFactory {
        public String keywords(int min, int max) {
            return IntStream.range(0, faker.number().numberBetween(min, max))
                    .mapToObj(i->faker.company().buzzword())
                    .collect(Collectors.joining(" "));
        }
        @SafeVarargs
        public final List<QuoteDTO> quotes(int min, int max, Consumer<QuoteDTO>... visitors) {
            return IntStream.range(0, faker.number().numberBetween(min, max))
                    .mapToObj(i->QuoteDTOFactory.this.make(visitors))
                    .collect(Collectors.toList());
        }

        @SafeVarargs
        public final QuoteListDTO make(int min, int max, Consumer<QuoteDTO>... visitors) {
            return QuoteListDTO.builder()
                    .quotes(quotes(min, max, visitors))
                    .build();
        }

    }

}


