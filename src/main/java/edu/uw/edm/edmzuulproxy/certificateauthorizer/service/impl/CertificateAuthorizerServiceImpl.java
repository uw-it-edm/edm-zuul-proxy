package edu.uw.edm.edmzuulproxy.certificateauthorizer.service.impl;

import com.google.common.base.Preconditions;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

import edu.uw.edm.edmzuulproxy.certificateauthorizer.CertificateAuthorizationRepository;
import edu.uw.edm.edmzuulproxy.certificateauthorizer.model.CertificateAuthorization;
import edu.uw.edm.edmzuulproxy.certificateauthorizer.service.CertificateAuthorizerService;
import edu.uw.edm.edmzuulproxy.security.User;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Maxime Deravet Date: 2019-01-28
 */
@Service
@Slf4j
public class CertificateAuthorizerServiceImpl implements CertificateAuthorizerService {
    public static final String HTTP_METHOD_WILDCARD = "*";
    public static final String UW_GROUPS_WILDCARD = "*";
    public static final String DEFAULT_SEPARATOR = ",";
    public static final String UW_GROUPS_SEPARATOR = DEFAULT_SEPARATOR;
    public static final String HTTP_METHODS_SEPARATOR = DEFAULT_SEPARATOR;
    public static final String CONTACT_EMAILS_SEPARATOR = DEFAULT_SEPARATOR;


    private CertificateAuthorizationRepository certificateAuthorizationRepository;

    @Autowired
    public CertificateAuthorizerServiceImpl(CertificateAuthorizationRepository certificateAuthorizationRepository) {
        this.certificateAuthorizationRepository = certificateAuthorizationRepository;
    }

    @Override
    @Cacheable(cacheNames = "authorized-uri")
    public boolean isAllowedForUri(String certificateName, HttpMethod httpMethod, String uri, User user) {
        log.trace("Checking if cert {} - user {} is allowed to access {} - {}", certificateName, user.getUsername(), httpMethod.name(), uri);

        final Iterable<CertificateAuthorization> byCertificateName = certificateAuthorizationRepository.findByCertificateName(certificateName);


        for (CertificateAuthorization certificateAuthorization : byCertificateName) {
            if (isAuthorizedForRequest(certificateAuthorization, httpMethod, uri, user)) {
                return true;
            }
        }

        return false;
    }

    private boolean isAuthorizedForRequest(CertificateAuthorization certificateAuthorization, HttpMethod httpMethod, String uri, User user) {

        final boolean methodIsAuthorized = methodIsAuthorized(httpMethod, certificateAuthorization);
        final boolean uriIsAuthorized = uriIsAuthorized(uri, certificateAuthorization);
        final boolean uwGroupsMatch = uwGroupsMatch(user, certificateAuthorization);


        final boolean authorized = methodIsAuthorized &&
                uriIsAuthorized &&
                uwGroupsMatch;

        log.trace("method : {} , uri : {}, uwGroups: {} => result : {}", methodIsAuthorized, uriIsAuthorized, uwGroupsMatch, authorized);
        return authorized;

    }

    private boolean uwGroupsMatch(User user, CertificateAuthorization certificateAuthorization) {
        return certificateAuthorization.getUwGroups() == null || certificateAuthorization.getUwGroups().contains(UW_GROUPS_WILDCARD) || userIsMemberOfOneGroup(user, certificateAuthorization);
    }

    private boolean userIsMemberOfOneGroup(User user, CertificateAuthorization certificateAuthorization) {
        //TODO
        return false;
    }

    private boolean uriIsAuthorized(String uri, CertificateAuthorization certificateAuthorization) {
        final Pattern pattern = Pattern.compile(certificateAuthorization.getUriRegex());

        return pattern.matcher(uri).matches();
    }

    private boolean methodIsAuthorized(HttpMethod httpMethod, CertificateAuthorization authorization) {
        return authorization.getHttpMethods().contains(HTTP_METHOD_WILDCARD) || authorization.getHttpMethods().contains(httpMethod.name());
    }


    @Override
    public void addNewAuthorization(String certificateName, String uriRegex, List<String> httpMethods, List<String> uwGroups, List<String> contactEmail, String notes) {
        Preconditions.checkArgument(!Strings.isBlank(certificateName), "certificateName cannot be blank");
        Preconditions.checkArgument(isValidRegex(uriRegex), "uriRegex is invalid");
        Preconditions.checkArgument(hasValidHttpMethods(httpMethods), "httpMethods are invalid");


        CertificateAuthorization newAuthorization = new CertificateAuthorization();

        //TODO the setKey might want to go somewhere else
        //newAuthorization.setKey(createAuthorizationHashKey(certificateName, uriRegex));

        newAuthorization.setCertificateName(certificateName);
        newAuthorization.setUriRegex(uriRegex);
        newAuthorization.setHttpMethods(String.join(HTTP_METHODS_SEPARATOR, httpMethods));
        newAuthorization.setUwGroups(String.join(UW_GROUPS_SEPARATOR, uwGroups));
        if (contactEmail != null) {
            newAuthorization.setContactEmails(String.join(CONTACT_EMAILS_SEPARATOR, httpMethods));
        }
        newAuthorization.setNotes(notes);

        certificateAuthorizationRepository.save(newAuthorization);

        final Iterable<CertificateAuthorization> byCertificateName = certificateAuthorizationRepository.findByCertificateName(certificateName);

        byCertificateName.forEach(addedCertificate -> {
            log.info("{} - {}", addedCertificate.getCertificateName(), addedCertificate.getUriRegex());

        });
    }

    private String createAuthorizationHashKey(String certificateName, String uriRegex) {
        return certificateName + "-" + uriRegex;
    }

    private boolean hasValidHttpMethods(List<String> httpMethods) {
        Preconditions.checkNotNull(httpMethods, "httpMethods cannot be null");
        Preconditions.checkArgument(httpMethods.size() > 0, "httpMethods cannot be empty");


        for (String httpMethod : httpMethods) {
            if (!HTTP_METHOD_WILDCARD.equals(httpMethod)) {
                try {
                    HttpMethod.valueOf(httpMethod);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isValidRegex(String uriRegex) {
        try {
            Pattern.compile(uriRegex);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
