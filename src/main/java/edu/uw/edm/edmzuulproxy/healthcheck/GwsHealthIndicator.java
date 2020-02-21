package edu.uw.edm.edmzuulproxy.healthcheck;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import edu.uw.edm.gws.GroupsWebServiceClient;
import edu.uw.edm.gws.model.GWSSearchType;
import edu.uw.edm.gws.model.GroupReference;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GwsHealthIndicator implements HealthIndicator {

    private GroupsWebServiceClient groupsWebServiceClient;

    @Autowired
    public GwsHealthIndicator(GroupsWebServiceClient groupsWebServiceClient) {
        this.groupsWebServiceClient = groupsWebServiceClient;
    }

    @Override
    public Health health() {
        String username = "edmsci"; // hardcode app user name for simplicity
        try {
            List<GroupReference> groupsForUser = groupsWebServiceClient.getGroupsForUser(username, GWSSearchType.direct);
            return Health.up().build();
        } catch (Exception e) {
            log.error("exception calling groupsWebServiceClient.getGroupsForUser(" + username +"): " + e.getMessage(), e);
            return Health.down().withDetail("Error calling GWS.getGroupsForUser", e.getMessage()).build();
        }
    }
}

