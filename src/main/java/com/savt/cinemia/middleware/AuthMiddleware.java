package com.savt.cinemia.middleware;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.savt.cinemia.model.User;
import com.savt.cinemia.security.auth.JWTDecoder;
import com.savt.cinemia.security.auth.JWTToPrincipleConverter;
import com.savt.cinemia.security.auth.UserPrinciple;
import com.savt.cinemia.security.auth.UserPrincipleAuthToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthMiddleware extends OncePerRequestFilter {
    private final JWTDecoder jwtDecoder;
    private final JWTToPrincipleConverter jwtToPrincipleConverter;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthMiddleware.class);

    public AuthMiddleware(JWTDecoder jwtDecoder, JWTToPrincipleConverter jwtToPrincipleConverter) {
        this.jwtDecoder = jwtDecoder;
        this.jwtToPrincipleConverter = jwtToPrincipleConverter;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {

        getTokenFromRequest(request)
            .map(jwtDecoder::decode)
            .map(jwtToPrincipleConverter::convert)
            .map(UserPrincipleAuthToken::new)
            .ifPresent(auth -> SecurityContextHolder.getContext().setAuthentication(auth));

        chain.doFilter(request, response);
    }

    Optional<String> getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if ( token == null ) return Optional.empty();
        if ( !token.startsWith("Bearer ") ) return Optional.empty();
        return Optional.of(token.substring(7));
    }

}
