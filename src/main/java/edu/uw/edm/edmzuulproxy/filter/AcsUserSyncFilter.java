package edu.uw.edm.edmzuulproxy.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import edu.uw.edm.edmzuulproxy.service.UserProvisioningService;
import lombok.extern.slf4j.Slf4j;

import static edu.uw.edm.edmzuulproxy.filter.CertificateAuthenticationFilter.CERTIFICATE_AUTHORIZATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Slf4j
@Component
public class AcsUserSyncFilter extends ZuulFilter {
    static final int ACS_USER_SYNC_FILTER_ORDER = CERTIFICATE_AUTHORIZATION_FILTER_ORDER + 1;
    private UserProvisioningService userProvisioningService;

    @Autowired
    public AcsUserSyncFilter(UserProvisioningService userProvisioningService) {
        this.userProvisioningService = userProvisioningService;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return ACS_USER_SYNC_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        if (log.isTraceEnabled()) {
            String s = getCurrentUser() != null ? "" : "not ";
            log.trace("{} filtering with AcsUserSyncFilter", s);
        }
        return getCurrentUser() != null;
    }

    @Override
    public Object run() {
        try {
            Principal user = getCurrentUser();
            userProvisioningService.provisionAcsUser(user.getName());
        } catch (Exception e) {
            log.error("Error Provisioning ACS User: ", e);
        }

        return null;
    }

    private Principal getCurrentUser() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        return request.getUserPrincipal();
    }
}
