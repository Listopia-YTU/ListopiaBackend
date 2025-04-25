package com.savt.listopia.service;

import com.savt.listopia.model.user.Session;
import com.savt.listopia.model.user.User;
import com.savt.listopia.repository.SessionRepository;
import com.savt.listopia.security.auth.AuthenticationToken;

import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
    private static final long SESSION_EXPIRY_TIME = 7 * 24 * 60 * 60 * 1000; // 1 hafta

    @Autowired
    SessionRepository sessionRepository;

    public Session createSession(User user) {
        Session session = new Session();

        long now = System.currentTimeMillis();
        long end = now + SESSION_EXPIRY_TIME;

        session.setCreatedAt(now);
        session.setUserId(user.getId());
        session.setExpiresAt(end);

        sessionRepository.save(session);

        return session;
    }

    public ResponseCookie createCookie(Session session) {
        return ResponseCookie.from("_SESSION", session.getUuid().toString())
                .secure(true)
                .httpOnly(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    public void deleteSession(Session session) {
        sessionRepository.delete(session);
    }

    public Session getSessionByUuid(String uuidStr) {
        UUID uuid = UUID.fromString(uuidStr);
        return sessionRepository.findByUuid(uuid);
    }

    public Session getCurrentSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return null;

        if (authentication instanceof AuthenticationToken authenticationToken) {
            return authenticationToken.getPrincipal();
        }

        return null;
    }
}
