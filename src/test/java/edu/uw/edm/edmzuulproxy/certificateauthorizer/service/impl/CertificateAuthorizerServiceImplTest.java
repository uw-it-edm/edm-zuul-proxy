package edu.uw.edm.edmzuulproxy.certificateauthorizer.service.impl;

import org.apache.commons.collections.iterators.CollatingIterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import edu.uw.edm.edmzuulproxy.certificateauthorizer.CertificateAuthorizationRepository;
import edu.uw.edm.edmzuulproxy.certificateauthorizer.model.dao.CertificateAuthorizationDAO;
import edu.uw.edm.edmzuulproxy.certificateauthorizer.service.CertificateAuthorizerService;
import edu.uw.edm.edmzuulproxy.security.User;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Maxime Deravet Date: 2019-01-29
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cache.type=NONE")
public class CertificateAuthorizerServiceImplTest {

    @MockBean(reset = MockReset.BEFORE)
    private CertificateAuthorizationRepository mockRepository;

    @Autowired
    private CertificateAuthorizerService service;


    @Test
    public void whenNoRepoEntryThenUnauthorizedTest() {
        when(mockRepository.findByCertificateName("cert")).thenReturn(CollatingIterator::new);

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri", new User("test", "", Collections.emptyList()));

        assertFalse("Shouldn't be allowed", allowedForUri);

    }

    @Test
    public void allPathAuthorizedTest() {
        final CertificateAuthorizationDAO auth = new CertificateAuthorizationDAO();
        auth.setUriRegex(".*");
        auth.setHttpMethods("*");
        auth.setCertificateName("cert");
        auth.setUwGroups("*");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));
        final User user = new User("test", "", getListOfAuthorities("group4", "group5", "group_2"));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/zuul/my/uri", user);

        assertTrue(allowedForUri);

    }

    @Test
    public void whenNoUserAndAllGroupsThenAuthorizedTest() {
        final CertificateAuthorizationDAO auth = new CertificateAuthorizationDAO();
        auth.setUriRegex("/my/.*");
        auth.setHttpMethods("POST,GET");
        auth.setCertificateName("cert");
        auth.setUwGroups("*");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri", null);

        assertTrue(allowedForUri);

    }

    @Test
    public void whenNoUserAndGroupsThenUnauthorizedTest() {
        final CertificateAuthorizationDAO auth = new CertificateAuthorizationDAO();
        auth.setUriRegex("/my/.*");
        auth.setHttpMethods("POST,GET");
        auth.setCertificateName("cert");
        auth.setUwGroups("group_1,group_2,group_3");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri", null);

        assertFalse(allowedForUri);

    }


    @Test
    public void whenOneGroupMatchThenAuthorizedTest() {
        final CertificateAuthorizationDAO auth = new CertificateAuthorizationDAO();
        auth.setUriRegex("/my/.*");
        auth.setHttpMethods("POST,GET");
        auth.setCertificateName("cert");
        auth.setUwGroups("group_1,group_2,group_3");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final User user = new User("test", "", getListOfAuthorities("group4", "group5", "group_2"));
        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri", user);

        assertTrue(allowedForUri);

    }

    @Test
    public void whenNoGroupMatchThenUnauthorizedTest() {
        final CertificateAuthorizationDAO auth = new CertificateAuthorizationDAO();
        auth.setUriRegex("/my/.*");
        auth.setHttpMethods("POST,GET");
        auth.setCertificateName("cert");
        auth.setUwGroups("group_1,group_2,group_3");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final User user = new User("test", "", getListOfAuthorities("group4", "group5", "group6"));
        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri", user);

        assertFalse(allowedForUri);

    }

    private List<GrantedAuthority> getListOfAuthorities(String... authorities) {
        return Arrays.stream(authorities).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }


    @Test
    public void whenOneMatchingRuleThenAuthorizedTest() {
        final CertificateAuthorizationDAO auth = new CertificateAuthorizationDAO();
        auth.setUriRegex("/my/.*");
        auth.setHttpMethods("POST,GET");
        auth.setCertificateName("cert");
        auth.setUwGroups("*");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri", new User("test", "", Collections.emptyList()));

        assertTrue(allowedForUri);

    }

    @Test
    public void whenExactPathRuleThenAuthorizedTest() {
        final CertificateAuthorizationDAO auth = new CertificateAuthorizationDAO();
        auth.setUriRegex("/my/uri/is");
        auth.setHttpMethods("POST,GET");
        auth.setCertificateName("cert");
        auth.setUwGroups("*");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri/is", new User("test", "", Collections.emptyList()));

        assertTrue(allowedForUri);

    }

    @Test
    public void whenComplexeRegexMatchingRuleThenAuthorizedTest() {
        final CertificateAuthorizationDAO auth = new CertificateAuthorizationDAO();
        auth.setUriRegex("/my/.*/(1|2)234.*");
        auth.setHttpMethods("POST,GET");
        auth.setCertificateName("cert");
        auth.setUwGroups("*");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri/2234", new User("test", "", Collections.emptyList()));

        assertTrue(allowedForUri);

    }

    @Test
    public void whenWildCardMethodThenAuthorizedTest() {
        final CertificateAuthorizationDAO auth = new CertificateAuthorizationDAO();
        auth.setUriRegex("/my/uri/is");
        auth.setHttpMethods("*");
        auth.setCertificateName("cert");
        auth.setUwGroups("*");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri/is", new User("test", "", Collections.emptyList()));

        assertTrue(allowedForUri);

    }

    @Test
    public void whenTwoRulesButOneMatchingRuleThenAuthorizedTest() {
        final CertificateAuthorizationDAO auth1 = new CertificateAuthorizationDAO();
        auth1.setUriRegex("/no/.*");
        auth1.setHttpMethods("POST,GET");
        auth1.setCertificateName("cert");
        auth1.setUwGroups("*");
        final CertificateAuthorizationDAO auth2 = new CertificateAuthorizationDAO();
        auth2.setUriRegex("/my/.*");
        auth2.setHttpMethods("POST,GET");
        auth2.setCertificateName("cert");
        auth2.setUwGroups("*");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Arrays.asList(auth1, auth2));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri", new User("test", "", Collections.emptyList()));

        assertTrue(allowedForUri);

    }

    @Test
    public void whenTwoRulesButNoneMatchThenUnauthorizedTest() {
        final CertificateAuthorizationDAO auth1 = new CertificateAuthorizationDAO();
        auth1.setUriRegex("/no/.*");
        auth1.setHttpMethods("POST,GET");
        auth1.setCertificateName("cert");
        auth1.setUwGroups("*");
        final CertificateAuthorizationDAO auth2 = new CertificateAuthorizationDAO();
        auth2.setUriRegex("/my/.*");
        auth2.setHttpMethods("POST,GET");
        auth2.setCertificateName("cert");
        auth2.setUwGroups("*");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Arrays.asList(auth1, auth2));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/your/uri", new User("test", "", Collections.emptyList()));

        assertFalse(allowedForUri);

    }

    @Test
    public void whenUriDoesntMatchThenUnauthorizedTest() {
        final CertificateAuthorizationDAO auth = new CertificateAuthorizationDAO();
        auth.setUriRegex("/your/.*");
        auth.setHttpMethods("POST,GET");
        auth.setCertificateName("cert");
        auth.setUwGroups("*");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final boolean allowedForUri = service.isAllowedForUri("cert", HttpMethod.GET, "/my/uri", new User("test", "", Collections.emptyList()));

        assertFalse(allowedForUri);

    }

    @Test
    public void shouldReturnDistinctListOfProfiles() {
        final CertificateAuthorizationDAO auth = new CertificateAuthorizationDAO();
        auth.setUriRegex("/my/uri");
        auth.setCertificateName("cert");
        auth.setHttpMethods("*");
        auth.setUwGroups("*");
        auth.setAuthorizedProfiles("TestProfile1, TestProfile2,  testprofile1    ");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Collections.singletonList(auth));

        final List<String> profiles = service.getAuthorizedProfilesForUri("cert", "/my/uri");

        assertThat(profiles, contains("testprofile1", "testprofile2"));
    }

    @Test
    public void shouldReturnProfilesOnlyFromMatchedUri() {
        final CertificateAuthorizationDAO auth1 = new CertificateAuthorizationDAO();
        auth1.setUriRegex("/first/uri");
        auth1.setCertificateName("cert");
        auth1.setHttpMethods("*");
        auth1.setUwGroups("*");
        auth1.setAuthorizedProfiles("TestProfile1");

        final CertificateAuthorizationDAO auth2 = new CertificateAuthorizationDAO();
        auth2.setUriRegex("/second/uri");
        auth2.setCertificateName("cert");
        auth2.setHttpMethods("*");
        auth2.setUwGroups("*");
        auth2.setAuthorizedProfiles("TestProfile2");

        when(mockRepository.findByCertificateName("cert")).thenReturn(Arrays.asList(auth1, auth2));

        List<String> profiles = service.getAuthorizedProfilesForUri("cert", "/first/uri");
        assertThat(profiles, contains("testprofile1"));

        profiles = service.getAuthorizedProfilesForUri("cert", "/second/uri");
        assertThat(profiles, contains("testprofile2"));

        profiles = service.getAuthorizedProfilesForUri("cert", "/third/uri");
        assertTrue(profiles.isEmpty());
    }
}