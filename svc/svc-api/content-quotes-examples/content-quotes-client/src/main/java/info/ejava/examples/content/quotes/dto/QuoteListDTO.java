package info.ejava.examples.content.quotes.dto;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Builder

@XmlRootElement(name = "quotes", namespace = "urn:ejava.svc-controllers.quotes")  // JAXB
@XmlType() // JAXB
@XmlAccessorType(XmlAccessType.NONE) // JAXB

@JacksonXmlRootElement(localName = "quotes", namespace = "urn:ejava.svc-controllers.quotes") // JACKSON
public class QuoteListDTO {

    @XmlAttribute(required = false) // JAXB
    private Integer offset;

    @XmlAttribute(required = false) // JAXB
    private Integer limit;

    @XmlAttribute(required = false) // JAXB
    private Integer total;

    @XmlAttribute(required = false) // JAXB
    private String keywords;

    @XmlElementWrapper(name = "quotes") // JAXB
    @XmlElement(name = "quote") // JAXB

    @JacksonXmlElementWrapper(localName = "quotes") // Jackson
    @JacksonXmlProperty(localName = "quote") // Jackson
    private List<QuoteDTO> quotes;

    @XmlAttribute(required = false) // JAXB
    public int getCount(){
        return quotes == null ? 0 : quotes.size(); 
    }

    public void setCount(Integer count){
        // ignored - count is determined from quotes.size
    }
    
}
