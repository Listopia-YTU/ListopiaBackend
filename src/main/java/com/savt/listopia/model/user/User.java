package com.savt.listopia.model.user;

import com.savt.listopia.model.movie.Movie;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    Long id;

    UUID uuid = UUID.randomUUID();

    String username;
    String firstName;
    String lastName;

    String email;
    String hashedPassword;

    @Nullable
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @ManyToMany
    @JoinTable(
            name = "user_liked_movies",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id")
    )
    List<Movie> likedMovies = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    List<User> friends = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_friend_requests",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "requested_friend_id")
    )
    List<User> friendRequests = new ArrayList<>();

}
