
/*
 * JAXB Initialization
    There is no sharable, up-front initialization for JAXB. All configuration must be done on individual,
    non-sharable JAXBContext objects. However, JAXB does have a package-wide annotation that the
    other frameworks do not. The following example shows a package-info.java file that contains
    annotations to be applied to every class in the same Java package.
 */
@XmlSchema(namespace = "urn:ejava.svc-controllers.quotes")
// adapters can be applied globally at the package level
//@XmlJavaTypeAdapter(JaxbTimeAdapters.LocalDateJaxbAdapter.class) // JAXB local date adapters since JAXB does not hav that default

package info.ejava.examples.content.quotes.dto;

//import info.ejava.examples.content.quotes.util.JaxbTimeAdapters;
import jakarta.xml.bind.annotation.XmlSchema;
//import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;