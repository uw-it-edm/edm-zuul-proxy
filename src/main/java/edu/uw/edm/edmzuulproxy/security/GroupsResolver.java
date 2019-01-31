package edu.uw.edm.edmzuulproxy.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author Maxime Deravet Date: 2019-01-29
 */

public interface  GroupsResolver {


     Collection<? extends GrantedAuthority> getGroupsForUser(String username);
}
