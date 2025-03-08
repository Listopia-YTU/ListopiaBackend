package com.savt.listopia.service;

import com.savt.listopia.payload.dto.MovieDTO;
import com.savt.listopia.payload.dto.MovieTranslationDTO;
import com.savt.listopia.payload.response.MovieFrontResponse;
import jakarta.validation.constraints.Max;

public interface MovieService {
    MovieFrontResponse getFrontMovies(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String genre, String language);

    MovieDTO getMovie(Integer movieId, String language);

    MovieDTO updateMovie(Integer movieId, MovieDTO movieDTO);

    MovieTranslationDTO addTranslation(Integer movieId, MovieTranslationDTO movieTranslationDTO);

    MovieTranslationDTO deleteTranslation(Integer movieId, Long translationId);

    MovieDTO fetchFromExternalDb(Integer movieId);

    MovieFrontResponse getFrontMoviesByWord(Integer pageNumber, @Max(50) Integer pageSize, String sortBy, String sortOrder, String genre, String language, String word);
}
