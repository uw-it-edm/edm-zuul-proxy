package edu.uw.edm.edmzuulproxy.security;

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

/**
 * @author Maxime Deravet Date: 2019-01-29
 */
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

        final List<GroupReference> groupsForUser = groupsWebServiceClient.getGroupsForUser(username, GWSSearchType.effective);

        return groupsForUser.stream().map(group -> new SimpleGrantedAuthority(group.getName())).collect(Collectors.toSet());
    }
}
