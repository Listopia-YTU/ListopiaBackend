package com.savt.listopia.controller;

import com.savt.listopia.config.AppConstants;
import com.savt.listopia.payload.response.MovieFrontResponse;
import com.savt.listopia.payload.response.MovieResponse;
import com.savt.listopia.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping("/movies")
    public ResponseEntity<MovieResponse> getMovies(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.SLIDE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_MOVIES_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
            @RequestParam(name = "genre", required = false) String genre) {
        MovieResponse movieResponse = movieService.getMovies(pageNumber, pageSize, sortBy, sortOrder, genre);
        return new ResponseEntity<>(movieResponse, HttpStatus.OK);
    }

    @GetMapping("/movies/front")
    public ResponseEntity<MovieFrontResponse> getFrontMovies(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.SLIDE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_MOVIES_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
            @RequestParam(name = "genre", required = false) String genre) {
        MovieFrontResponse movieFrontResponse = movieService.getFrontMovies(pageNumber, pageSize, sortBy, sortOrder, genre);
        return new ResponseEntity<>(movieFrontResponse, HttpStatus.OK);
    }
}
