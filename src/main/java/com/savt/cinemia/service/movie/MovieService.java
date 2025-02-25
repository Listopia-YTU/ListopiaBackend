package com.savt.cinemia.service.movie;

import com.savt.cinemia.payload.dto.MovieDTO;
import com.savt.cinemia.payload.response.MovieResponse;
import jakarta.validation.Valid;

public interface MovieService {
    MovieResponse getAllMovies();

    MovieDTO createMovie(@Valid MovieDTO movieDTO);
}
