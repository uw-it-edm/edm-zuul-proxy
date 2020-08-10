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

    /**
     * Retrieves a list of distinct profiles names (lower cased) that the given certificate is authorized to access for the given uri.
     * @param certificateName Certificate name that is attempting to access the given uri.
     * @param uri Uri that is attempted to access.
     * @return List of profiles names that are authorized.
     */
    List<String> getAuthorizedProfilesForUri(String certificateName, String uri);
}
