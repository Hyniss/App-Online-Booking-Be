package com.fpt.h2s.configurations;

import com.fpt.h2s.utilities.MoreRequests;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import java.util.Locale;

@Configuration
public class LocaleConfiguration {
    @Bean(name = "localeResolver")
    public LocaleResolver getLocaleResolver()  {
        return new SmartLocaleResolver();
    }

    @Bean(name = "messageSource")
    public MessageSource getMessageResource()  {
        ReloadableResourceBundleMessageSource messageResource= new ReloadableResourceBundleMessageSource();
        messageResource.setBasename("classpath:lang/messages");
        messageResource.setDefaultEncoding("UTF-8");
        return messageResource;
    }

    @Bean
    public LocalValidatorFactoryBean getValidator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

    public static class SmartLocaleResolver extends AcceptHeaderLocaleResolver {
        @Override
        public @NotNull Locale resolveLocale(@NotNull HttpServletRequest request) {
            try {
                HttpServletRequest httpRequest = MoreRequests.getCurrentHttpRequest();
                return Locale.forLanguageTag(httpRequest.getHeader("X-LOCALE"));
            } catch (Exception e) {
                return Locale.forLanguageTag("en");
            }
        }
    }
}
