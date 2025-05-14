package info.ejava.examples.content.quotes.dto;

import static info.ejava.examples.content.quotes.util.QuoteDTOFactory.oneUpId;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import info.ejava.examples.common.time.ISODateFormat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JacksonJsonTest extends MarshallingTestBase {

    private ObjectMapper mapper ;

    @BeforeEach
    public void init() {
        mapper = new Jackson2ObjectMapperBuilder()
                    .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
                    .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .dateFormat(new ISODateFormat())
                    .createXmlMapper(false)
                    .build();
    }

    @Override
    protected <T> String marshal(T object) throws Exception {
            StringWriter buffer = new StringWriter();
            mapper.writeValue(buffer, object);
            log.info("{} toJSON: {} ",object, buffer);
            return buffer.toString();
    }

    @Override
    protected <T> T unmarshal(Class<T> type, String buffer) throws Exception {
        T result = mapper.readValue(buffer, type);
        log.info("{} fromJSON: {}", buffer, result);
        return result;
    }

    private<T> T marshal_and_unmarshal(T obj, Class<T> type) throws Exception {

            String jacksonJsonString = marshal(obj);
            
            T result = unmarshal(type, jacksonJsonString);
            
            return result;
    }

    @Test
    void quote_dto_marshal() throws Exception {
        // given / arrange a quote
        QuoteDTO quote = quoteDTOFactory.make();
        quote.setIgnored("ignored");

        // when / act
        QuoteDTO result = marshal_and_unmarshal(quote, QuoteDTO.class);

        // then / evaluate / assert

        BDDAssertions.then(result.getId()).isEqualTo(quote.getId());
        BDDAssertions.then(result.getText()).isEqualTo(quote.getText());
        BDDAssertions.then(result.getAuthor()).isEqualTo(quote.getAuthor());
        BDDAssertions.then(result.getDate()).isEqualTo(quote.getDate());
        BDDAssertions.then(result.getIgnored()).isNull();

        log.info(" date = {} ", quote.getDate());
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;
        log.info("dtf = {} ", dtf.format(quote.getDate()));
    }

    @Test 
    void quoteList_dto_marshals() throws Exception {
        // given / arrange - some quotes
        QuoteListDTO quoteListDTO = quoteDTOFactory.listBuilder().make(3,3, oneUpId);

        // when / act
        QuoteListDTO result = marshal_and_unmarshal(quoteListDTO, QuoteListDTO.class);

        // then / evaluate / assert
        BDDAssertions.then(result.getCount()).isEqualTo(quoteListDTO.getCount());
        Map<Integer, QuoteDTO> quoteMap = result.getQuotes().stream().collect(Collectors.toMap(QuoteDTO::getId, q->q));
        for (QuoteDTO  expected : quoteListDTO.getQuotes()) {
                QuoteDTO actual = quoteMap.get(expected.getId());
                BDDAssertions.then(actual).isNotNull();
                BDDAssertions.then(actual.getText()).isEqualTo(expected.getText());
                BDDAssertions.then(actual.getAuthor()).isEqualTo(expected.getAuthor());
                BDDAssertions.then(actual.getDate()).isEqualTo(expected.getDate());
                BDDAssertions.then(actual.getIgnored()).isNull();
        }
    }

    @Test
    void message_dto_marshal() throws Exception {
        // given / arrange
        MessageDTO msg = MessageDTO.builder().text("A text msg").url("/api/msg").build();

        // when / act 
        MessageDTO result = marshal_and_unmarshal(msg, MessageDTO.class);

        // then / evaluate / assert
        BDDAssertions.then(result.getText()).isEqualTo(msg.getText());
        BDDAssertions.then(result.getUrl()).isEqualTo(msg.getUrl());
    }

    protected String ADATE_JSON = "{\n" + "\"date\" : \"%s\"\n" + "}";

    @Override
    protected String get_marshalled_adate(String dateText) {
        return String.format(ADATE_JSON, dateText);
    }


    @Override
    public String get_date(String marshalledDates) {
        Pattern pattern = Pattern.compile(".*\"date\" : \"(.+)\".*}.*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(marshalledDates);

        if (matcher.matches()) {
            String date = matcher.group(1);
            return date;
        }
        return null;
    }

    @Override
    protected boolean canParseFormat(String format, ZoneOffset tzo) {
        return tzo==ZoneOffset.UTC;
    }    

    @ParameterizedTest
    @MethodSource("read_from_formats") 
    public void parse_date(String dateText, String name, Date date) throws Exception {
        // given / arrange - a known date with a specific format added to the marshalled body
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
        BDDAssertions.assertThat(dates.getDate()).isEqualTo(date);
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
        BDDAssertions.assertThat(date).isEqualTo(Date.from(zdt.toInstant()));
    }

    
}
