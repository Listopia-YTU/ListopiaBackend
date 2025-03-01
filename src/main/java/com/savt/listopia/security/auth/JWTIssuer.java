package com.savt.listopia.security.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class JWTIssuer {
    public String issue(Long id, String name, String email) {
        return JWT.create()
            .withSubject(String.valueOf(id))
            .withExpiresAt(Instant.now().plus(Duration.of(5, ChronoUnit.MINUTES)))
            .withClaim("name", name)
            .withClaim("email", email)
            .sign(Algorithm.HMAC256("secret"));
    }
}
