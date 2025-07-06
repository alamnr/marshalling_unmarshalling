package info.ejava.examples.content.quotes.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import info.ejava.examples.content.quotes.util.JaxbTimeAdapters;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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

@XmlRootElement(name="quote", namespace = "urn:ejava.svc-controllers.quotes")  // JAXB
@XmlAccessorType(XmlAccessType.FIELD) // JAXB java.util.Date and java.time adapters 
@JacksonXmlRootElement(localName = "quote", namespace = "urn:ejava.svc-controllers.quotes") // Jackson xml
public class QuoteDTO {

    @XmlAttribute // JAXB
    @JacksonXmlProperty(isAttribute = true) // Jackson xml
    private int id;
    private String author;
    private String text;
    @XmlJavaTypeAdapter(JaxbTimeAdapters.LocalDateJaxbAdapter.class) 
    // JAXB local date adapters since JAXB does not have that default
    private LocalDate date;

    @JsonIgnore // Jackson
    @JsonbTransient // Json-B
    @XmlTransient // JAXB
    private String ignored;
}