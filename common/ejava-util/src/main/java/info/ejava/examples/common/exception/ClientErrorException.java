package info.ejava.examples.common.exception;

import java.time.Instant;

public abstract class ClientErrorException extends RuntimeException {

    protected Instant date = Instant.now();
    protected String error;

    public ClientErrorException withDate(Instant date){
        this.date = date;
        return this;
    }

    public ClientErrorException withError(String error){
        this.error = error;
        return this;
    }

    public Instant getDate() {
        return date;
    }


    public String getError(){
        return error;
    }


    public ClientErrorException(Throwable cause){
        super(cause);
    }

    public ClientErrorException(String msg, Object... args){
        super(String.format(msg, args));
    }

    public ClientErrorException(Throwable cause, String msg, Object... args) {
        super(String.format(msg, args), cause);
    }

    public static class BadRequestException extends ClientErrorException {

        public BadRequestException(String msg, Object... args) {
            super(msg, args);
            
        }

        public BadRequestException(Throwable cause , String msg, Object... args) {
            super(cause,msg,args);
        }

        public static class NotFoundException extends ClientErrorException {
            public NotFoundException(String msg, Object... args){
                super(msg, args);
            }
            public NotFoundException(Throwable cause,String msg,Object... args){
                super(cause,msg,args);
            }
        }

        public static class InvalidInputException extends ClientErrorException {
            public InvalidInputException(String message, Object...args) {  super(message, args); }
            public InvalidInputException(Throwable cause, String message, Object...args) { super(cause, message, args); }
        }

        public static class NotAuthorizedException extends ClientErrorException {
            public NotAuthorizedException(String message, Object...args) {  super(message, args); }
            public NotAuthorizedException(Throwable cause, String message, Object...args) { super(cause, message, args); }
        }

        // 400 - Bad Request
        // 404 - Not Found
        // 422 - Unprocessable Entity / Invalid Input
        // 401 - Not Authorized
        // 403 - Forbidden
        // 405 - Unsupprted Media Type
        // 415 - Not Acceptable
        
    }



}