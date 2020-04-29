package edu.uw.edm.edmzuulproxy.healthcheck;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Status;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

import edu.uw.edm.edmzuulproxy.security.GroupsResolver;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GwsHealthIndicatorTest {

    @Mock
    private GroupsResolver groupsResolver;

    GwsHealthIndicator gwsHealthIndicator;

    @Before
    public void setUp() {
        this.gwsHealthIndicator = new GwsHealthIndicator(groupsResolver);
    }

    @Test
    public void isUp() {
        List<SimpleGrantedAuthority> groupsForUser = new ArrayList();
        when(groupsResolver.getGroupsForUserDirectMembership("edmsci")).thenAnswer(x -> groupsForUser);
        assertThat(gwsHealthIndicator.health().getStatus(), is(Status.UP));
    }

    @Test
    public void isDown() {
        when(groupsResolver.getGroupsForUserDirectMembership("edmsci")).thenThrow(new RuntimeException("test"));
        assertThat(gwsHealthIndicator.health().getStatus(), is(Status.DOWN));
    }
}
