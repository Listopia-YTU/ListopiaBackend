package com.savt.listopia.repository;

import com.savt.listopia.model.core.Genre;
import com.savt.listopia.model.translation.GenreTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreTranslationRepository extends JpaRepository<GenreTranslation, Integer> {
    GenreTranslation findGenreTranslationByGenreAndLanguage(Genre genre, String language);
}
