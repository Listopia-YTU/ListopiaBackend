package com.savt.listopia.controller;

import com.savt.listopia.config.AppConstants;
import com.savt.listopia.model.user.UserRole;
import com.savt.listopia.payload.APIResponse;
import com.savt.listopia.payload.dto.MovieTranslationDTO;
import com.savt.listopia.service.AuthServiceImpl;
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
@RequestMapping("/api/v1/admin/fetch")
public class FetchController {
    @Autowired
    private FetchUtil fetchUtil;
    @Autowired
    private AuthServiceImpl authServiceImpl;

    @PostMapping("/genres")
    public ResponseEntity<String> fetchGenres() throws TmdbException {
        authServiceImpl.requireRoleOrThrow(UserRole.ADMIN);
        fetchUtil.fetchGenres();
        return new ResponseEntity<>("Fetched genres", HttpStatus.CREATED);
    }

    @PostMapping("/genres/translations")
    public ResponseEntity<String> fetchGenreTranslation(@RequestParam(name = "language") String language) throws TmdbException {
        authServiceImpl.requireRoleOrThrow(UserRole.ADMIN);

        fetchUtil.fetchGenreTranslations(language);
        return new ResponseEntity<>("Fetched genre translations for language: " + language, HttpStatus.CREATED);
    }

    @PostMapping("/movies")
    public ResponseEntity<String> fetchMovies(@Min(1) @RequestParam(name = "startId", defaultValue = "1", required = false) Integer startId,
                                              @Max(1000001) @RequestParam(name = "endId", defaultValue = "100", required = false) Integer endId,
                                              @Min(0) @RequestParam(name = "minPopularity", defaultValue = "20", required = false) Integer minPopularity,
                                              @RequestParam(name = "fetchAllImages", defaultValue = "false", required = false) Boolean fetchAllImages,
                                              @RequestParam(name = "fetchKeywords", defaultValue = "false", required = false) Boolean fetchKeywords) {
        authServiceImpl.requireRoleOrThrow(UserRole.ADMIN);
        fetchUtil.fetchMovies(startId, endId, minPopularity, fetchAllImages, fetchKeywords);
        return new ResponseEntity<>("Fetching movies is completed", HttpStatus.CREATED);
    }

    @PostMapping("/persons")
    public ResponseEntity<String> fetchPersons(@Min(1) @RequestParam(name = "startId", defaultValue = "1", required = false) Integer startId,
                                               @Max(1000001) @RequestParam(name = "endId", defaultValue = "100", required = false) Integer endId,
                                               @RequestParam(name = "downloadImages", defaultValue = "false", required = false) Boolean downloadImages) throws IOException {
        authServiceImpl.requireRoleOrThrow(UserRole.ADMIN);
        fetchUtil.fetchPersons(startId, endId, downloadImages);
        return new ResponseEntity<>("Fetching persons is completed", HttpStatus.CREATED);
    }

    @PostMapping("/movies/{movieId}/images/backdrops")
    public ResponseEntity<String> downloadBackdrops(@PathVariable Integer movieId) throws TmdbException, IOException {
        authServiceImpl.requireRoleOrThrow(UserRole.ADMIN);
        fetchUtil.downloadBackdrops(movieId);
        return new ResponseEntity<>("Downloaded backdrops for movieId: " + movieId, HttpStatus.CREATED);
    }

    @PostMapping("/movies/{movieId}/images/posters")
    public ResponseEntity<String> downloadPosters(@PathVariable Integer movieId) throws IOException {
        authServiceImpl.requireRoleOrThrow(UserRole.ADMIN);
        fetchUtil.downloadPosters(movieId);
        return new ResponseEntity<>("Downloaded posters for movieId: " + movieId, HttpStatus.CREATED);
    }

    @PostMapping("/movies/{movieId}/images/logos")
    public ResponseEntity<String> downloadLogos(@PathVariable Integer movieId) throws IOException {
        authServiceImpl.requireRoleOrThrow(UserRole.ADMIN);
        fetchUtil.downloadLogos(movieId);
        return new ResponseEntity<>("Downloaded logos for movieId: " + movieId, HttpStatus.CREATED);
    }

    @PostMapping("/movies/{movieId}/images/cast")
    public ResponseEntity<String> downloadCastImages(@PathVariable Integer movieId) throws IOException {
        authServiceImpl.requireRoleOrThrow(UserRole.ADMIN);
        fetchUtil.downloadMovieCastImages(movieId);
        return new ResponseEntity<>("Downloaded cast images for movieId: " + movieId, HttpStatus.CREATED);
    }

}
