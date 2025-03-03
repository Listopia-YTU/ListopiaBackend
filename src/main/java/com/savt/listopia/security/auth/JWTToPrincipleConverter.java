package com.savt.listopia.security.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

@Component
public class JWTToPrincipleConverter {
    public UserPrinciple convert(DecodedJWT jwt) {
        return UserPrinciple.builder()
            .userId(jwt.getSubject())
            .email(jwt.getClaim("email").asString())
            .build();
    }
}
