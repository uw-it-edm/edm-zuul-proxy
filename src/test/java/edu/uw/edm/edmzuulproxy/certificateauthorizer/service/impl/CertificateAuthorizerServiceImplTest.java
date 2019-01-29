package edu.uw.edm.edmzuulproxy.certificateauthorizer.service.impl;

import org.apache.commons.collections.iterators.CollatingIterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;

import edu.uw.edm.edmzuulproxy.certificateauthorizer.CertificateAuthorizationRepository;
import edu.uw.edm.edmzuulproxy.certificateauthorizer.model.CertificateAuthorization;
import edu.uw.edm.edmzuulproxy.certificateauthorizer.service.CertificateAuthorizerService;
import edu.uw.edm.edmzuulproxy.security.User;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Maxime Deravet Date: 2019-01-29
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cache.type=NONE")
public class CertificateAuthorizerServiceImplTest {

    @MockBean(reset = MockReset.BEFORE)
    CertificateAuthorizationRepository mockRepository;

    @Autowired
    CertificateAuthorizerService service;


    @Test
    public void whenNoRepoEntryThenUnauthorizedTest() {
        Mockito.when(mockRepository.findByCertificateName("cert")).thenReturn(CollatingIterator::new);

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri", new User("test", "", Collections.emptyList()));

        assertFalse("Shouldn't be allowed", allowedForUri);

    }

    @Test
    public void whenOneMatchingRuleThenAuthorizedTest() {
        final CertificateAuthorization auth = new CertificateAuthorization();
        auth.setUriRegex("/my/.*");
        auth.setHttpMethods("POST,GET");
        auth.setCertificateName("cert");

        Mockito.when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri", new User("test", "", Collections.emptyList()));

        assertTrue(allowedForUri);

    }

    @Test
    public void whenExactPathRuleThenAuthorizedTest() {
        final CertificateAuthorization auth = new CertificateAuthorization();
        auth.setUriRegex("/my/uri/is");
        auth.setHttpMethods("POST,GET");
        auth.setCertificateName("cert");

        Mockito.when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri/is", new User("test", "", Collections.emptyList()));

        assertTrue(allowedForUri);

    }

    @Test
    public void whenComplexeRegexMatchingRuleThenAuthorizedTest() {
        final CertificateAuthorization auth = new CertificateAuthorization();
        auth.setUriRegex("/my/.*/(1|2)234.*");
        auth.setHttpMethods("POST,GET");
        auth.setCertificateName("cert");

        Mockito.when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri/2234", new User("test", "", Collections.emptyList()));

        assertTrue(allowedForUri);

    }

    @Test
    public void whenWildCardMethodThenAuthorizedTest() {
        final CertificateAuthorization auth = new CertificateAuthorization();
        auth.setUriRegex("/my/uri/is");
        auth.setHttpMethods("*");
        auth.setCertificateName("cert");

        Mockito.when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri/is", new User("test", "", Collections.emptyList()));

        assertTrue(allowedForUri);

    }

    @Test
    public void whenTwoRulesButOneMatchingRuleThenAuthorizedTest() {
        final CertificateAuthorization auth1 = new CertificateAuthorization();
        auth1.setUriRegex("/no/.*");
        auth1.setHttpMethods("POST,GET");
        auth1.setCertificateName("cert");
        final CertificateAuthorization auth2 = new CertificateAuthorization();
        auth2.setUriRegex("/my/.*");
        auth2.setHttpMethods("POST,GET");
        auth2.setCertificateName("cert");

        Mockito.when(mockRepository.findByCertificateName("cert")).thenReturn(Arrays.asList(auth1, auth2));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri", new User("test", "", Collections.emptyList()));

        assertTrue(allowedForUri);

    }

    @Test
    public void whenTwoRulesButNoneMatchThenUnauthorizedTest() {
        final CertificateAuthorization auth1 = new CertificateAuthorization();
        auth1.setUriRegex("/no/.*");
        auth1.setHttpMethods("POST,GET");
        auth1.setCertificateName("cert");
        final CertificateAuthorization auth2 = new CertificateAuthorization();
        auth2.setUriRegex("/my/.*");
        auth2.setHttpMethods("POST,GET");
        auth2.setCertificateName("cert");

        Mockito.when(mockRepository.findByCertificateName("cert")).thenReturn(Arrays.asList(auth1, auth2));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/your/uri", new User("test", "", Collections.emptyList()));

        assertFalse(allowedForUri);

    }

    @Test
    public void whenUriDoesntMatchThenUnauthorizedTest() {
        final CertificateAuthorization auth = new CertificateAuthorization();
        auth.setUriRegex("/your/.*");
        auth.setHttpMethods("POST,GET");
        auth.setCertificateName("cert");

        Mockito.when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri", new User("test", "", Collections.emptyList()));

        assertFalse(allowedForUri);

    }

}