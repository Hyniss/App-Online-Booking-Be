package com.fpt.h2s.configurations;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class AmazonConfiguration {
    
    private final ConsulConfiguration consul;
    
    @Bean
    public AmazonS3 s3Client() {
        final String accessKey = this.consul.get("service.amazon.s3.AMAZON_ACCESS_KEY");
        final String secretKey = this.consul.get("service.amazon.s3.AMAZON_SECRET_KEY");
        final String url = this.consul.get("service.amazon.s3.AMAZON_ENDPOINT_URL");
        final String region = this.consul.get("service.amazon.s3.AMAZON_REGION");
        final AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        final AwsClientBuilder.EndpointConfiguration endPoint = new AwsClientBuilder.EndpointConfiguration(url, region);
        final AWSStaticCredentialsProvider credential = new AWSStaticCredentialsProvider(credentials);
        return AmazonS3ClientBuilder.standard().withEndpointConfiguration(endPoint).withCredentials(credential).build();
    }
}
