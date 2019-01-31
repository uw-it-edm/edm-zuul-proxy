package edu.uw.edm.edmzuulproxy.certificateauthorizer.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import edu.uw.edm.edmzuulproxy.certificateauthorizer.CertificateAuthorizationRepository;
import edu.uw.edm.edmzuulproxy.certificateauthorizer.model.CompiledCertificateAuthorization;
import edu.uw.edm.edmzuulproxy.certificateauthorizer.model.dao.CertificateAuthorizationDAO;

import static edu.uw.edm.edmzuulproxy.certificateauthorizer.service.impl.CertificateAuthorizerServiceImpl.UW_GROUPS_SEPARATOR;

/**
 * @author Maxime Deravet Date: 2019-01-30
 */
@Service
public class CertificateAuthorizationRetriever {

    private CertificateAuthorizationRepository certificateAuthorizationRepository;

    @Autowired
    public CertificateAuthorizationRetriever(CertificateAuthorizationRepository certificateAuthorizationRepository) {
        this.certificateAuthorizationRepository = certificateAuthorizationRepository;
    }


    @Cacheable(cacheNames = "cert-retriever")
    public Iterable<CompiledCertificateAuthorization> findByCertificateName(String certificateName) {

        final Iterable<CertificateAuthorizationDAO> certs = certificateAuthorizationRepository.findByCertificateName(certificateName);

        return StreamSupport
                .stream(certs.spliterator(), false)
                .map(this::toCompiledCertificateAuthorization)
                .collect(Collectors.toSet());
    }

    private CompiledCertificateAuthorization toCompiledCertificateAuthorization(CertificateAuthorizationDAO certificateAuthorizationDAO) {
        Preconditions.checkNotNull(certificateAuthorizationDAO);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(certificateAuthorizationDAO.getCertificateName()));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(certificateAuthorizationDAO.getUriRegex()));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(certificateAuthorizationDAO.getHttpMethods()));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(certificateAuthorizationDAO.getUwGroups()));

        return CompiledCertificateAuthorization.builder()
                .certificateName(certificateAuthorizationDAO.getCertificateName())
                .uriRegex(Pattern.compile(certificateAuthorizationDAO.getUriRegex()))
                .httpMethods(toHttpMethodList(certificateAuthorizationDAO.getHttpMethods()))
                .uwGroups(toGroupList(certificateAuthorizationDAO.getUwGroups()))
                .build();


    }

    private List<String> toHttpMethodList(String httpMethodsList) {
        return Arrays.asList(httpMethodsList.split(UW_GROUPS_SEPARATOR));
    }

    private List<String> toGroupList(String groupsList) {
        if (groupsList == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(groupsList.split(UW_GROUPS_SEPARATOR));
    }


}
