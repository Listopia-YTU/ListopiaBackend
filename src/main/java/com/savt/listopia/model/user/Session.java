package com.savt.listopia.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.Data;

@Data
@Entity(name = "sessions")
public class Session {
    @Id
    UUID uuid = UUID.randomUUID();

    Long userId;
    String ipAddress; // will be none for now.

    long createdAt;
    long expiresAt;
}
