package com.savt.cinemia.security;

import com.savt.cinemia.middleware.AuthMiddleware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private final AuthMiddleware authMiddleware;

    public WebSecurityConfig(AuthMiddleware authMiddleware) {
        this.authMiddleware = authMiddleware;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
            .addFilterBefore(authMiddleware, OncePerRequestFilter.class);

        return http.build();
    }
}