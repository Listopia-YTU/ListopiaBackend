package com.savt.listopia.controller;

import com.savt.listopia.config.AppConstants;
import com.savt.listopia.payload.APIResponse;
import com.savt.listopia.payload.dto.MovieTranslationDTO;
import com.savt.listopia.util.FetchUtil;
import info.movito.themoviedbapi.tools.TmdbException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/test")
public class FetchController {
    @Autowired
    private FetchUtil fetchUtil;

    @PostMapping("/fetch/genres")
    public ResponseEntity<String> fetchGenres() throws TmdbException {
        fetchUtil.fetchGenres();
        return new ResponseEntity<>("Fetched genres", HttpStatus.CREATED);
    }

    @PostMapping("/fetch/genres/translations")
    public ResponseEntity<String> fetchGenreTranslation(@RequestParam(name = "language") String language) throws TmdbException {
        fetchUtil.fetchGenreTranslations(language);
        return new ResponseEntity<>("Fetched genre translations for language: " + language, HttpStatus.CREATED);
    }

    @PostMapping("/fetch/movies")
    public ResponseEntity<String> fetchMovies(@Min(1) @RequestParam(name = "startId", defaultValue = "1", required = false) Integer startId,
                                              @Max(1000001) @RequestParam(name = "endId", defaultValue = "100", required = false) Integer endId,
                                              @Min(0) @RequestParam(name = "minPopularity", defaultValue = "20", required = false) Integer minPopularity,
                                              @RequestParam(name = "fetchAllImages", defaultValue = "false", required = false) Boolean fetchAllImages,
                                              @RequestParam(name = "fetchKeywords", defaultValue = "false", required = false) Boolean fetchKeywords) {
        fetchUtil.fetchMovies(startId, endId, minPopularity, fetchAllImages, fetchKeywords);
        return new ResponseEntity<>("Fetching movies is completed", HttpStatus.CREATED);
    }

    @PostMapping("/fetch/persons")
    public ResponseEntity<String> fetchPersons(@Min(1) @RequestParam(name = "startId", defaultValue = "1", required = false) Integer startId,
                                               @Max(1000001) @RequestParam(name = "endId", defaultValue = "100", required = false) Integer endId,
                                               @RequestParam(name = "downloadImages", defaultValue = "false", required = false) Boolean downloadImages) throws IOException {
        fetchUtil.fetchPersons(startId, endId, downloadImages);
        return new ResponseEntity<>("Fetching persons is completed", HttpStatus.CREATED);
    }

    @PostMapping("/fetch/movies/{movieId}/images/backdrops")
    public ResponseEntity<String> downloadBackdrops(@PathVariable Integer movieId) throws TmdbException, IOException {
        fetchUtil.downloadBackdrops(movieId);
        return new ResponseEntity<>("Downloaded backdrops for movieId: " + movieId, HttpStatus.CREATED);
    }

    @PostMapping("/fetch/movies/{movieId}/images/posters")
    public ResponseEntity<String> downloadPosters(@PathVariable Integer movieId) throws IOException {
        fetchUtil.downloadPosters(movieId);
        return new ResponseEntity<>("Downloaded posters for movieId: " + movieId, HttpStatus.CREATED);
    }

    @PostMapping("/fetch/movies/{movieId}/images/logos")
    public ResponseEntity<String> downloadLogos(@PathVariable Integer movieId) throws IOException {
        fetchUtil.downloadLogos(movieId);
        return new ResponseEntity<>("Downloaded logos for movieId: " + movieId, HttpStatus.CREATED);
    }

    @PostMapping("/fetch/movies/{movieId}/images/cast")
    public ResponseEntity<String> downloadCastImages(@PathVariable Integer movieId) throws IOException {
        fetchUtil.downloadMovieCastImages(movieId);
        return new ResponseEntity<>("Downloaded cast images for movieId: " + movieId, HttpStatus.CREATED);
    }

}
