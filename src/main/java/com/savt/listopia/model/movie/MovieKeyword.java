package com.savt.listopia.model.movie;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "movie_keywords")
public class MovieKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movieKeywordId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;
}
