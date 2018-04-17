package edu.uw.edm.edmzuulproxy.service.impl;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.AWSLambdaException;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import edu.uw.edm.edmzuulproxy.model.AcsUserSyncInput;
import edu.uw.edm.edmzuulproxy.properties.AwsProperties;
import edu.uw.edm.edmzuulproxy.properties.SecurityProperties;
import edu.uw.edm.edmzuulproxy.service.UserProvisioningService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserProvisioningServiceImpl implements UserProvisioningService {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AwsProperties awsProperties;
    private SecurityProperties securityProperties;
    private AWSLambda awsLambdaClient;


    @Autowired
    public UserProvisioningServiceImpl(AwsProperties awsProperties, SecurityProperties securityProperties, AWSLambda awsLambdaClient) {
        this.awsProperties = awsProperties;
        this.securityProperties = securityProperties;
        this.awsLambdaClient = awsLambdaClient;
    }

    @Override
    @Cacheable("acs-users")
    public String provisionAcsUser(String userId) throws JsonProcessingException {
        log.debug("Provision ACS User: {} ", userId);

        final AcsUserSyncInput acsUserSyncInput = new AcsUserSyncInput(securityProperties.getAuthenticationHeaderName(), userId);
        final String payload = OBJECT_MAPPER.writeValueAsString(acsUserSyncInput);
        final InvokeRequest req = new InvokeRequest()
                .withFunctionName(awsProperties.getAcsUserSyncFunctionName())
                .withPayload(payload);
        final InvokeResult result = awsLambdaClient.invoke(req);

        if (HttpStatus.SC_OK == result.getStatusCode()) {
            return userId;
        } else {
            throw new AWSLambdaException("Error Provisioning ACS User. Status-code: " + result.getStatusCode() + ", function-error: " + result.getFunctionError());
        }
    }
}
