package com.fpt.h2s.configurations;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.context.annotation.Configuration;
import org.webjars.NotFoundException;

import java.util.Base64;
import java.util.HashMap;

@Configuration
public class ConsulConfiguration {
    
    private final HashMap<String, String> consulKvMap;
    
    public ConsulConfiguration(final ConsulClient client) {
        this.consulKvMap = new HashMap<>();
        final String consulPrefix = System.getenv("CONSUL_PREFIX");
        final String keyPrefix = consulPrefix + "/" + System.getenv("CONSUL_NAME") + "/";
        final Base64.Decoder decoder = Base64.getDecoder();
        
        client.getKVValues(consulPrefix).getValue().forEach(kv -> {
            try {
                final String key = kv.getKey().replace(keyPrefix, "").replace("/", ".");
                final String value = new String(decoder.decode(kv.getValue()));
                this.consulKvMap.put(key, value);
            } catch (final Exception ignored) {
            }
        });
    }
    
    public String get(final String key) {
        if (this.consulKvMap.containsKey(key)) {
            return this.consulKvMap.get(key);
        }
        throw new NotFoundException("Key %s not found".formatted(key));
    }
    
    
}
