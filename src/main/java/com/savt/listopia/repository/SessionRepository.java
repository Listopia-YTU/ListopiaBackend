package com.savt.listopia.repository;

import com.savt.listopia.model.user.Session;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Session findByUuid(UUID uuid);
}
