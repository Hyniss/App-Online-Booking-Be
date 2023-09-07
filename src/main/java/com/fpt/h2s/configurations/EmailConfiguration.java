package com.fpt.h2s.configurations;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class EmailConfiguration {
    
    private String port;
    private String host;
    private String username;
    private String password;
    
    private final ConsulConfiguration consulConfiguration;
    
    @PostConstruct
    private void postInit() {
        this.port = this.consulConfiguration.get("service.email.PORT");
        this.host = this.consulConfiguration.get("service.email.HOST");
        this.username = this.consulConfiguration.get("service.email.USERNAME");
        this.password = this.consulConfiguration.get("service.email.PASSWORD");
    }
    
    @Bean
    public JavaMailSender mailSender() {
        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(this.host);
        mailSender.setPort(Integer.parseInt(this.port));
        mailSender.setUsername(this.username);
        mailSender.setPassword(this.password);
        
        final Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.enable", "true");
        
        return mailSender;
    }
    
}