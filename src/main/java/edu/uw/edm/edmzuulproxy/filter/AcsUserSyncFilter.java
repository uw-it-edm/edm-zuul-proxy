package edu.uw.edm.edmzuulproxy.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import edu.uw.edm.edmzuulproxy.service.UserProvisioningService;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;

@Slf4j
@Component
public class AcsUserSyncFilter extends ZuulFilter {
    private UserProvisioningService userProvisioningService;

    @Autowired
    public AcsUserSyncFilter(UserProvisioningService userProvisioningService) {
        this.userProvisioningService = userProvisioningService;
    }

    @Override
    public String filterType() {
        return ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 2;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        Principal user = request.getUserPrincipal();

        try {
            userProvisioningService.provisionAcsUser(user.getName());
        } catch (Exception e) {
            log.error("Error Provisioning ACS User: ", e);
        }

        return null;
    }
}
