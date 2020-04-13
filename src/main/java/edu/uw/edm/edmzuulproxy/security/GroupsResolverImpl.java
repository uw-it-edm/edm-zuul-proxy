package edu.uw.edm.edmzuulproxy.security;

import com.google.common.base.Preconditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.uw.edm.gws.GroupsWebServiceClient;
import edu.uw.edm.gws.model.GWSSearchType;
import edu.uw.edm.gws.model.GroupReference;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Maxime Deravet Date: 2019-01-29
 */
@Slf4j
@Service
public class GroupsResolverImpl implements GroupsResolver {

    private GroupsWebServiceClient groupsWebServiceClient;

    @Autowired
    public GroupsResolverImpl(GroupsWebServiceClient groupsWebServiceClient) {
        this.groupsWebServiceClient = groupsWebServiceClient;
    }

    @Override
    @Cacheable(cacheNames = "user-groups")
    public Collection<? extends GrantedAuthority> getGroupsForUser(String username) {
        Preconditions.checkNotNull(username, "A username is required for getGroupsForUser");

        return this.getGroupsForUser(username, GWSSearchType.effective);
    }

    @Override
    @Cacheable(cacheNames = "direct-user-groups")
    public Collection<? extends GrantedAuthority> getGroupsForUserDirectMembership(String username) {
        Preconditions.checkNotNull(username, "A username is required for getGroupsForUserDirectMembership");

        return this.getGroupsForUser(username, GWSSearchType.direct);
    }

    private Collection<? extends GrantedAuthority> getGroupsForUser(String username, GWSSearchType searchType) {
        log.debug("Calling GWS to get '{}' membership for the user '{}'", searchType, username);

        final List<GroupReference> groupsForUser = groupsWebServiceClient.getGroupsForUser(username, searchType);

        return groupsForUser.stream().map(group -> new SimpleGrantedAuthority(group.getName())).collect(Collectors.toSet());
    }
}
