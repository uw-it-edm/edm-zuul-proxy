package edu.uw.edm.edmzuulproxy.certificateauthorizer.service;

import org.springframework.http.HttpMethod;

import java.util.List;

import edu.uw.edm.edmzuulproxy.security.User;

/**
 * @author Maxime Deravet Date: 2019-01-28
 */
public interface CertificateAuthorizerService {

    boolean isAllowedForUri(String certificateName, HttpMethod httpMethod, String uri , User user);

    void addNewAuthorization(String certificateName, String uriRegex, List<String> httpMethods, List<String> uwGroups, List<String> contactEmail, String notes);
}
