package com.savt.listopia.model.translation;

import com.savt.listopia.model.movie.Movie;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movie_translations")
public class MovieTranslation{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JoinColumn(name = "translation_id")
    private Integer translationId;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @JoinColumn(name = "iso_code")
    private String language;

    @Column(columnDefinition="TEXT", length = 512)
    private String title;

    @Column(columnDefinition="TEXT", length = 2048)
    private String overview;

    @Column(columnDefinition="TEXT", length = 2048)
    private String tagline;

}
