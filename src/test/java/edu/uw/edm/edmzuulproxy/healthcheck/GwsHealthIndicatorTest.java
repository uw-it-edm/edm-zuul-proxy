package edu.uw.edm.edmzuulproxy.healthcheck;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.springframework.boot.actuate.health.Status;

import edu.uw.edm.gws.GroupsWebServiceClient;
import edu.uw.edm.gws.model.GWSSearchType;
import edu.uw.edm.gws.model.GroupReference;

import java.util.List;
import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class GwsHealthIndicatorTest {

    @Mock
    private GroupsWebServiceClient groupsWebServiceClient;

    GwsHealthIndicator gwsHealthIndicator;

    @Before
    public void setUp() {
        this.gwsHealthIndicator = new GwsHealthIndicator(groupsWebServiceClient);
    }

    @Test
    public void isUp() {
        List<GroupReference> groupsForUser = new ArrayList();
        when(groupsWebServiceClient.getGroupsForUser("edmsci", GWSSearchType.direct)).thenReturn(groupsForUser);
        assertThat(gwsHealthIndicator.health().getStatus(), is(Status.UP));
    }

    @Test
    public void isDown() {
        when(groupsWebServiceClient.getGroupsForUser("edmsci", GWSSearchType.direct)).thenThrow(new RuntimeException("test"));
        assertThat(gwsHealthIndicator.health().getStatus(), is(Status.DOWN));
    }

}
