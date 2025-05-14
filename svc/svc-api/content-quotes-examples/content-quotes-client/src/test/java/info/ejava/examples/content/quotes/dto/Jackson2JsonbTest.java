package info.ejava.examples.content.quotes.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Jackson2JsonbTest extends InteropTestBase {
    
    private JacksonJsonTest jacksonJsonTest = new JacksonJsonTest();
    private JsonbTest jsonbTest = new JsonbTest();

    @BeforeEach
    public void init() {
        jacksonJsonTest.init();
        jsonbTest.init();
    }

    @ParameterizedTest
    @MethodSource("dtos")
    void jackson2jsonbTest(Object dto) throws Exception {
        
        // when 
        String jacksonJson = jacksonJsonTest.marshal(dto);
        Object jsonbObjectFromJacsonJson = jsonbTest.unmarshal(dto.getClass(), jacksonJson);
        Assertions.assertThat(jsonbObjectFromJacsonJson).isEqualTo(dto);

        if(dto instanceof ADate){
            compareTimes((ADate)dto, (ADate)jsonbObjectFromJacsonJson);
        }
        
    }

    @ParameterizedTest
    @MethodSource("dtos")
    void jsob2jacsonTest(Object dto) throws Exception {

        // when / act
        String jsonbJson = jsonbTest.marshal(dto);
        Object jacksonObjectFromJsonbXml = jacksonJsonTest.unmarshal(dto.getClass(), jsonbJson);

        // then / assert
        Assertions.assertThat(jacksonObjectFromJsonbXml).isEqualTo(dto);

        if(dto instanceof ADate) {
            compareTimes((ADate)dto, (ADate)jacksonObjectFromJsonbXml);
        }

    }
}


