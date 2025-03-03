package com.savt.listopia.security.auth;

import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {
    private static final long SESSION_EXPIRY_TIME = 7 * 24 * 60 * 60 * 1000; // 1 hafta
    private HashMap<String, Session> sessionStore =
        new HashMap<>(); // bunu database ile değiştirmek lazım
    private HashMap<String, String> userStorage =
        new HashMap<>(); // bunu da database ile değiştirmek lazım

    public String createSessionToken(String userId) {
        String sessionId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        Session session = new Session(sessionId, userId, now, now + SESSION_EXPIRY_TIME);
        sessionStore.put(sessionId, session);
        return sessionId;
    }

    // sessionId ile mevcut session'u almaya ve gerekirse güncellemeye çalışır.
    public Session getSession(String sessionId) {
        Session session = sessionStore.get(sessionId);
        if (session != null && session.getExpiresAt() > System.currentTimeMillis()) {
            // @TODO: Update session expire date
            return session;
        }
        sessionStore.remove(sessionId);
        return null;
    }

    // Var olan sessiondan UserPrinciple' objesi üretir.
    public UserPrinciple getPrinciple(Session session) {
        String userId = userStorage.get(session.getUserId());
        if (userId != null) {
            // var olan userId ile database'den bilgi çekip UserPrinciple yaratıyoruz.
            // @TODO: database verisinden email falan da alacağız.
            return UserPrinciple.builder().userId(userId).build();
        }
        return null;
    }

    public Cookie createCookieForUserId(String userId) {
        String token = createSessionToken(userId);
        Cookie session = new Cookie("_SESSIONID", token);
        session.setHttpOnly(true);
        session.setSecure(true);
        session.setPath("/");
        // session.setMaxAge();
        // session.setDomain("listopia.shop");
        return session;
    }

    public void invalidateSession(String sessionId) { sessionStore.remove(sessionId); }
}
