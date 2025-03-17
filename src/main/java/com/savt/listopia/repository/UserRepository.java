package com.savt.listopia.repository;

import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByUuid(UUID uuid);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Query("SELECT m FROM User u JOIN u.likedMovies m WHERE u.id = :userId")
    List<Movie> findLikedMoviesByUserId(@Param("userId") Long userId);

}
