package com.fpt.h2s.configurations.requests;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class TimestampSerializer extends StdSerializer<Timestamp> {
    
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    
    public TimestampSerializer(final Class<Timestamp> t) {
        super(t);
    }
    
    @Override
    public void serialize(final Timestamp timestamp, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(TimestampSerializer.SIMPLE_DATE_FORMAT.format(timestamp));
    }
}
