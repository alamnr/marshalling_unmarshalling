package info.ejava.examples.content.quotes.dto;

import static info.ejava.examples.content.quotes.util.QuoteDTOFactory.oneUpId;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JaxbTest extends MarshallingTestBase {

    
    public void init(){

    }
    @Override
    protected <T> String marshal(T object) throws Exception {
        if(object == null){
            return "";
        }
        JAXBContext jbx =JAXBContext.newInstance(object.getClass());

        Marshaller marshaller = jbx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter buffer = new StringWriter();
        marshaller.marshal(object, buffer);
        log.info("{} toXML {} : ", object, buffer);
        return buffer.toString();
    }

    @Override
    protected <T> T unmarshal(Class<T> type, String buffer) throws Exception {
        if(buffer == null) {
            return null;
        }

        JAXBContext jbx =JAXBContext.newInstance(type);
        Unmarshaller unmarshaller = jbx.createUnmarshaller();

        ByteArrayInputStream bis = new ByteArrayInputStream(buffer.getBytes(StandardCharsets.UTF_8));
        Object obj = unmarshaller.unmarshal(bis);
        T result = type.cast(obj);
        log.info("{} fromXML {} : ", buffer, result);
        return result;

    }


    private <T> T marshall_and_unmarshall(T object,Class<T> type) throws Exception {

        String jaxbXml = marshal(object);
        T result = unmarshal(type, jaxbXml);
        return result;
    }

    @Test
    void quote_dto_marshall() throws Exception {
        // given / arrange
        QuoteDTO quoteDTO = quoteDTOFactory.make();

        // when / act
        QuoteDTO result =  marshall_and_unmarshall(quoteDTO, QuoteDTO.class);

        // then 
        BDDAssertions.then(result.getDate()).isEqualTo(quoteDTO.getDate());
        BDDAssertions.then(result.getText()).isEqualTo(quoteDTO.getText());
        BDDAssertions.then(result.getAuthor()).isEqualTo(quoteDTO.getAuthor());
        BDDAssertions.then(result.getIgnored()).isNull();
    }

    @Test
    void quoteList_dto_marshal() throws Exception {

        // given / arrange
        QuoteListDTO quoteList = quoteDTOFactory.listBuilder().make(3, 3, oneUpId);

        // when / act
        QuoteListDTO resultList = marshall_and_unmarshall(quoteList, QuoteListDTO.class);

        // then / evaluate / assert
        BDDAssertions.then(resultList.getCount()).isEqualTo(quoteList.getCount());
        Map<Integer,QuoteDTO> quoteMap = quoteList.getQuotes().stream().collect(Collectors.toMap(QuoteDTO::getId, q->q));
        for (QuoteDTO actualDto : quoteList.getQuotes()) {
                QuoteDTO expected = quoteMap.get(actualDto.getId());
                BDDAssertions.then(expected.getDate()).isEqualTo(actualDto.getDate());
                BDDAssertions.then(expected.getText()).isEqualTo(actualDto.getText());
                BDDAssertions.then(expected.getAuthor()).isEqualTo(actualDto.getAuthor());
                BDDAssertions.then(expected.getIgnored()).isNull();
        }
    }

    @Test
    void msg_dto_marshal() throws Exception {
        // given / arrange
        MessageDTO msg = MessageDTO.builder().text("a msg").url("/api/msgs").build();

        // when / act
        MessageDTO result = marshall_and_unmarshall(msg, MessageDTO.class);

        // then / evaluate / assert
        BDDAssertions.then(result.getText()).isEqualTo(msg.getText());
        BDDAssertions.then(result.getUrl()).isEqualTo(msg.getUrl());
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
        Pattern pattern = Pattern.compile(".*<date>(.+)<\\/date>.*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(marshalledQuote);

        if (matcher.matches()) {
            String date = matcher.group(1);
            return date;
        }
        return null;
    }

    @Override
    protected boolean canParseFormat(String format, ZoneOffset tzo) {
        //can parse Z and +05:00 but cannot parse +05
//        if (format.equals(ISO_8601_DATETIME5_FORMAT) ||
//                (tz!=null && tz.getID().equals(TimeZone.getTimeZone("UTC").getID()))) {
//            return true;
//        }
//        return false;
        return true;
    }    
}
