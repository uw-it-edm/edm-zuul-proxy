package edu.uw.edm.edmzuulproxy.filter;

import com.netflix.zuul.context.RequestContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import edu.uw.edm.edmzuulproxy.properties.SecurityProperties;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;

public class DocFinityAuditUserFilterTest {
    private MockHttpServletRequest mockHttpServletRequest;
    private MockHttpServletResponse mockHttpServletResponse;
    private DocFinityAuditUserFilter filter;

    @Before
    public void setup() {
        RequestContext context = new RequestContext();
        mockHttpServletRequest = new MockHttpServletRequest();
        context.setRequest(mockHttpServletRequest);
        mockHttpServletResponse = new MockHttpServletResponse();
        context.setResponse(mockHttpServletResponse);
        context.setResponseGZipped(false);

        RequestContext.testSetCurrentContext(context);

        filter = new DocFinityAuditUserFilter(new SecurityProperties());
    }

    @After
    public void reset() {
        RequestContext.getCurrentContext().clear();
    }

    @Test
    public void shouldRunOnlyForDocFinityRequests() {
        // test root
        mockHttpServletRequest.setRequestURI("/");
        assertFalse(filter.shouldFilter());
      
        // test non docfinity
        mockHttpServletRequest.setRequestURI("/someother/url");
        assertFalse(filter.shouldFilter());

        // test docfinity
        mockHttpServletRequest.setRequestURI("/docfinity");
        assertTrue(filter.shouldFilter());

        // test docfinity with casing
        mockHttpServletRequest.setRequestURI("/DOCFINITY");
        assertTrue(filter.shouldFilter());

        // test docfinity with deep url
        mockHttpServletRequest.setRequestURI("/docfinity/very/long/path?query=string");
        assertTrue(filter.shouldFilter());
    }

    @Test
    public void shouldSucceedIfHeaderIsPresent() throws Exception {
        // arrange
        mockHttpServletRequest.addHeader("x-audituser", "someuser");

        // act
        filter.run();

        // assert
        assertThat(mockHttpServletResponse.getStatus(), is(200));
    }

    @Test
    public void shouldSucceedIfHeaderIsPresentWithMixedCasing() throws Exception {
        // arrange
        mockHttpServletRequest.addHeader("x-AUDITUSER", "someuser");

        // act
        filter.run();

        // assert
        assertThat(mockHttpServletResponse.getStatus(), is(200));
    }

    @Test
    public void shouldFailIfHeaderIsAbsent() throws Exception {
        // act
        filter.run();

        // assert
        assertThat(mockHttpServletResponse.getStatus(), is(400));
        assertEquals("Header 'x-audituser' is required for requests to '/docfinity'.",
                    RequestContext.getCurrentContext().getResponseBody());
    }

    @Test
    public void shouldFailIfHeaderIsPresentWithMissingValue() throws Exception {
        // arrange
        mockHttpServletRequest.addHeader("x-audituser", "");

        // act
        filter.run();

        // assert
        assertThat(mockHttpServletResponse.getStatus(), is(400));
        assertEquals("Header 'x-audituser' is required for requests to '/docfinity'.",
                    RequestContext.getCurrentContext().getResponseBody());
    }
}
