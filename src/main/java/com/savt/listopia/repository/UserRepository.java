package com.savt.listopia.repository;

import com.savt.listopia.model.user.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByUsername(String username);
    User findByUuid(UUID uuid);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
