package com.savt.cinemia.controller;

import com.savt.cinemia.payload.dto.MovieDTO;
import com.savt.cinemia.payload.response.MovieResponse;
import com.savt.cinemia.service.movie.MovieService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MovieController {

    @Autowired
    private MovieService movieService;

    /**
     * Pagination will be added later
     *
     */
    @GetMapping("/movies")
    public ResponseEntity<MovieResponse> getAllMovies(){
        MovieResponse movieResponse = movieService.getAllMovies();
        return new ResponseEntity<>(movieResponse, HttpStatus.OK);
    }

    @PostMapping("/movies")
    public ResponseEntity<MovieDTO> createMovie(@Valid @RequestBody MovieDTO movieDTO){
        MovieDTO savedMovieDTO = movieService.createMovie(movieDTO);
        return new ResponseEntity<>(savedMovieDTO, HttpStatus.CREATED);
    }

}
