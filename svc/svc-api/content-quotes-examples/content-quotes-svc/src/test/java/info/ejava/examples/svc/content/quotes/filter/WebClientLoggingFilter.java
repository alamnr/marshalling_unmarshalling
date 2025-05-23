package info.ejava.examples.svc.content.quotes.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientLoggingFilter {
    public static ExchangeFilterFunction requestFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request->{
            if (log.isDebugEnabled()) {
                String message = String.format("\n%s %s\nsent: %s",
                        request.method(),
                        request.url(),
                        request.headers());
                log.debug(message);
            }
            return Mono.just(request);
        });
    }

    public static ExchangeFilterFunction responseFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(response->{
            if (log.isDebugEnabled()) {
                String message = String.format("\n%s/%s\nrcvd: %s",
                    HttpStatus.resolve(response.statusCode().value()),
                    response.statusCode().value(),
                    response.headers().asHttpHeaders());
                log.debug(message);
            }
            return Mono.just(response);
        });
    }
}
