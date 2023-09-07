package com.fpt.h2s.configurations;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@EnableTransactionManagement
public class DatabaseConfiguration {
    private final ConsulConfiguration consul;
    private String datasourceDriverClassName;
    private String datasourceUrl;
    private String dataSourceUsername;
    private String datasourcePassword;

    @PostConstruct
    private void postInit() {
        this.datasourceDriverClassName = this.consul.get("database.DRIVER_CLASS_NAME");
//        this.datasourceUrl = this.consul.get("database.URL").replace("54.65.18.196", "localhost");
//        this.dataSourceUsername = this.consul.get("database.USERNAME");
//        this.datasourcePassword = "12345678";

        this.datasourceUrl = this.consul.get("database.URL");
        this.dataSourceUsername = this.consul.get("database.USERNAME");
        this.datasourcePassword = this.consul.get("database.PASSWORD");
    }

    @Bean
    DataSource dataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(this.datasourceDriverClassName);
        dataSource.setUrl(this.datasourceUrl);
        dataSource.setUsername(this.dataSourceUsername);
        dataSource.setPassword(this.datasourcePassword);
        return dataSource;
    }
}
