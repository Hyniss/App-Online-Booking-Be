package com.fpt.h2s;

import com.fpt.h2s.interceptors.ApiInterceptor;
import com.fpt.h2s.interceptors.proccessors.RequestProcessor;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@Log4j2
@AllArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {

    private final List<ApiInterceptor> interceptors;
    private final List<RequestProcessor<?>> processors;

    @Override
    public void addInterceptors(@NonNull final InterceptorRegistry registry) {
        this.interceptors.forEach(registry::addInterceptor);
    }

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("*").allowedOrigins("http://localhost:3000")
            .allowCredentials(true);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        for (RequestProcessor<?> provider : processors) {
            log.info("Add custom formatter for field type '{}'", provider.getType());
            registry.addFormatterForFieldType(provider.getType(), provider.getTypedFieldFormatter());
        }
    }
}
