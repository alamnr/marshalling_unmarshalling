package info.ejava.examples.svc.authz.authorities;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
public class HttpToHttpsRedirectConfiguraion {
    
    /**
	 * The following set of re-direct snippets are from
	 * http://zetcode.com/springboot/https/[Spring Boot HTTPS example]
	 */
	// @Bean
	// @Profile("redirect") //only enable on demand
	// public ServletWebServerFactory servletContainer() {
	// 	TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
	// 		@Override
	// 		protected void postProcessContext(Context context) {
	// 			SecurityConstraint securityConstraint = new SecurityConstraint();
	// 			securityConstraint.setUserConstraint("CONFIDENTIAL");

	// 			SecurityCollection collection = new SecurityCollection();
	// 			collection.addPattern("/*");
	// 			securityConstraint.addCollection(collection);
	// 			context.addConstraint(securityConstraint);
	// 		}
	// 	};

	// 	tomcat.addAdditionalTomcatConnectors(redirectConnector());
	// 	return tomcat;
	// }

	// private Connector redirectConnector() {
	// 	Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
	// 	connector.setScheme("http");
	// 	connector.setPort(8080);
	// 	connector.setSecure(false);
	// 	connector.setRedirectPort(8443);
	// 	return connector;
	// }

	
	@Bean
	@Profile("redirect") //only enable on demand
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> redirectConnector() {
        return factory -> {
            Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setScheme("http");
            connector.setPort(8080);
            connector.setSecure(false);
            connector.setRedirectPort(8443); // redirect to HTTPS
            factory.addAdditionalTomcatConnectors(connector);
        };
    }
}
