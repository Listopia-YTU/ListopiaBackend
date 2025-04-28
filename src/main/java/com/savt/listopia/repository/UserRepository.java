package com.savt.listopia.repository;

import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.user.User;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<Movie> findLikedMoviesByUserId(@Param("userId") Long userId, Pageable page);

    @Query("SELECT fr FROM User u JOIN u.friendRequestsReceived fr WHERE u.id = :userId")
    Page<User> findFriendRequestsReceivedByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT fr FROM User u JOIN u.friendRequestsSent fr WHERE u.id = :userId")
    Page<User> findFriendRequestsSentByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.friends f WHERE f.id = :userId")
    Page<User> findFriendsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT m FROM User u JOIN u.watchlist m WHERE u.id = :userId")
    Page<Movie> findWatchlistByUserId(@Param("userId") Long userId, Pageable page);

    @Query("SELECT m FROM User u JOIN u.watchedList m WHERE u.id = :userId")
    Page<Movie> findWatchedListByUserId(@Param("userId") Long userId, Pageable page);

}
