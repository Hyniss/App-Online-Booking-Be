package com.fpt.h2s;

import com.fpt.h2s.models.entities.converters.JPASqlLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class H2sApplication {
    
    public static void main(final String[] args) {
        H2sApplication.defineSystemEnv();
        SpringApplication.run(H2sApplication.class, args);
    }
    
    public static void defineSystemEnv() {
        System.setProperty("server.port", "${API_PORT:52291}");
        System.setProperty("spring.main.banner-mode", "off");
        System.setProperty("spring.application.name", "Home2Stay");
        System.setProperty("spring.cloud.consul.host", "${CONSUL_HOST:13.250.138.136}");
        System.setProperty("spring.cloud.consul.port", "${CONSUL_PORT:8500}");
        System.setProperty("spring.cloud.consul.config.name", "${CONSUL_NAME:h2s}");
        System.setProperty("spring.cloud.consul.config.acl-token", "${CONSUL_ACL_TOKEN:0bc6bc46-f25e-4262-b2d9-ffbe1d96be6f}");
        System.setProperty("spring.cloud.consul.discovery.acl-token", "${CONSUL_ACL_TOKEN:0bc6bc46-f25e-4262-b2d9-ffbe1d96be6f}");
        System.setProperty("spring.cloud.consul.config.prefixes", "${CONSUL_PREFIX:config}");
        System.setProperty("spring.cloud.consul.config.enabled", "true");
        System.setProperty("spring.cloud.consul.config.import-check.enabled", "false");
        System.setProperty("spring.cloud.consul.discovery.instanceId", "${spring.application.name}:${random.value}");
        System.setProperty("spring.data.redis.repositories.enabled", "false");
        System.setProperty("spring.main.allow-bean-definition-overriding", "true");
        System.setProperty("decorator.datasource.p6spy.enable-logging", "${SHOW_SQL:false}");
        System.setProperty("decorator.datasource.p6spy.logging", "custom");
        System.setProperty("decorator.datasource.p6spy.custom-appender-class", JPASqlLogger.class.getName());
        System.setProperty("decorator.datasource.p6spy.tracing.include-parameter-values", "true");
        System.setProperty("spring.servlet.multipart.max-file-size", "128MB");
        System.setProperty("spring.servlet.multipart.max-request-size", "128MB");
    }
    
}
