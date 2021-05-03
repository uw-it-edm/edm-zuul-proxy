package edu.uw.edm.edmzuulproxy.filter;

import com.google.common.base.Strings;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import edu.uw.edm.edmzuulproxy.properties.SecurityProperties;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * Filter that ensures that the docfinity request header is present when routing to DocFinity server.
 * Will return 400 error if header is missing.
 */
@Slf4j
@Component
public class DocFinityAuditUserFilter extends ZuulFilter {
    private static final String DOCFINITY_URI_PREFIX = "/docfinity";

    private final String headerName;

    @Autowired
    public DocFinityAuditUserFilter(SecurityProperties securityProperties) {
      this.headerName = securityProperties.getDocfinityAuthenticationHeaderName();
    }

    @Override
    public String filterType() {
      return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
      return 3;
    }

    @Override
    public boolean shouldFilter() {
      RequestContext ctx = RequestContext.getCurrentContext();
      String uri = ctx.getRequest().getRequestURI();
      return uri != null && uri.toLowerCase().startsWith(DOCFINITY_URI_PREFIX);
    }

    @Override
    public Object run() throws ZuulException {
      RequestContext ctx = RequestContext.getCurrentContext();
      String headerValue = ctx.getRequest().getHeader(this.headerName);

      if (Strings.isNullOrEmpty(headerValue)) {
          String errorMessage = String.format("Header '%s' is required for requests to '%s'.", this.headerName, DOCFINITY_URI_PREFIX);
          log.error(errorMessage);

          ctx.setResponseStatusCode(HttpStatus.BAD_REQUEST.value());
          ctx.setResponseBody(errorMessage);
          ctx.setSendZuulResponse(false);
      }

      return null;
    }
}
