package info.ejava.examples.common.exception;

import java.time.Instant;

public abstract class ServerErrorException extends RuntimeException {

    public Instant date;
    public String error;

    public ServerErrorException withDate(Instant date) {
        this.date  = date;
        return this;
    }

    public ServerErrorException withError(String error) {
        this.error = error;
        return this;
    }

    public Instant getDate(){
        return this.date;
    }

    public String getError(){
        return this.error;
    }

    public ServerErrorException(Throwable cause) {
        super(cause);
    }

    public ServerErrorException(String msg, Object... args){
        super(String.format(msg, args));
    }

    public ServerErrorException(Throwable cause, String msg, Object... args){
        super(String.format(msg, args), cause);
    }

    public static class InternalServerErrorException extends ServerErrorException {
        public InternalServerErrorException(String msg, Object... args){
            super(msg, args);
        }
        public InternalServerErrorException(Throwable cause,String msg, Object... args){
            super(String.format(msg,args), cause);
        }
    }
    
}
