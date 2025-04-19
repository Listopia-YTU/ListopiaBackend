package com.savt.listopia.service;

import com.savt.listopia.model.user.Session;
import com.savt.listopia.model.user.User;
import com.savt.listopia.repository.SessionRepository;
import com.savt.listopia.security.auth.AuthenticationToken;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Cookie createCookie(Session session) {
        Cookie cookie = new Cookie("_SESSION", session.getUuid().toString());

        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        return cookie;
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
