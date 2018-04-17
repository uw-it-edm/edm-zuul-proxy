package edu.uw.edm.edmzuulproxy.service.impl;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.AWSLambdaException;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.uw.edm.edmzuulproxy.properties.AwsProperties;
import edu.uw.edm.edmzuulproxy.properties.SecurityProperties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserProvisioningServiceImplTest {

    @Mock
    AwsProperties awsProperties;

    @Mock
    SecurityProperties securityProperties;

    @Mock
    AWSLambda awsLambdaClient;

    UserProvisioningServiceImpl userProvisioningService;

    @Before
    public void setUp() {
        this.userProvisioningService = new UserProvisioningServiceImpl(awsProperties, securityProperties, awsLambdaClient);
        when(securityProperties.getAuthenticationHeaderName()).thenReturn("auth-header");
    }

    @Test
    public void provisionValidAcsUser() throws JsonProcessingException {
        InvokeResult mockInvokeResult = mock(InvokeResult.class);
        when(mockInvokeResult.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(awsLambdaClient.invoke(any())).thenReturn(mockInvokeResult);

        String testUser = "testUser";
        String result = userProvisioningService.provisionAcsUser(testUser);

        verify(awsLambdaClient, times(1)).invoke(any());
        assertThat(result, is(equalTo(testUser)));
    }

    @Test(expected = AWSLambdaException.class)
    public void provisionInvalidAcsUser() throws JsonProcessingException {
        InvokeResult mockInvokeResult = mock(InvokeResult.class);
        when(mockInvokeResult.getStatusCode()).thenReturn(HttpStatus.SC_UNPROCESSABLE_ENTITY);
        when(awsLambdaClient.invoke(any())).thenReturn(mockInvokeResult);

        String testUser = "testUser";
        String result = userProvisioningService.provisionAcsUser(testUser);

        verify(awsLambdaClient, times(1)).invoke(any());
        assertThat(result, is(equalTo(testUser)));
    }
}
