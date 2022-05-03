package edu.uw.edm.edmzuulproxy.filter;

import com.google.common.collect.Lists;
import com.netflix.zuul.context.RequestContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import edu.uw.edm.edmzuulproxy.certificateauthorizer.service.CertificateAuthorizerService;
import edu.uw.edm.edmzuulproxy.properties.CertificateAuthorizationProperties;
import edu.uw.edm.edmzuulproxy.security.User;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet Date: 2019-01-29
 */
@RunWith(SpringRunner.class)
public class CertificateAuthenticationFilterTest {

    private static final String CERTIFICATE_NAME_HEADER = "CERT_NAME";
    private CertificateAuthorizerService certificateAuthorizerService;
    private CertificateAuthorizationProperties certificateAuthorizationProperties;

    private MockHttpServletRequest mockHttpServletRequest;
    private MockHttpServletResponse mockHttpServletResponse;

    @Before
    public void initMocks() {
        certificateAuthorizerService = mock(CertificateAuthorizerService.class);
        certificateAuthorizationProperties = new CertificateAuthorizationProperties(CERTIFICATE_NAME_HEADER);
        Map<String, String> myMap = new HashMap();
        myMap.put("mycert", "apikey");
        certificateAuthorizationProperties.setCertificateToApiKeyMap(myMap);
    }

    @Before
    public void setTestRequestcontext() {
        RequestContext context = new RequestContext();
        mockHttpServletRequest = new MockHttpServletRequest();
        context.setRequest(mockHttpServletRequest);
        mockHttpServletResponse = new MockHttpServletResponse();
        context.setResponse(mockHttpServletResponse);
        context.setResponseGZipped(false);

        RequestContext.testSetCurrentContext(context);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new User("test", "", Collections.emptyList()));
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

    }

    @After
    public void reset() {
        RequestContext.getCurrentContext().clear();
    }

    @Test
    public void shouldWorkWithAnonymousUserTest() {
        mockHttpServletRequest.setRequestURI("/my/uri");
        mockHttpServletRequest.setMethod("GET");
        mockHttpServletRequest.addHeader(CERTIFICATE_NAME_HEADER, "mycert");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(new AnonymousAuthenticationToken("key", "anonymousUser", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        SecurityContextHolder.setContext(securityContext);


        when(certificateAuthorizerService.isAllowedForUri(any(), any(), any(), any())).thenReturn(true);

        CertificateAuthenticationFilter filter = newFilter();

        filter.run();

        assertThat(mockHttpServletResponse.getStatus(), is(200));


    }

    @Test
    public void shouldThrowAnErrorWhenExceptionTest() {
        mockHttpServletRequest.setRequestURI("/my/uri");
        mockHttpServletRequest.setMethod("GET");
        mockHttpServletRequest.addHeader(CERTIFICATE_NAME_HEADER, "mycert");

        when(certificateAuthorizerService.isAllowedForUri(any(), any(), any(), any())).thenThrow(new RuntimeException());


        CertificateAuthenticationFilter filter = newFilter();

        filter.run();

        assertThat(mockHttpServletResponse.getStatus(), is(500));

    }

    @Test
    public void shouldntFilterWhenNoCertHeaderTest() {
        mockHttpServletRequest.setRequestURI("/my/uri");
        mockHttpServletRequest.setMethod("GET");

        CertificateAuthenticationFilter filter = newFilter();

        assertFalse(filter.shouldFilter());

    }

    @Test
    public void shouldValidateFilterWhenCertHeaderTest() {
        mockHttpServletRequest.setRequestURI("/my/uri");
        mockHttpServletRequest.setMethod("GET");
        mockHttpServletRequest.addHeader(CERTIFICATE_NAME_HEADER, "mycert");

        CertificateAuthenticationFilter filter = newFilter();

        assertTrue(filter.shouldFilter());
    }

    @Test
    public void whenUnauthorizedThen401Test() {
        mockHttpServletRequest.setRequestURI("/my/uri");
        mockHttpServletRequest.setMethod("GET");
        mockHttpServletRequest.addHeader(CERTIFICATE_NAME_HEADER, "mycert");

        when(certificateAuthorizerService.isAllowedForUri(any(), any(), any(), any())).thenReturn(false);

        CertificateAuthenticationFilter filter = newFilter();

        filter.run();

        assertThat(mockHttpServletResponse.getStatus(), is(401));


    }

    @Test
    public void whenAuthorizedThen200Test() {
        mockHttpServletRequest.setRequestURI("/my/uri");
        mockHttpServletRequest.setMethod("GET");
        mockHttpServletRequest.addHeader(CERTIFICATE_NAME_HEADER, "mycert");

        when(certificateAuthorizerService.isAllowedForUri(any(), any(), any(), any())).thenReturn(true);

        CertificateAuthenticationFilter filter = newFilter();

        filter.run();

        assertThat(mockHttpServletResponse.getStatus(), is(200));


    }

    @Test
    public void shouldAddAuthorizedProfilesHeaderToRequest() {
        mockHttpServletRequest.setRequestURI("/my/uri");
        mockHttpServletRequest.setMethod("GET");
        mockHttpServletRequest.addHeader(CERTIFICATE_NAME_HEADER, "mycert");

        when(certificateAuthorizerService.isAllowedForUri(any(), any(), any(), any())).thenReturn(true);
        when(certificateAuthorizerService.getAuthorizedProfilesForUri(any(), any())).thenReturn(Lists.newArrayList("testprofile1", "testprofile2"));

        CertificateAuthenticationFilter filter = newFilter();
        filter.run();

        final String header = RequestContext.getCurrentContext().getZuulRequestHeaders().get("x-uw-authorized-profiles");
        assertThat(header, is("testprofile1,testprofile2"));
    }

    @Test
    public void shouldAddEmptyAuthorizedProfilesHeaderIfNoneDefined() {
        mockHttpServletRequest.setRequestURI("/my/uri");
        mockHttpServletRequest.setMethod("GET");
        mockHttpServletRequest.addHeader(CERTIFICATE_NAME_HEADER, "mycert");

        when(certificateAuthorizerService.isAllowedForUri(any(), any(), any(), any())).thenReturn(true);
        when(certificateAuthorizerService.getAuthorizedProfilesForUri(any(), any())).thenReturn(null);

        CertificateAuthenticationFilter filter = newFilter();
        filter.run();

        final String header = RequestContext.getCurrentContext().getZuulRequestHeaders().get("x-uw-authorized-profiles");
        assertTrue(header.isEmpty());
    }

    @Test
    public void shouldAddDocfinityHeaderToRequest() {
        mockHttpServletRequest.setRequestURI("/docfinity/webservices/rest/metadata");
        mockHttpServletRequest.setMethod("GET");
        mockHttpServletRequest.addHeader(CERTIFICATE_NAME_HEADER, "mycert");

        when(certificateAuthorizerService.isAllowedForUri(any(), any(), any(), any())).thenReturn(true);
        when(certificateAuthorizerService.getAuthorizedProfilesForUri(any(), any())).thenReturn(Lists.newArrayList("testprofile1", "testprofile2"));

        CertificateAuthenticationFilter filter = newFilter();
        filter.run();

        final String authHeader = RequestContext.getCurrentContext().getZuulRequestHeaders().get("authorization");
        final String tokenHeader = RequestContext.getCurrentContext().getZuulRequestHeaders().get("x-xsrf-token");
        final String cookieHeader = RequestContext.getCurrentContext().getZuulRequestHeaders().get("cookie");
        assertThat(authHeader, is("Bearer apikey"));
        assertThat(tokenHeader, is("edm-token"));
        assertThat(cookieHeader, is("xsrf-token=edm-token"));
    }

    @Test
    public void shouldAddDocfinityHeaderWhenRequestForDocumentApi() {
        // arrange
        mockHttpServletRequest.setRequestURI("/documents/create");
        mockHttpServletRequest.setMethod("GET");
        mockHttpServletRequest.addHeader(CERTIFICATE_NAME_HEADER, "mycert");

        when(certificateAuthorizerService.isAllowedForUri(any(), any(), any(), any())).thenReturn(true);
        when(certificateAuthorizerService.getAuthorizedProfilesForUri(any(), any())).thenReturn(Lists.newArrayList("testprofile1", "testprofile2"));

        // act
        CertificateAuthenticationFilter filter = newFilter();
        filter.run();

        // assert
        final String apiKeyHeader = RequestContext.getCurrentContext().getZuulRequestHeaders().get("x-api-key");
        assertThat(apiKeyHeader, is("apikey"));
    }

    @Test
    public void shouldNotAddDocfinityHeaderToRequestForUnknownCert() {
        mockHttpServletRequest.setRequestURI("/docfinity/webservices/rest/metadata");
        mockHttpServletRequest.setMethod("GET");
        mockHttpServletRequest.addHeader(CERTIFICATE_NAME_HEADER, "unknownCert");

        when(certificateAuthorizerService.isAllowedForUri(any(), any(), any(), any())).thenReturn(true);
        when(certificateAuthorizerService.getAuthorizedProfilesForUri(any(), any())).thenReturn(Lists.newArrayList("testprofile1", "testprofile2"));

        CertificateAuthenticationFilter filter = newFilter();
        filter.run();

        final String authHeader = RequestContext.getCurrentContext().getZuulRequestHeaders().get("authorization");
        final String tokenHeader = RequestContext.getCurrentContext().getZuulRequestHeaders().get("x-xsrf-token");
        final String cookieHeader = RequestContext.getCurrentContext().getZuulRequestHeaders().get("cookie");
        assertNull(authHeader);
        assertNull(tokenHeader);
        assertNull(cookieHeader);
    }

    @Test
    public void shouldNotAddDocfinityHeaderToRequestForNonDocFinityUri() {
        mockHttpServletRequest.setRequestURI("/data/webservices/rest/metadata");
        mockHttpServletRequest.setMethod("GET");
        mockHttpServletRequest.addHeader(CERTIFICATE_NAME_HEADER, "mycert");

        when(certificateAuthorizerService.isAllowedForUri(any(), any(), any(), any())).thenReturn(true);
        when(certificateAuthorizerService.getAuthorizedProfilesForUri(any(), any())).thenReturn(Lists.newArrayList("testprofile1", "testprofile2"));

        CertificateAuthenticationFilter filter = newFilter();
        filter.run();

        final String authHeader = RequestContext.getCurrentContext().getZuulRequestHeaders().get("authorization");
        final String tokenHeader = RequestContext.getCurrentContext().getZuulRequestHeaders().get("x-xsrf-token");
        final String cookieHeader = RequestContext.getCurrentContext().getZuulRequestHeaders().get("cookie");
        assertNull(authHeader);
        assertNull(tokenHeader);
        assertNull(cookieHeader);
    }

    private CertificateAuthenticationFilter newFilter() {
        return new CertificateAuthenticationFilter(certificateAuthorizerService, certificateAuthorizationProperties, new ProxyRequestHelper(new ZuulProperties()));
    }

}
