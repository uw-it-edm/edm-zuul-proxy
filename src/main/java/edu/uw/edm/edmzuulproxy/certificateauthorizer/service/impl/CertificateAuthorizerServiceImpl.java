package edu.uw.edm.edmzuulproxy.certificateauthorizer.service.impl;

import com.google.common.base.Preconditions;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import edu.uw.edm.edmzuulproxy.certificateauthorizer.CertificateAuthorizationRepository;
import edu.uw.edm.edmzuulproxy.certificateauthorizer.model.CompiledCertificateAuthorization;
import edu.uw.edm.edmzuulproxy.certificateauthorizer.model.dao.CertificateAuthorizationDAO;
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
    public static final String PROFILES_SEPARATOR = DEFAULT_SEPARATOR;

    private CertificateAuthorizationRetriever certificateAuthorizationRetriever;
    private CertificateAuthorizationRepository certificateAuthorizationRepository;

    @Autowired
    public CertificateAuthorizerServiceImpl(CertificateAuthorizationRetriever certificateAuthorizationRetriever, CertificateAuthorizationRepository certificateAuthorizationRepository) {
        this.certificateAuthorizationRetriever = certificateAuthorizationRetriever;
        this.certificateAuthorizationRepository = certificateAuthorizationRepository;
    }

    @Override
    @Cacheable(cacheNames = "authorized-uri")
    public boolean isAllowedForUri(String certificateName, HttpMethod httpMethod, String uri, User user) {
        log.debug("Checking if cert {} - user {} is allowed to access {} - {}", certificateName, user == null ? "anonymous" : user.getUsername(), httpMethod.name(), uri);

        final Iterable<CompiledCertificateAuthorization> byCertificateName = certificateAuthorizationRetriever.findByCertificateName(certificateName);


        for (CompiledCertificateAuthorization certificateAuthorizationDAO : byCertificateName) {
            if (isAuthorizedForRequest(certificateAuthorizationDAO, httpMethod, uri, user)) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Cacheable(cacheNames = "authorized-profiles-uri")
    public List<String> getAuthorizedProfilesForUri(String certificateName, String uri) {
      log.debug("Retrieving authorized profiles for cert {} to access uri {}", certificateName, uri);

      final Iterable<CompiledCertificateAuthorization> byCertificateName = certificateAuthorizationRetriever.findByCertificateName(certificateName);

      return StreamSupport
                .stream(byCertificateName.spliterator(), false)
                .filter(entry -> uriIsAuthorized(uri, entry))
                .flatMap(entry -> entry.getAuthorizedProfiles().stream())
                .map(profile -> profile.toLowerCase().trim())
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isAuthorizedForRequest(CompiledCertificateAuthorization certificateAuthorizationDAO, HttpMethod httpMethod, String uri, User user) {

        final boolean authorized = methodIsAuthorized(httpMethod, certificateAuthorizationDAO) &&
                uriIsAuthorized(uri, certificateAuthorizationDAO)
                && uwGroupsMatch(user, certificateAuthorizationDAO);
        log.trace("Cert {} - user {} {} authorized for request to access {} - {}", certificateAuthorizationDAO.getCertificateName(), user == null ? "anonymous" : user.getUsername(), authorized ? "IS" : "IS NOT", httpMethod.name(), uri);

        return authorized;

    }

    private boolean uwGroupsMatch(User user, CompiledCertificateAuthorization certificateAuthorization) {
        return certificateAuthorization.getUwGroups().contains(UW_GROUPS_WILDCARD)
                || userIsMemberOfOneGroup(user, certificateAuthorization);
    }

    private boolean userIsMemberOfOneGroup(User user, CompiledCertificateAuthorization certificateAuthorization) {

        return user != null && user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> certificateAuthorization.getUwGroups().contains(authority));
    }

    private boolean uriIsAuthorized(String uri, CompiledCertificateAuthorization certificateAuthorization) {
        return certificateAuthorization.getUriRegex().matcher(uri).matches();
    }

    private boolean methodIsAuthorized(HttpMethod httpMethod, CompiledCertificateAuthorization authorization) {
        return authorization.getHttpMethods().contains(HTTP_METHOD_WILDCARD)
                || authorization.getHttpMethods().contains(httpMethod.name());
    }


    @Override
    public void addNewAuthorization(String certificateName, String uriRegex, List<String> httpMethods, List<String> uwGroups, List<String> contactEmail, String notes) {
        Preconditions.checkArgument(!Strings.isBlank(certificateName), "certificateName cannot be blank");
        Preconditions.checkArgument(isValidRegex(uriRegex), "uriRegex is invalid");
        Preconditions.checkArgument(hasValidHttpMethods(httpMethods), "httpMethods are invalid");
        Preconditions.checkNotNull(uwGroups, "uwGroup is required");


        CertificateAuthorizationDAO newAuthorization = new CertificateAuthorizationDAO();

        newAuthorization.setCertificateName(certificateName);
        newAuthorization.setUriRegex(uriRegex);
        newAuthorization.setHttpMethods(String.join(HTTP_METHODS_SEPARATOR, httpMethods));
        newAuthorization.setUwGroups(String.join(UW_GROUPS_SEPARATOR, uwGroups));

        newAuthorization.setMethodAndURI(newAuthorization.getHttpMethods() + " " + newAuthorization.getUriRegex());

        if (contactEmail != null) {
            newAuthorization.setContactEmails(String.join(CONTACT_EMAILS_SEPARATOR, httpMethods));
        }
        newAuthorization.setNotes(notes);

        certificateAuthorizationRepository.save(newAuthorization);

        final Iterable<CertificateAuthorizationDAO> byCertificateName = certificateAuthorizationRepository.findByCertificateName(certificateName);

        byCertificateName.forEach(addedCertificate -> {
            log.info("{} - {}", addedCertificate.getCertificateName(), addedCertificate.getUriRegex());

        });
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
