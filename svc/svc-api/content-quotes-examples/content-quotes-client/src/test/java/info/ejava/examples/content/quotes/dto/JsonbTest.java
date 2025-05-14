package info.ejava.examples.content.quotes.dto;

import static info.ejava.examples.content.quotes.util.QuoteDTOFactory.oneUpId;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.json.JsonConfig;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonbTest  extends MarshallingTestBase {

    private Jsonb builder;

    @BeforeEach
    public void init() {
        JsonbConfig config = new JsonbConfig().setProperty(JsonbConfig.FORMATTING, true);
        builder = JsonbBuilder.create(config);

    }

    @Override
    protected <T> String marshal(T object) throws Exception {
            if(object == null) {
                return "";
            }

            String buffer = builder.toJson(object);
            log.info("{} toJSON:  {}", object, buffer );
            return buffer;
    }

    @Override
    protected <T> T unmarshal(Class<T> type, String buffer) throws Exception {
        T result = (T) builder.fromJson(buffer, type);
        log.info("{} fromJSON: {}", buffer, result);
        return result;
    }

    private <T> T marshal_and_unmarshal(T object, Class<T> type) throws Exception {
        String jsonbJson = marshal(object);
        T result  = unmarshal( type, jsonbJson);
        return result;
    }

    @Test
    void quote_dto_marshal() throws Exception {
        // given / arrange
        QuoteDTO quote = quoteDTOFactory.make();

        // when / act
        QuoteDTO result = marshal_and_unmarshal(quote, QuoteDTO.class);
        
        // then / avaluate / assert

        BDDAssertions.then(result.getDate()).isEqualTo(quote.getDate());
        BDDAssertions.then(result.getText()).isEqualTo(quote.getText());
        BDDAssertions.then(result.getAuthor()).isEqualTo(quote.getAuthor());
        BDDAssertions.then(result.getIgnored()).isNull();
    }

    @Test
    void quoteList_dto_marshal() throws Exception {

        // given / arrange
        QuoteListDTO quoteList = quoteDTOFactory.listBuilder().make(3, 3, oneUpId);

        // when / act
        QuoteListDTO result  = marshal_and_unmarshal(quoteList, QuoteListDTO.class);

        // then  / evaluate / assert
        BDDAssertions.then(result.getCount()).isEqualTo(quoteList.getCount());
        Map<Integer,QuoteDTO> quoteMap = quoteList.getQuotes().stream().collect(Collectors.toMap(QuoteDTO::getId,q->q));
        for (QuoteDTO actual : quoteList.getQuotes()) {
            QuoteDTO expected = quoteMap.get(actual.getId());

            BDDAssertions.then(expected.getDate()).isEqualTo(actual.getDate());
            BDDAssertions.then(expected.getText()).isEqualTo(actual.getText());
            BDDAssertions.then(expected.getAuthor()).isEqualTo(actual.getAuthor());
            BDDAssertions.then(expected.getIgnored()).isNull();
        }
    }

    @Test
    void msg_dto_marshal() throws Exception {
        // given / arrange
        MessageDTO msg = MessageDTO.builder().text("a msg").url("/api/msgs").build();

        // when / act
        MessageDTO result = marshal_and_unmarshal(msg, MessageDTO.class);

        // then / evaluate / assert

        BDDAssertions.then(result.getText()).isEqualTo(msg.getText());
        BDDAssertions.then(result.getUrl()).isEqualTo(msg.getUrl());

    }

    @ParameterizedTest
    @MethodSource("read_by_formats")
    public void marshal_dates(ZonedDateTime zdt, String name, Object format) throws Exception {
        //marshall an object with a date using the baseline parser
        ADate dates = ADate.of(zdt);
        String text = marshal(dates);
        name = (format instanceof DateTimeFormatter) ? name : (String)format;

        //extract the date out of the text payload
        String dateText = get_date(text);
        log.info("{} {}", dateText, dates);
        //log.info("{} {} parsed {}", format, (tz==null? "null":tz.getID()), dateText);
        log.info("{} parsed {}", name, dateText);

        //parse it with a variable DTF format
        DateTimeFormatter dtf = null;
        if (format instanceof DateTimeFormatter) {
            dtf = (DateTimeFormatter) format;
        } else {
            dtf = DateTimeFormatter.ofPattern((String)format);
        }
        Date date = Date.from(ZonedDateTime.parse(dateText, dtf).toInstant());
        Assertions.assertThat(date).isEqualTo(Date.from(zdt.toInstant()));
    }

    @ParameterizedTest
    @MethodSource("read_from_formats")
    public void parse_date(String dateText, String name, Date date) throws Exception {
        //given - a known date with a specific format added to the marshalled body
        String body = get_marshalled_adate(dateText);
        log.info("{} => {}", name, dateText);

        //when unmarshalled
        ADate dates=null;
        try {
            dates = unmarshal(ADate.class, body);
        } catch (Exception ex) {
            log.debug("{}", ex.toString());
            fail(ex.toString());
        }
        Assertions.assertThat(dates.getDate()).isEqualTo(date);
    }



    
    @Override
    protected String get_marshalled_adate(String dateText) {
        return String.format(DATES_JSON, dateText);
    }

    @Override
    protected String get_date(String marshalledQuote) {
        Pattern pattern = Pattern.compile(".*\"date\": \"(.+?)\",.*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(marshalledQuote);

        if (matcher.matches()) {
            String date = matcher.group(1);
            return date;
        }
        return null;
    }

    @Override
    protected boolean canParseFormat(String format, ZoneOffset tz) {
        //JSONB cannot parse +05:00, but can parse +05 and Z
//        if (format.equals(ISO_8601_DATETIME5_FORMAT) && tz==null) {
//            return false;
//        }
        return true;
    }
    
}
