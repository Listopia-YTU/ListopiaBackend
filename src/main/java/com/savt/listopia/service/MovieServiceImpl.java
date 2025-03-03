package com.savt.listopia.service;

import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.payload.dto.MovieDTO;
import com.savt.listopia.payload.dto.MovieFrontDTO;
import com.savt.listopia.payload.response.MovieFrontResponse;
import com.savt.listopia.payload.response.MovieResponse;
import com.savt.listopia.repository.ImageRepository;
import com.savt.listopia.repository.MovieRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Override
    public MovieResponse getMovies(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String genre) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Movie> pageMovies;

        if (genre != null) {
            pageMovies = movieRepository.findAllByGenreName(pageDetails, genre);
        } else {
            pageMovies = movieRepository.findAll(pageDetails);
        }

        List<Movie> movies = pageMovies.getContent();

        List<MovieDTO> movieDTOS = movies.stream()
                .map(product -> modelMapper.map(product, MovieDTO.class))
                .toList();

        MovieResponse movieResponse = new MovieResponse();
        movieResponse.setContent(movieDTOS);
        movieResponse.setPageNumber(pageMovies.getNumber());
        movieResponse.setPageSize(pageMovies.getSize());
        movieResponse.setTotalElements(pageMovies.getTotalElements());
        movieResponse.setTotalPages(pageMovies.getTotalPages());
        movieResponse.setLastPage(pageMovies.isLast());
        return movieResponse;
    }

    @Override
    public MovieFrontResponse getFrontMovies(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String genre) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Movie> pageMovies;

        if (genre != null) {
            pageMovies = movieRepository.findAllByGenreName(pageDetails, genre);
        } else {
            pageMovies = movieRepository.findAll(pageDetails);
        }

        List<Movie> movies = pageMovies.getContent();

        List<MovieFrontDTO> movieFrontDTOS = movies.stream()
                .map(product -> modelMapper.map(product, MovieFrontDTO.class))
                .toList();

        movieFrontDTOS.forEach(movieFrontDTO -> movieFrontDTO
                .setPoster(
                        imageRepository.findMovieImageByMovieId(movieFrontDTO.getMovieId(), Limit.of(1))
                ));

        MovieFrontResponse movieFrontResponse = new MovieFrontResponse();
        movieFrontResponse.setContent(movieFrontDTOS);
        movieFrontResponse.setPageNumber(pageMovies.getNumber());
        movieFrontResponse.setPageSize(pageMovies.getSize());
        movieFrontResponse.setTotalElements(pageMovies.getTotalElements());
        movieFrontResponse.setTotalPages(pageMovies.getTotalPages());
        movieFrontResponse.setLastPage(pageMovies.isLast());
        return movieFrontResponse;
    }
}
