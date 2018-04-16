package edu.uw.edm.edmzuulproxy.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * @author Maxime Deravet
 * Date: 4/9/18
 */
@Slf4j
@Component
public class LoggingFilter extends ZuulFilter {


    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        log.trace(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        return null;
    }

}
