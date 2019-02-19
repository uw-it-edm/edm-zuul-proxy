package edu.uw.edm.edmzuulproxy.properties;

import com.amazonaws.regions.Regions;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "uw.aws")
@Data
public class AwsProperties {

    private String amazonAWSAccessKey;
    private String amazonAWSSecretKey;


    @NotNull
    private String dynamoTableNamePrefix;

    private String dynamoDBEndpointOverride;
    private boolean dynamoDBCreateTable = false;

    private String acsUserSyncFunctionName;

    private Regions region = Regions.US_WEST_2;

    public void setRegionName(String awsRegionName) {
        if (awsRegionName != null) {
            this.region = Regions.fromName(awsRegionName);
        }
    }
}
