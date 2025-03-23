package com.savt.listopia.util;

import com.savt.listopia.exception.APIException;
import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.translation.GenreTranslation;
import com.savt.listopia.repository.GenreRepository;
import com.savt.listopia.repository.GenreTranslationRepository;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbGenre;
import info.movito.themoviedbapi.model.core.Genre;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.tools.TmdbException;
import jakarta.transaction.Transactional;
import org.hibernate.TransientObjectException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FetchUtil {
    private final GenreTranslationRepository genreTranslationRepository;
    private final GenreRepository genreRepository;

    @Value("${tmdb.apiKey}")
    private String tmdbKey;

    private final ModelMapper modelMapper;

    public FetchUtil(GenreTranslationRepository genreTranslationRepository, GenreRepository genreRepository, ModelMapper modelMapper) {
        this.genreTranslationRepository = genreTranslationRepository;
        this.genreRepository = genreRepository;
        this.modelMapper = modelMapper;
    }

    public void fetchGenres() throws TmdbException {
        TmdbApi tmdbApi = new TmdbApi(tmdbKey);

        TmdbGenre tmdbGenre = tmdbApi.getGenre();
        List<Genre> genresFromTmdb;

        try {
            genresFromTmdb = tmdbGenre.getMovieList("en");
        } catch (TmdbException e) {
            throw new TmdbException(e.getMessage());
        }

        List<com.savt.listopia.model.core.Genre> genres = genresFromTmdb.stream()
                .map(g -> modelMapper.map(g, com.savt.listopia.model.core.Genre.class))
                .toList();

        genreRepository.saveAll(genres);
    }

    public void fetchGenreTranslations(String language) throws TmdbException {
        TmdbApi tmdbApi = new TmdbApi(tmdbKey);

        TmdbGenre tmdbGenre = tmdbApi.getGenre();
        List<Genre> genresFromTmdb;

        try {
            genresFromTmdb = tmdbGenre.getMovieList(language);
        } catch (TmdbException e) {
            throw new TmdbException(e.getMessage());
        }

        List<com.savt.listopia.model.core.Genre> genres = genresFromTmdb.stream()
                .map(g -> modelMapper.map(g, com.savt.listopia.model.core.Genre.class))
                .toList();

        for (int j = 0; j < genres.size(); j++) {
            com.savt.listopia.model.core.Genre genre = genres.get(j);
            GenreTranslation genreTranslation = new GenreTranslation();
            genreTranslation.setLanguage(language);
            genreTranslation.setName(genresFromTmdb.get(j).getName());
            genreTranslation.setGenre(genre);

            try {
                genreTranslationRepository.save(genreTranslation);
            } catch (Exception e) {
                throw new APIException("Fetch genres first!");
            }
        }
    }
}
