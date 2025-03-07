package com.savt.listopia.model.movie;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.savt.listopia.model.core.Genre;
import com.savt.listopia.model.core.image.MovieImage;
import com.savt.listopia.model.translation.MovieTranslation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movies")
public class Movie {
    @Id
    private Integer movieId;

    private String originalLanguage;

    private String originalTitle;

    private String title;

    @Column(columnDefinition = "TEXT", length = 2048)
    private String overview;

    @Column(columnDefinition = "TEXT", length = 2048)
    private String tagline;

    private Double popularity;

    private String releaseDate;

    private String trailerKey;

    private String trailerLink;

    private Integer runtime;

    //

    private Integer watchCount;

    private Integer likeCount;

    private Double ratingAverage;

    private Integer ratingCount;

    //

    private String imdbId;

    private String wikidataId;

    private String facebookId;

    private String instagramId;

    private String twitterId;

    @OneToMany(mappedBy = "movie", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    @JsonManagedReference
    private List<MovieImage> backdrops;

    @JsonManagedReference
    @OneToMany(mappedBy = "movie", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<MovieImage> logos;

    @JsonManagedReference
    @OneToMany(mappedBy = "movie", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<MovieImage> posters;

    @JsonManagedReference
    @OneToMany(mappedBy = "movie", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<MovieKeyword> keywords;

    @ManyToMany
    @JoinTable(name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private List<Genre> genres;

    @OneToMany(mappedBy = "movie", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<MovieTranslation> translations;

}
