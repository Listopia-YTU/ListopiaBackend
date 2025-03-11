package com.savt.listopia.model.comment;

import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;

@EqualsAndHashCode(callSuper = false)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movie_comments")
public class MovieComment extends Comment {
    @Column(columnDefinition = "TEXT", length = 1024)
    private String content;

    private LocalDateTime creationDate;

    private String profilePicture;

    private String profileName;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

}
