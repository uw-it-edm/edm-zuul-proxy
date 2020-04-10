package edu.uw.edm.edmzuulproxy.healthcheck;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import edu.uw.edm.edmzuulproxy.security.GroupsResolver;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GwsHealthIndicator implements HealthIndicator {

    private GroupsResolver groupsResolver;

    @Autowired
    public GwsHealthIndicator(GroupsResolver groupsResolver) {
        this.groupsResolver = groupsResolver;
    }

    @Override
    public Health health() {
        // hardcode app user name for simplicity
        // the value is not important here. Even an invalid netid would work.
        // for an invalid netid, the call to GWS would succeed and return an empty list.
        String username = "edmsci";
        try {
            this.groupsResolver.getGroupsForUserDirectMembership(username);
            return Health.up().build();
        } catch (Exception e) {
            log.error("exception calling groupsWebServiceClient.getGroupsForUser(" + username + "): " + e.getMessage(), e);
            return Health.down().withDetail("Error calling GWS.getGroupsForUser", e.getMessage()).build();
        }
    }
}

