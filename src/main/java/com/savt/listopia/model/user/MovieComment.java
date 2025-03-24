package com.savt.listopia.model.user;

import com.savt.listopia.model.movie.Movie;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "movie_comments")
public class MovieComment {
    @Id
    @GeneratedValue
    Long id;

    @ManyToOne
    @JoinColumn(name = "from_user", nullable = false)
    User fromUser;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    Movie movie;

    Long sentAtTimestampSeconds;

    Boolean isReported = false;
    Boolean isSpoiler;

    @Column(columnDefinition = "TEXT", length = 4096)
    String message;
}
