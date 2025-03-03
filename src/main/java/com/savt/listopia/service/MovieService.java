package com.savt.listopia.service;

import com.savt.listopia.payload.response.MovieFrontResponse;
import com.savt.listopia.payload.response.MovieResponse;

public interface MovieService {
    MovieResponse getMovies(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String genre);

    MovieFrontResponse getFrontMovies(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String genre);
}
