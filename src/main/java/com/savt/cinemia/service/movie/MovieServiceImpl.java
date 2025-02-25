package com.savt.cinemia.service.movie;

import com.savt.cinemia.exception.model.ResourceAlreadyExistException;
import com.savt.cinemia.model.movie.Movie;
import com.savt.cinemia.payload.dto.MovieDTO;
import com.savt.cinemia.payload.response.MovieResponse;
import com.savt.cinemia.repository.MovieRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService{

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public MovieResponse getAllMovies() {
        List<Movie> movies = movieRepository.findAll();

        List<MovieDTO> movieDTOS = movies.stream()
                .map(movie -> modelMapper.map(movie, MovieDTO.class))
                .toList();

        MovieResponse movieResponse = new MovieResponse();
        movieResponse.setContent(movieDTOS);
        return movieResponse;
    }

    @Override
    public MovieDTO createMovie(MovieDTO movieDTO) {
        Movie movie = modelMapper.map(movieDTO, Movie.class);
        Long movieId = movie.getMovieId();
        Movie movieFromDb = movieRepository.findMovieByMovieId(movieId);

        if (movieFromDb != null) {
            throw new ResourceAlreadyExistException("Movie", "movieId", movieId);
        }

        Movie savedMovie = movieRepository.save(movie);
        return modelMapper.map(savedMovie, MovieDTO.class);
    }


}
