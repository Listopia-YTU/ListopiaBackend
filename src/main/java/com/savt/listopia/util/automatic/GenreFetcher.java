package com.savt.listopia.util.automatic;

import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.translation.GenreTranslation;
import com.savt.listopia.repository.GenreRepository;
import com.savt.listopia.repository.GenreTranslationRepository;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbGenre;
import info.movito.themoviedbapi.model.core.Genre;
import info.movito.themoviedbapi.model.movies.MovieDb;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GenreFetcher {

    private final GenreTranslationRepository genreTranslationRepository;
    @Value("${tmdb.apiKey}")
    private String tmdbKey;

    public GenreFetcher(GenreTranslationRepository genreTranslationRepository) {
        this.genreTranslationRepository = genreTranslationRepository;
    }

    //    @Bean
    public CommandLineRunner initGenres(GenreRepository genreRepository) {
        return args -> {
            ModelMapper modelMapper = new ModelMapper();
            TmdbApi tmdbApi = new TmdbApi(tmdbKey);

            modelMapper.typeMap(MovieDb.class, Movie.class).addMappings(mapper -> {
                mapper.skip(Movie::setGenres);
            });

            TmdbGenre tmdbGenre = tmdbApi.getGenre();
            List<Genre> genresFromTmdb = tmdbGenre.getMovieList("en");

            List<com.savt.listopia.model.core.Genre> genres = genresFromTmdb.stream()
                    .map(g -> modelMapper.map(g, com.savt.listopia.model.core.Genre.class))
                    .toList();

            genreRepository.saveAll(genres);


//            String[] langs = {"en", "de", "it", "fr", "es", "ru", "cs", "pt", "pl", "hu", "nl", "lt", "tr", "he", "el", "zh", "da", "ro", "ko", "uk", "pt", "fi", "bg", "sk", "sv", "es", "ja", "ka", "lv", "hr", "ca", "th", "et"};

            String[] langs = {"tr"};
            for (int y = 0; y < langs.length; y++) {
                genresFromTmdb = tmdbGenre.getMovieList(langs[y]);
                for (int j = 0; j < genres.size(); j++) {
                    com.savt.listopia.model.core.Genre genre = genres.get(j);
                    GenreTranslation genreTranslation = new GenreTranslation();
                    genreTranslation.setLanguage(langs[y]);
                    genreTranslation.setName(genresFromTmdb.get(j).getName());
                    genreTranslation.setGenre(genre);
                    genreTranslationRepository.save(genreTranslation);
                }
            }


            System.out.println("SAVED GENRES");
        };

    }

}
