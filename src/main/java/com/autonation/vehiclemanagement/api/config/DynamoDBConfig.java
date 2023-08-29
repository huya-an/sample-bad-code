package com.autonation.vehiclemanagement.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/** Provides the core functionality for Dynamo Db client configuration.
 *
 *
 */
@Configuration
@EnableAutoConfiguration
public class DynamoDBConfig {


    @Value("${amazon.aws.region}")
    private String region = "us-east-1";
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient() {
        Region region = Region.of(this.region);
        DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
        DynamoDbClient ddb = DynamoDbClient.builder().credentialsProvider(credentialsProvider).region(region).build();
        return DynamoDbEnhancedClient.builder().dynamoDbClient(ddb).build();
    }
}