package com.savt.listopia.repository;

import com.savt.listopia.model.user.User;

import java.util.Optional;
import java.util.UUID;

import com.savt.listopia.payload.dto.MovieDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByUuid(UUID uuid);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
