package info.ejava.examples.content.quotes.dto;

import java.io.StringWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
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
import static info.ejava.examples.content.quotes.util.QuoteDTOFactory.oneUpId;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class JacksonXmlTest extends MarshallingTestBase {

    private ObjectMapper mapper;

    @BeforeEach
    public void init() {
        mapper = new Jackson2ObjectMapperBuilder()
                        .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
                        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                        .dateFormat(new ISODateFormat())
                        .createXmlMapper(true)
                        .build();
    }

    @Override
    protected <T> String marshal(T object) throws Exception {
        StringWriter buffer = new StringWriter();
        mapper.writeValue(buffer, object);
        log.info("{} toXML: {}",object, buffer);
        return buffer.toString();    
    }

    @Override
    protected <T> T unmarshal(Class<T> type, String buffer) throws Exception {
        T result = mapper.readValue(buffer,type);
        log.info("{} fromXML: {}", buffer, result);
        return result;
    }

    private <T> T marshall_and_unmarshal(T object, Class<T> type) throws Exception {
        String jacksonXml  = marshal(object);
        T result = unmarshal(type, jacksonXml);
        return result;
    }

    @Test
    void quote_dto_marshal() throws Exception {
        // given / arrange 
        QuoteDTO quote = quoteDTOFactory.make();
        // when / act
        QuoteDTO result = marshall_and_unmarshal(quote, QuoteDTO.class);

        // then / evaluate / assert

        BDDAssertions.then(result.getId()).isEqualTo(quote.getId());
        BDDAssertions.assertThat(result.getText()).isEqualTo(quote.getText());
        BDDAssertions.assertThat(result.getDate()).isEqualTo(quote.getDate());
        BDDAssertions.assertThat(result.getAuthor()).isEqualTo(quote.getAuthor());
        BDDAssertions.assertThat(result.getIgnored()).isNull();

        log.info(" date = {} ", quote.getDate());
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;
        log.info("dtf = {} ", dtf.format(quote.getDate()));
    }

    @Test
    void quoteList_dto_marshal() throws Exception {
        // given / arrange
        QuoteListDTO quotesList = quoteDTOFactory.listBuilder().make(3,3,oneUpId);

        // when / act
        QuoteListDTO result = marshall_and_unmarshal(quotesList, QuoteListDTO.class);

        // then
        BDDAssertions.then(result.getCount()).isEqualTo(quotesList.getCount());
        Map<Integer,QuoteDTO> quoteMap = quotesList.getQuotes().stream().collect(Collectors.toMap(QuoteDTO::getId, q->q));
        for (QuoteDTO actual : quotesList.getQuotes()) {
                QuoteDTO expected  = quoteMap.get(actual.getId());
                BDDAssertions.then(expected.getId()).isEqualTo(actual.getId());
                BDDAssertions.then(expected.getText()).isEqualTo(actual.getText());
                BDDAssertions.then(expected.getAuthor()).isEqualTo(actual.getAuthor());
                BDDAssertions.then(expected.getDate()).isEqualTo(actual.getDate());
                BDDAssertions.then(expected.getIgnored()).isNull();
            
        }

    }


    @Test
    void message_dto_marshal() throws Exception {
        // given / arrange
        MessageDTO msg = MessageDTO.builder().text("a message").url("/api/msgs").build();

        // when / act
        MessageDTO result = marshall_and_unmarshal(msg, MessageDTO.class);

        // then / evaluae / assert

        BDDAssertions.then(result.getText()).isEqualTo(msg.getText());

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
    
    @Override
    protected String get_marshalled_adate(String dateText) {
        return String.format(DATES_XML,dateText);
    }


    @Override
    public String get_date(String marshalledQuote) {
        Pattern pattern = Pattern.compile(".*<date xmlns=\"\">(.+)<\\/date>.*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(marshalledQuote);

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

    
}
