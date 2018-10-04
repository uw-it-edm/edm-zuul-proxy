package edu.uw.edm.edmzuulproxy.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface UserProvisioningService {
    String provisionAcsUser(String userId) throws JsonProcessingException;
}
