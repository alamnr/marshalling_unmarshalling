package info.ejava.examples.content.quotes.util;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class JsonUtil extends DtoUtil {
    protected static final JsonUtil instance = new JsonUtil();
    protected final ObjectMapper mapper;

    public JsonUtil() {
        mapper = new ObjectMapper();
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleModule dateSerialization = new SimpleModule();
        dateSerialization.addDeserializer(Date.class, DateDeserializers.DateDeserializer.instance);
        dateSerialization.addSerializer(Date.class, new DateSerializer(false, sdf));

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule());

        mapper.registerModule(dateSerialization);
        init(); 
        
    }
    public JsonUtil(ObjectMapper mapper) {
        this.mapper = mapper;
        init(); 
    }

   

    @Override
    public <T> void marshalThrows(T object, OutputStream os) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(os, object);
    }

    @Override
    public <T> T unmarshalThrows(InputStream is, Class<T> type) throws IOException {
        return mapper.readValue(is, type);
    }

    public static JsonUtil instance() { return instance; }

}

