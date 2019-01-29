package edu.uw.edm.edmzuulproxy.config.aws;

import com.google.common.base.Strings;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import edu.uw.edm.edmzuulproxy.certificateauthorizer.model.CertificateAuthorization;
import edu.uw.edm.edmzuulproxy.properties.AwsProperties;

/**
 * @author Maxime Deravet Date: 10/4/18
 */
@Configuration
@EnableDynamoDBRepositories(
        dynamoDBMapperConfigRef = "dynamoDBMapperConfig",
        basePackages = "edu.uw.edm.edmzuulproxy.certificateauthorizer")
public class DynamoDBConfig {


    @Bean
    public AmazonDynamoDB amazonDynamoDB(AwsProperties awsProperties) {

        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder
                .standard();


        if (!Strings.isNullOrEmpty(awsProperties.getAmazonAWSAccessKey()) && !Strings.isNullOrEmpty(awsProperties.getAmazonAWSSecretKey())) {
            builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsProperties.getAmazonAWSAccessKey(), awsProperties.getAmazonAWSSecretKey())));
        }

        if (!Strings.isNullOrEmpty(awsProperties.getDynamoDBEndpointOverride())) {

            AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(awsProperties.getDynamoDBEndpointOverride(), "dev");

            builder.withEndpointConfiguration(endpointConfiguration);
        } else {
            builder.withRegion(awsProperties.getRegion());
        }

        AmazonDynamoDB amazonDynamoDB = builder.build();


        if (isWorkstationEnvironment(awsProperties)) {

            Optional<String> table = amazonDynamoDB.listTables().getTableNames().stream().
                    filter(tableName -> tableName.equals(CertificateAuthorization.TABLE_NAME)).findFirst();

            if (!table.isPresent()) {
                amazonDynamoDB.createTable(new DynamoDBMapper(amazonDynamoDB)
                        .generateCreateTableRequest(CertificateAuthorization.class)
                        .withProvisionedThroughput(new ProvisionedThroughput(1000L, 1000L)));
            }


        }

        return amazonDynamoDB;
    }

    private boolean isWorkstationEnvironment(AwsProperties awsProperties) {
        return !Strings.isNullOrEmpty(awsProperties.getDynamoDBEndpointOverride()) && awsProperties.isDynamoDBCreateTable();
    }

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig(DynamoDBMapperConfig.TableNameOverride tableNameOverrider, AwsProperties awsProperties) {

        DynamoDBMapperConfig.Builder builder = new DynamoDBMapperConfig.Builder();

        if (!isWorkstationEnvironment(awsProperties)) {
            builder.setTableNameOverride(tableNameOverrider);
        }

        // Sadly this is a @deprecated method but new DynamoDBMapperConfig.Builder() is incomplete compared to DynamoDBMapperConfig.DEFAULT
        return new DynamoDBMapperConfig(DynamoDBMapperConfig.DEFAULT, builder.build());
    }

    @Bean
    public DynamoDBMapperConfig.TableNameOverride tableNameOverrider(AwsProperties awsProperties) {
        String prefix = awsProperties.getDynamoTableNamePrefix(); // Use @Value to inject values via Spring or use any logic to define the table prefix
        return DynamoDBMapperConfig.TableNameOverride.withTableNamePrefix(prefix);
    }


}
