package com.savt.listopia.repository;

import com.savt.listopia.model.translation.MovieTranslation;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieTranslationRepository extends JpaRepository<MovieTranslation, Long> {
    MovieTranslation findByMovieMovieIdAndLanguage(Integer movieId, String language);

    @Query("SELECT mt FROM MovieTranslation mt WHERE mt.translationId = :translationId AND mt.movie.movieId = :movieId")
    MovieTranslation findMovieTranslationsByTranslationIdAndMovieMovieId(Long translationId, Integer movieId);

    MovieTranslation findMovieTranslationByMovieMovieIdAndLanguage(Integer movieId, String language, Limit limit);
}
