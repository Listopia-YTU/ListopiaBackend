package com.savt.listopia.repository;

import com.savt.listopia.model.translation.MovieTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

@Repository
public interface MovieTranslationRepository extends JpaRepository<MovieTranslation, Long> {
    MovieTranslation findByMovieMovieIdAndLanguage(Integer movieId, String language);
}
