package edu.uw.edm.edmzuulproxy.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import edu.uw.edm.edmzuulproxy.certificateauthorizer.service.CertificateAuthorizerService;
import edu.uw.edm.edmzuulproxy.properties.CertificateAuthorizationProperties;
import edu.uw.edm.edmzuulproxy.security.User;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static edu.uw.edm.edmzuulproxy.certificateauthorizer.service.impl.CertificateAuthorizerServiceImpl.PROFILES_SEPARATOR;

import java.util.List;
import java.util.Map;

/**
 * @author Maxime Deravet Date: 2019-01-28
 */
@Slf4j
@Component
public class CertificateAuthenticationFilter extends ZuulFilter {

    private static final String AUTHORIZED_PROFILES_HEADER = "x-uw-authorized-profiles";
    private static final String AUTHORIZATION_HEADER = "authorization";
    private static final String COOKIE_HEADER = "cookie";
    private static final String COOKIE_VALUE = "xsrf-token=edm-token";
    private static final String X_API_KEY_HEADER = "x-api-key";
    private static final String X_XSRF_TOKEN_HEADER = "x-xsrf-token";
    private static final String X_XSRF_TOKEN_VALUE = "edm-token";
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
            final Map<String, String> certificateToApiKeyMap = certificateAuthorizationProperties.getCertificateToApiKeyMap();

            if (!allowedForUri) {
                sendAuthorizationError(certificateName, httpMethod, zuulRequestURI, user, ctx);
                return null;
            }

            final List<String> profiles = certificateAuthorizerService.getAuthorizedProfilesForUri(certificateName, zuulRequestURI);
            final String profilesHeaderValue = profiles != null ? String.join(PROFILES_SEPARATOR, profiles) : "";
            ctx.addZuulRequestHeader(AUTHORIZED_PROFILES_HEADER, profilesHeaderValue);

            if (   certificateToApiKeyMap != null && certificateToApiKeyMap.containsKey(certificateName)
                && zuulRequestURI != null) {
                String apiKey = certificateToApiKeyMap.get(certificateName);
                if (zuulRequestURI.startsWith("/docfinity/")) {
                    ctx.addZuulRequestHeader(AUTHORIZATION_HEADER, "Bearer " + apiKey);
                    ctx.addZuulRequestHeader(X_XSRF_TOKEN_HEADER, X_XSRF_TOKEN_VALUE);
                    ctx.addZuulRequestHeader(COOKIE_HEADER, COOKIE_VALUE);
                } else if (zuulRequestURI.startsWith("/documents/")) {
                    ctx.addZuulRequestHeader(X_API_KEY_HEADER, apiKey);
                }
            }

        } catch (Exception e) {
            log.error("Error running CertificateAuthenticationFilter: ", e);

            ctx.setResponseStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            ctx.setSendZuulResponse(false);
        }

        return null;
    }

    private String getCertificateName(RequestContext ctx) {
        return ctx.getRequest().getHeader(certificateAuthorizationProperties.getCertificateNameHeader());
    }


    private void sendAuthorizationError(String certificateName, HttpMethod httpMethod, String uri, User user, RequestContext ctx) {
        log.warn("certificate {} - {} is not allowed to access {} - {}", certificateName, user == null ? "anonymous" : user.getUsername(), httpMethod.name(), uri);

        ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());

        ctx.setSendZuulResponse(false);
    }

    private User getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return null;
        } else {
            return (User) authentication.getPrincipal();
        }
    }
}
