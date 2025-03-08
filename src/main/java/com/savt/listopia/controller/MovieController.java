package com.savt.listopia.controller;

import com.savt.listopia.config.AppConstants;
import com.savt.listopia.payload.dto.MovieDTO;
import com.savt.listopia.payload.dto.MovieTranslationDTO;
import com.savt.listopia.payload.response.MovieFrontResponse;
import com.savt.listopia.payload.response.MovieResponse;
import com.savt.listopia.service.MovieService;
import jakarta.validation.constraints.Max;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Validated
public class MovieController {
    @Autowired
    private MovieService movieService;

    @GetMapping("/movies/{movieId}")
    public ResponseEntity<MovieDTO> getMovie(@PathVariable Integer movieId,
                                             @RequestParam(name = "language", defaultValue = "en", required = false) String language) {
        MovieDTO movieDTO = movieService.getMovie(movieId, language);
        return new ResponseEntity<>(movieDTO, HttpStatus.OK);
    }

    @GetMapping("/movies/front")
    public ResponseEntity<MovieFrontResponse> getFrontMovies(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_MOVIES_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
            @RequestParam(name = "genre", required = false) String genre,
            @RequestParam(name = "language", defaultValue = "en", required = false) String language) {
        MovieFrontResponse movieFrontResponse = movieService.getFrontMovies(pageNumber, pageSize, sortBy, sortOrder, genre, language);
        return new ResponseEntity<>(movieFrontResponse, HttpStatus.OK);
    }

    @GetMapping("/movies/front/search")
    public ResponseEntity<MovieFrontResponse> getFrontMoviesByWord(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_MOVIES_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
            @RequestParam(name = "genre", required = false) String genre,
            @RequestParam(name = "language", defaultValue = "en", required = false) String language,
            @RequestParam String word) {
        MovieFrontResponse movieFrontResponse = movieService.getFrontMoviesByWord(pageNumber, pageSize, sortBy, sortOrder, genre, language, word);
        return new ResponseEntity<>(movieFrontResponse, HttpStatus.OK);
    }

    @PutMapping("/movies/{movieId}")
    public ResponseEntity<MovieDTO> updateMovie(@PathVariable Integer movieId,
                                                @RequestBody MovieDTO movieDTO) {
        MovieDTO savedMovieDTO = movieService.updateMovie(movieId, movieDTO);
        return new ResponseEntity<>(savedMovieDTO, HttpStatus.OK);
    }

    @PostMapping("/movies/{movieId}/translations")
    public ResponseEntity<MovieTranslationDTO> addTranslation(@PathVariable Integer movieId
            , @RequestBody MovieTranslationDTO movieTranslationDTO) {
        MovieTranslationDTO savedMovieTranslationDTO = movieService.addTranslation(movieId, movieTranslationDTO);
        return new ResponseEntity<>(savedMovieTranslationDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/movies/{movieId}/translations/{translationId}")
    public ResponseEntity<MovieTranslationDTO> deleteTranslation(@PathVariable Integer movieId, @PathVariable Long translationId) {
        MovieTranslationDTO deletedMovieTranslationDTO = movieService.deleteTranslation(movieId, translationId);
        return new ResponseEntity<>(deletedMovieTranslationDTO, HttpStatus.OK);
    }

    @GetMapping("/movies/{movieId}/fetch")
    public ResponseEntity<MovieDTO> fetchFromExternalDb(@PathVariable Integer movieId) {
        MovieDTO fetchedMovieDTO = movieService.fetchFromExternalDb(movieId);
        return new ResponseEntity<>(fetchedMovieDTO, HttpStatus.OK);
    }


}
