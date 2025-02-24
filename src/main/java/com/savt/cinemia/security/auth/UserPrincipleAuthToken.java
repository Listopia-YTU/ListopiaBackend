package com.savt.cinemia.security.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class UserPrincipleAuthToken extends AbstractAuthenticationToken {
    private final UserPrinciple principle;

    public UserPrincipleAuthToken(UserPrinciple principle) {
        super(principle.getAuthorities());
        this.principle = principle;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public UserPrinciple getPrincipal() {
        return principle;
    }
}
