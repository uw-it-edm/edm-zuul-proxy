package edu.uw.edm.edmzuulproxy.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Maxime Deravet Date: 10/20/17
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class User extends org.springframework.security.core.userdetails.User {

    public User(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }


}
