package info.ejava.examples.content.quotes.dto;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import info.ejava.examples.common.time.ISODateFormat;
import info.ejava.examples.content.quotes.util.QuoteDTOFactory;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

@Slf4j
public abstract class MarshallingTestBase {

    protected static final Faker faker = new Faker();

    public static ZonedDateTime randomZdt() {
        return ZonedDateTime.ofInstant(faker.date().past(100*365, TimeUnit.DAYS).toInstant(),ZoneOffset.UTC);
    }
    public static final TimeZone UTC_TZ = TimeZone.getTimeZone("UTC");

    public static final String ISO_8601_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX"; //1976-07-04T00:00:00.123Z, .123+00
    public static final String RFC_822_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"; //1976-07-04T00:00:00.123+0000
    public static final String ISO_8601_DATETIME4_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXX"; //1976-07-04T00:00:00.123+0000
    public static final String ISO_8601_DATETIME5_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"; //1976-07-04T00:00:00.123+00:00
//    public static final String ISO_8601_DATETIME5_FORMAT_DTF = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSxxx"; //1976-07-04T00:00:00.123+00:00

    public final String DATES_XML=
    // "<q:dates xmlns:q=\"urn:ejava.svc-controllers.quotes\"><date>%s</date></q:dates>";
       "<q:dates xmlns:q=\"urn:ejava.svc-controllers.quotes\"><date>%s</date></q:dates>";

    public final String DATES_JSON = "{\"date\" : \"%s\"}";
    protected static ZonedDateTime jul4Utc = ZonedDateTime.of(1776, 7, 4, 0, 0, 0, 123456789, ZoneId.of("UTC"));
    protected QuoteDTOFactory quoteDTOFactory = new QuoteDTOFactory();

    public void init() {}
    
    protected abstract <T> String marshal(T object) throws Exception;
    protected abstract <T> T unmarshal(Class<T> type, String buffer) throws Exception;
    protected abstract String get_marshalled_adate(String dateText);
    protected abstract String get_date(String marshalledDates);

    private <T> T marshal_and_demarshal(T obj, Class<T> type) throws Exception {
        String buffer = marshal(obj);
        T result = unmarshal(type, buffer);
        return result;
    }

    public static Stream<Arguments> read_from_formats() {
        List<Arguments> params = new ArrayList<>();
        for (Object[] spec: new Object[][]{
                new Object[]{ "ISO_OFFSET_DATE_TIME", DateTimeFormatter.ISO_OFFSET_DATE_TIME, ZoneOffset.UTC },
                new Object[]{ "yyyy-MM-dd'T'HH:mm:ss.SSSX", null },
                new Object[]{ "yyyy-MM-dd'T'HH:mm:ss.SSSXX", null },
                new Object[]{ "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", null },
                new Object[]{ "yyyy-MM-dd'T'HH:mm:ss.SSSXXX[z]", null },
                new Object[]{ "yyyy-MM-dd'T'HH:mm:ss.SSSXXX[z]", null },
        }) {
            for (ZoneId zid : new ZoneId[]{ZoneOffset.UTC, ZoneOffset.ofHours(-5)}) {
                for (int nanos : new int[]{123456789, 100000000, 0}) {
                    ZonedDateTime zdt = jul4Utc.withNano(nanos).withZoneSameInstant(zid);
                    Date date = Date.from(zdt.toInstant());

                    String dateText = null;
                    if (spec[1]==null) {
                        SimpleDateFormat sdf = new SimpleDateFormat((String) spec[0]);
                        sdf.setTimeZone(TimeZone.getTimeZone(zid));
                        dateText = sdf.format(date);
                    } else {
                        OffsetDateTime odt = OffsetDateTime.ofInstant(date.toInstant(), zid);
                        log.info("{}", spec[1]);
                        dateText = ((DateTimeFormatter) spec[1]).format(odt);
                    }
                    params.add(Arguments.of(dateText, spec[0], date));
                }
            }
        }
        return params.stream();
    }

    protected boolean canParseFormat(String format, ZoneOffset tzo) {
        return true;
    }

    public static Stream<Arguments> read_by_formats() {
        List<Arguments> params = new ArrayList<>();
        for (ZoneId zid: new ZoneId[]{ ZoneOffset.UTC, ZoneOffset.ofHours(-5)}) {
            for (int nanos: new int[]{123456789, 100000000, 0}) {
                ZonedDateTime zdt = jul4Utc.withNano(nanos).withZoneSameInstant(zid);
                params.add(Arguments.of(zdt, "ISODateFormat.UNMARSHALLER", ISODateFormat.UNMARSHALER));
                params.add(Arguments.of(zdt, "ISO_OFFSET_DATE_TIME", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
//                params.add(Arguments.of(zdt, null, "yyyy-MM-dd'T'HH:mm:ss[.SSS]X"));
//                params.add(Arguments.of(zdt, null, "yyyy-MM-dd'T'HH:mm:ss[.SSS]XX"));
//                params.add(Arguments.of(zdt, null, "yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX"));
            }
        }
        return params.stream();
    }

    

    
}
