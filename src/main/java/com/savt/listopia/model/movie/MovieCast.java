package com.savt.listopia.model.movie;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.savt.listopia.model.people.Person;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movie_casts")
public class MovieCast {
    @Id
    @JoinColumn(name = "cast_id")
    private Integer castId;

    private String originalName;

    private Double popularity;

    private String profilePath;

    @Column(columnDefinition = "TEXT", length = 512)
    private String character;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;
}
