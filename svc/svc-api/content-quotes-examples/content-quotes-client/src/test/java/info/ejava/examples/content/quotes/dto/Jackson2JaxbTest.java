package info.ejava.examples.content.quotes.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Jackson2JaxbTest extends InteropTestBase {
    
    private JacksonXmlTest jacksonXmlTest = new JacksonXmlTest();
    private JaxbTest jaxbTest = new JaxbTest();

    @BeforeEach
    public void init(){
        jacksonXmlTest.init();
        jaxbTest.init();
    }

    @ParameterizedTest
    @MethodSource("dtos")
    void jacksonXml2JaxbObj(Object dto) throws Exception {
        String jacksonXml = jacksonXmlTest.marshal(dto);
        
        Object jaxbObjectFromJacjsonXml = jaxbTest.unmarshal(dto.getClass(), jacksonXml) ;
        
        Assertions.assertThat(jaxbObjectFromJacjsonXml).isEqualTo(dto);

        if(dto instanceof ADate){
            compareTimes((ADate)dto, (ADate)jaxbObjectFromJacjsonXml);
        }
    }

    @ParameterizedTest
    @MethodSource("dtos")
    void jaxbXml2JacksonObj(Object dto) throws Exception {
        String jaxbXml = jaxbTest.marshal(dto);
        Object jacksonObjectFromJaxbXml = jacksonXmlTest.unmarshal(dto.getClass(), jaxbXml);
        Assertions.assertThat(jacksonObjectFromJaxbXml).isEqualTo(dto);

        if(dto instanceof ADate){
            compareTimes((ADate)dto, (ADate)jacksonObjectFromJaxbXml);
        }
    }
}
