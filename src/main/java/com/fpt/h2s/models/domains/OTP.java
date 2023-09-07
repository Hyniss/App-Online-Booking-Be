package com.fpt.h2s.models.domains;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fpt.h2s.utilities.MoreStrings;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public class OTP {

    private static final int OTP_LENGTH = 6;

    private final String value;


    public OTP() {
        this.value = "";
    }

    public OTP(@Nullable final String value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return this.value == null || this.value.isBlank();
    }

    public String getValue() {
        return this.value;
    }

    public static OTP generate() {
        final String value = MoreStrings.randomOTP(OTP_LENGTH);
        return new OTP(value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this.isEmpty()) {
            return false;
        }
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof OTP other) {
            return Objects.equals(this.value, other.value);
        }
        if (obj instanceof String other) {
            return Objects.equals(this.value, other);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

    @Override
    public String toString() {
        if (this.isEmpty()) {
            throw new IllegalArgumentException("Please fill OTP");
        }
        return this.value;
    }

    public static class Serializer extends StdSerializer<OTP> {

        public Serializer(final Class<OTP> t) {
            super(t);
        }
        @Override
        public void serialize(final OTP otp, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(otp.value);
        }
    }

    public static class Deserializer extends StdDeserializer<OTP> {

        public Deserializer(final Class<?> vc) {
            super(vc);
        }
        @Override
        public OTP deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JacksonException {
            return new OTP(jsonParser.getText());
        }
    }
}