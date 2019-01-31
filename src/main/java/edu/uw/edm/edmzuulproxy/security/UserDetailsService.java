package edu.uw.edm.edmzuulproxy.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;


/**
 * @author James Renfro
 */
@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private GroupsResolver groupsResolver;

    public UserDetailsService(GroupsResolver groupsResolver) {
        this.groupsResolver = groupsResolver;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (StringUtils.isEmpty(username)) {
            return null;
        }


        return new User(username, "", groupsResolver.getGroupsForUser(username));
    }

}
