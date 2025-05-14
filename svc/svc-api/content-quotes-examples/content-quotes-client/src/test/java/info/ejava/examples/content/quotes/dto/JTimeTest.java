package info.ejava.examples.content.quotes.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JTimeTest {
    
     @Test
    void test() {
        for (LocalDateTime ldt : List.of(
                LocalDateTime.of(1970, 1, 1, 0, 0, 0, 1),
                LocalDateTime.of(1770, 1, 1, 0, 0, 0, 1),
                LocalDateTime.of(1970, 12, 1, 0, 0, 0, 1),
                LocalDateTime.of(1770, 12, 1, 0, 0, 0, 1)
        )) {
            log.info("instant={}", ldt.toInstant(ZoneOffset.UTC));
            log.info("ldt={}", ldt);
            ZoneId z = ZoneId.of("America/New_York");
            OffsetDateTime odt = ldt.atOffset(ZoneOffset.ofHours(-4));
            log.info("odt={}", odt);
            //Collection<String> z = new TreeSet<>(ZoneOffset.getAvailableZoneIds());
            ZonedDateTime zdt = odt.atZoneSameInstant(z);
            log.info("zdt={}", zdt);
        }

    }
}
