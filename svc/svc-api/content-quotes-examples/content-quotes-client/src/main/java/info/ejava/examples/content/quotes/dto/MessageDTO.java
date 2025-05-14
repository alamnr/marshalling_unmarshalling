package info.ejava.examples.content.quotes.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlRootElement(name = "message", namespace = "urn:ejava.svc-controllers.quotes") // JAXB
@JacksonXmlRootElement(localName = "message", namespace = "urn:ejava.svc-controllers.quotes") // JACKSON
public class MessageDTO {

    private String url;
    private String text;
    
}
