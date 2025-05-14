package info.ejava.examples.content.quotes.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import info.ejava.examples.common.time.ISODateFormat;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

public abstract class JsonbTimeSerializers<T> implements JsonbSerializer<T> {

    protected abstract String doFormat(DateTimeFormatter dtf, T timeStamp);

    @Override
    public void serialize(T timeStamp, JsonGenerator generator, SerializationContext ctx) {
        generator.write(doFormat(ISODateFormat.MARSHALER, timeStamp));
        
    }

    /*
     * Need a simple , first serializer here
     */

    public static class DateJsonbSerializer implements JsonbSerializer<Date> {

        @Override
        public void serialize(Date date, JsonGenerator generator, SerializationContext ctx) {
             generator.write(DateTimeFormatter.ISO_INSTANT.format(date.toInstant()));
        }

    }

    public static class InstantJsonbSerializer extends JsonbTimeSerializers<Instant>{

        @Override
        protected String doFormat(DateTimeFormatter dtf, Instant timeStamp) {
            return dtf.format(timeStamp);
        }
        
    }

    public static class LocalDateTimeJsonbSerializer extends JsonbTimeSerializers<LocalDateTime> {

        @Override
        protected String doFormat(DateTimeFormatter dtf, LocalDateTime timeStamp) {
            return dtf.format(timeStamp);
        }
        
    }

    public static class ZonedDateTimeJsonbSerializer extends JsonbTimeSerializers<ZonedDateTime> {

        @Override
        protected String doFormat(DateTimeFormatter dtf, ZonedDateTime timeStamp) {
            return dtf.format(timeStamp);
        }

    }

    public static class OffsetDateTimeJsonbSerializer extends JsonbTimeSerializers<OffsetDateTime>{

        @Override
        protected String doFormat(DateTimeFormatter dtf, OffsetDateTime timeStamp) {
            return dtf.format(timeStamp);
        }

        
    }

    /*
    public static class DateJsonbSerializer extends JsonbTimeSerializers<Date> {
        @Override
        protected String doFormat(DateTimeFormatter dtf, Date timestamp) {
            return dtf.format(OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneOffset.UTC));
        }
    }
*/

}
