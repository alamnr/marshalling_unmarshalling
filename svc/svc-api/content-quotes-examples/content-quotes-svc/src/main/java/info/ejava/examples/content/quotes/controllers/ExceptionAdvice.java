package info.ejava.examples.content.quotes.controllers;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import info.ejava.examples.common.exception.ClientErrorException;
import info.ejava.examples.common.exception.ClientErrorException.InvalidInputException;
import info.ejava.examples.common.exception.ClientErrorException.NotFoundException;
import info.ejava.examples.common.exception.ServerErrorException.InternalServerErrorException;
import info.ejava.examples.content.quotes.dto.MessageDTO;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;

/*
 * This classprovides custom error handling for exception thrown by the 
 * controller. It is one of several techniques offered by Spring, selected 
 * primarily because we retain full control over the response and response headers 
 * returned to the caller
 */

 @Hidden
 @RestControllerAdvice
 @Slf4j
public class ExceptionAdvice {
    
    protected ResponseEntity<MessageDTO> buildResponse(HttpStatus status, String text) {
        String url = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
        MessageDTO message = MessageDTO.builder().url(url).text(text).build();
        ResponseEntity<MessageDTO> response = ResponseEntity.status(status).body(message);
        return response;
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<MessageDTO> handle(NotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND,  ex.getMessage());
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<MessageDTO> handle(InvalidInputException ex){
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ClientErrorException.BadRequestException.class)
    public ResponseEntity<MessageDTO> handle(ClientErrorException.BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<MessageDTO> handle(InternalServerErrorException ex) {
        log.warn("{}  , exception trace - {}", ex.getMessage(),ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageDTO> handle(RuntimeException ex){
        log.warn("ex message - {}, exception trace - {}", ex.getMessage(), ex);
        String text = String.format("Unexpected error executing  request : %s", ex.toString());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, text);
    }

}
