package edu.uw.edm.edmzuulproxy.config.aws;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import edu.uw.edm.edmzuulproxy.properties.AwsProperties;

@Configuration
public class AwsConfiguration {
    @Bean
    public AWSLambda awsLambdaClient(AwsProperties awsProperties) {
        AWSLambdaClientBuilder builder = AWSLambdaClientBuilder.standard()
                .withRegion(awsProperties.getRegion());
        AWSLambda client = builder.build();
        return client;
    }
}
