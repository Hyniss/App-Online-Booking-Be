package com.fpt.h2s.configurations;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadCapability;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DatatypeFeature;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.ObjectBuffer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fpt.h2s.configurations.requests.RequestBodyExceptionContextHolder;
import com.fpt.h2s.configurations.requests.TimestampDeserializer;
import com.fpt.h2s.configurations.requests.TimestampSerializer;
import com.fpt.h2s.interceptors.RequestBodyDeserializationException;
import com.fpt.h2s.interceptors.proccessors.RequestProcessor;
import com.fpt.h2s.models.domains.OTP;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Configuration
@Log4j2
@AllArgsConstructor
public class JacksonConfiguration {

    private final List<RequestProcessor<?>> processors;

    @Bean
    public ObjectMapper getObjectMapper() {
        final DefaultDeserializationContext.Impl context = new DefaultDeserializationContext.Impl(BeanDeserializerFactory.instance);
        final ObjectMapper jsonMapper = new ObjectMapper(null, null, new DelegateDeserializationContext(context));
        jsonMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        jsonMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        jsonMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        jsonMapper.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true);
        jsonMapper.setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS);
        jsonMapper.setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS);
        jsonMapper.findAndRegisterModules();
        
        final SimpleModule module = new SimpleModule();
        module.addSerializer(Timestamp.class, new TimestampSerializer(Timestamp.class));
        module.addDeserializer(Timestamp.class, new TimestampDeserializer(Timestamp.class));
        
        module.addSerializer(OTP.class, new OTP.Serializer(OTP.class));
        module.addDeserializer(OTP.class, new OTP.Deserializer(OTP.class));
        jsonMapper.registerModules(module);
        jsonMapper.registerModules(customSerDeModule());

        return jsonMapper;
    }

    public Module customSerDeModule() {
        final var module = new SimpleModule("Custom SerDe module");
        for (RequestProcessor provider : processors) {
            log.info("Add custom serde for type '{}'", provider.getType());
            module.addSerializer(provider.getType(), provider.getJsonSerializer());
            module.addDeserializer(provider.getType(), provider.getJsonDeserializer());
        }
        return module;
    }

    private static class DelegateDeserializationContext extends DefaultDeserializationContext {
        
        @Delegate(excludes = ExcludedListMethods.class)
        private final DefaultDeserializationContext delegate;
        
        protected DelegateDeserializationContext(final DefaultDeserializationContext delegate) {
            super(delegate);
            this.delegate = delegate;
        }
        
        @Override
        public JsonDeserializer<?> handlePrimaryContextualization(final JsonDeserializer<?> jsonDeserializer, final BeanProperty prop, final JavaType type) throws JsonMappingException {
            final JsonDeserializer<?> delegateDeserializer = super.handlePrimaryContextualization(jsonDeserializer, prop, type);
            return new JsonDeserializer<>() {
                @Override
                public Object deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
                    try {
                        return delegateDeserializer.deserialize(jsonParser, deserializationContext);
                    } catch (final Exception exception) {
                        RequestBodyExceptionContextHolder.collectFailedFieldWhileDeserialization(jsonParser, exception);
                        return RequestBodyExceptionContextHolder.DEFAULT_VALUE_WHEN_DESERIALIZATION_FAILED;
                    }
                }
            };
        }
        
        @Override
        public Object readRootValue(final JsonParser parser, final JavaType valueType, final JsonDeserializer<Object> deserializer, final Object valueToUpdate) throws IOException {
            final Object deserializedObject = super.readRootValue(parser, valueType, deserializer, valueToUpdate);
            final Map<String, Throwable> exceptions = RequestBodyExceptionContextHolder.getStoredExceptions();
            if (!exceptions.isEmpty()) {
                RequestBodyExceptionContextHolder.clearExceptions();
                throw new RequestBodyDeserializationException(exceptions);
            }
            return deserializedObject;
        }
        
        @Override
        public DefaultDeserializationContext with(final DeserializerFactory deserializerFactory) {
            return new DelegateDeserializationContext(this.delegate.with(deserializerFactory));
        }
        
        @Override
        public DefaultDeserializationContext createInstance(final DeserializationConfig deserializationConfig, final JsonParser jsonParser, final InjectableValues injectableValues) {
            return new DelegateDeserializationContext(this.delegate.createInstance(deserializationConfig, jsonParser, injectableValues));
        }
        
        @Override
        public DefaultDeserializationContext createDummyInstance(final DeserializationConfig deserializationConfig) {
            return new DelegateDeserializationContext(this.delegate.createDummyInstance(deserializationConfig));
        }
        
        @SuppressWarnings("unused")
        private interface ExcludedListMethods {
            
            Object readRootValue(final JsonParser p, final JavaType valueType, final JsonDeserializer<Object> deserializer, final Object valueToUpdate);
            
            JsonDeserializer<?> handlePrimaryContextualization(final JsonDeserializer<?> deserializer, final BeanProperty prop, final JavaType type);
            
            DefaultDeserializationContext with(final DeserializerFactory deserializerFactory);
            
            DefaultDeserializationContext createInstance(final DeserializationConfig deserializationConfig, final JsonParser jsonParser, final InjectableValues injectableValues);
            
            DefaultDeserializationContext createDummyInstance(final DeserializationConfig deserializationConfig);
            
            KeyDeserializer keyDeserializerInstance(Annotated ann, Object o);
            
            Class<?> getActiveView();
            
            boolean canOverrideAccessModifiers();
            
            boolean isEnabled(MapperFeature feature);
            
            boolean isEnabled(DatatypeFeature feature);
            
            JsonFormat.Value getDefaultPropertyFormat(Class<?> baseType);
            
            AnnotationIntrospector getAnnotationIntrospector();
            
            TypeFactory getTypeFactory();
            
            boolean isEnabled(DeserializationFeature feat);
            
            boolean isEnabled(StreamReadCapability cap);
            
            int getDeserializationFeatures();
            
            boolean hasDeserializationFeatures(int featureMask);
            
            boolean hasSomeOfFeatures(int featureMask);
            
            JsonParser getParser();
            
            Object findInjectableValue(Object valueId, BeanProperty forProperty, Object beanInstance) throws JsonMappingException;
            
            Base64Variant getBase64Variant();
            
            JsonNodeFactory getNodeFactory();
            
            TokenBuffer bufferForInputBuffering();
            
            JsonDeserializer<Object> findContextualValueDeserializer(JavaType type, BeanProperty prop) throws JsonMappingException;
            
            JsonDeserializer<Object> findNonContextualValueDeserializer(JavaType type) throws JsonMappingException;
            
            JsonDeserializer<Object> findRootValueDeserializer(JavaType type) throws JsonMappingException;
            
            KeyDeserializer findKeyDeserializer(JavaType keyType, BeanProperty prop) throws JsonMappingException;
            
            JavaType constructType(Class<?> cls);
            
            ObjectBuffer leaseObjectBuffer();
            
            void returnObjectBuffer(ObjectBuffer buf);
            
            ArrayBuilders getArrayBuilders();
        }
    }
    
}
