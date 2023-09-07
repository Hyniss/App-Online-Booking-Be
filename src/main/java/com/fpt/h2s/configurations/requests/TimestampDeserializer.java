package com.fpt.h2s.configurations.requests;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.SneakyThrows;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampDeserializer extends StdDeserializer<Timestamp> {
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    
    public TimestampDeserializer(final Class<?> vc) {
        super(vc);
    }
    
    @Override
    @SneakyThrows
    public Timestamp deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) {
        final String text = jsonParser.getText();
        try {
            return Timestamp.valueOf(text);
        } catch (final Exception ignored) {
        }
        
        try {
            final Date value = TimestampDeserializer.SIMPLE_DATE_FORMAT.parse(text);
            return new Timestamp(value.getTime());
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
