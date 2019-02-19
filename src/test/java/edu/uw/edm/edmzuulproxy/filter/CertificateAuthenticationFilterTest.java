package edu.uw.edm.edmzuulproxy.filter;

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

import edu.uw.edm.edmzuulproxy.certificateauthorizer.service.CertificateAuthorizerService;
import edu.uw.edm.edmzuulproxy.properties.CertificateAuthorizationProperties;
import edu.uw.edm.edmzuulproxy.security.User;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
    public void shoudlWorkWithAnonymousUserTest() {
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


    private CertificateAuthenticationFilter newFilter() {
        return new CertificateAuthenticationFilter(certificateAuthorizerService, certificateAuthorizationProperties, new ProxyRequestHelper(new ZuulProperties()));
    }

}