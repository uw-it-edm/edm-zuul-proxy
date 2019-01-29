package edu.uw.edm.edmzuulproxy.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import edu.uw.edm.edmzuulproxy.certificateauthorizer.service.CertificateAuthorizerService;
import edu.uw.edm.edmzuulproxy.properties.CertificateAuthorizationProperties;
import edu.uw.edm.edmzuulproxy.security.User;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * @author Maxime Deravet Date: 2019-01-28
 */
@Slf4j
@Component
public class CertificateAuthenticationFilter extends ZuulFilter {

    public static final int CERTIFICATE_AUTHORIZATION_FILTER_ORDER = 2;
    private CertificateAuthorizerService certificateAuthorizerService;
    private CertificateAuthorizationProperties certificateAuthorizationProperties;

    private ProxyRequestHelper proxyRequestHelper;

    @Autowired
    public CertificateAuthenticationFilter(CertificateAuthorizerService certificateAuthorizerService, CertificateAuthorizationProperties certificateAuthorizationProperties, ProxyRequestHelper proxyRequestHelper) {
        this.certificateAuthorizerService = certificateAuthorizerService;
        this.certificateAuthorizationProperties = certificateAuthorizationProperties;
        this.proxyRequestHelper = proxyRequestHelper;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return CERTIFICATE_AUTHORIZATION_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();

        return !Strings.isBlank(getCertificateName(ctx));
    }


    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        try {
            User user = getCurrentUser();
            final String zuulRequestURI = proxyRequestHelper.buildZuulRequestURI(ctx.getRequest());
            final String certificateName = getCertificateName(ctx);
            final HttpMethod httpMethod = HttpMethod.valueOf(ctx.getRequest().getMethod());
            final boolean allowedForUri = certificateAuthorizerService.isAllowedForUri(certificateName, httpMethod, zuulRequestURI, user);

            if (!allowedForUri) {
                sendAuthorizationError(certificateName, httpMethod, zuulRequestURI, user, ctx);
                return null;
            }
        } catch (Exception e) {
            log.error("Error running CertificateAuthenticationFilter: ", e);

        }

        return null;
    }

    private String getCertificateName(RequestContext ctx) {
        return ctx.getRequest().getHeader(certificateAuthorizationProperties.getCertificateNameHeader());
    }


    private void sendAuthorizationError(String certificateName, HttpMethod httpMethod, String uri, User user, RequestContext ctx) {
        log.debug("certificate {} - {} is not allowed to access {} - {}", certificateName, user.getUsername(), httpMethod.name(), uri);

        ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());

        ctx.setSendZuulResponse(false);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((User) principal);
    }
}
