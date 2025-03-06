package com.savt.listopia.service;

import com.savt.listopia.exception.ResourceNotFoundException;
import com.savt.listopia.model.core.Genre;
import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.translation.GenreTranslation;
import com.savt.listopia.model.translation.MovieTranslation;
import com.savt.listopia.payload.dto.MovieDTO;
import com.savt.listopia.payload.dto.MovieFrontDTO;
import com.savt.listopia.payload.response.MovieFrontResponse;
import com.savt.listopia.repository.GenreTranslationRepository;
import com.savt.listopia.repository.ImageRepository;
import com.savt.listopia.repository.MovieRepository;
import com.savt.listopia.repository.MovieTranslationRepository;
import info.movito.themoviedbapi.model.movies.MovieDb;
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

    @Autowired
    private MovieTranslationRepository movieTranslationRepository;
    @Autowired
    private GenreTranslationRepository genreTranslationRepository;

    @Override
    public MovieFrontResponse getFrontMovies(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String genre, String language) {
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

        if (!(language.equals("en"))) {
            for (Movie movie: movies){
                MovieTranslation movieTranslation = movieTranslationRepository
                        .findByMovieMovieIdAndLanguage(movie.getMovieId(), language);


                if (movieTranslation == null){
                    continue;
                }

                String title = movieTranslation.getTitle();

                if (title == null){
                    continue;
                }

                if (title.isEmpty() || title.isBlank()){
                    continue;
                }

                movie.setTitle(movieTranslation.getTitle());
            }
        }

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

    @Override
    public MovieDTO getMovie(Integer movieId, String language) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "movieId", movieId));

        MovieTranslation movieTranslation = movieTranslationRepository
                .findByMovieMovieIdAndLanguage(movieId, language);

        if (!(language.equals("en"))) {
            if (movieTranslation != null){
                String title = movieTranslation.getTitle();

                if (title != null){
                    if (!(title.isEmpty() || title.isBlank())){
                        movie.setTitle(title);
                    }
                }

                String overview = movieTranslation.getOverview();

                if (overview != null){
                    if (!(overview.isEmpty() || overview.isBlank())){
                        movie.setOverview(overview);
                    }
                }

                String tagline = movieTranslation.getTagline();

                if (tagline != null){
                    if (!(tagline.isEmpty() || tagline.isBlank())){
                        movie.setTagline(tagline);
                    }
                }
            }
        }

        MovieDTO movieDTO = modelMapper.map(movie, MovieDTO.class);

        List<Genre> genres = movieDTO.getGenres();

        for (Genre genre: genres){
            GenreTranslation genreTranslation = genreTranslationRepository.findGenreTranslationByGenreAndLanguage(genre, language);
            if (genreTranslation != null){
                String translatedGenreName  = genreTranslation.getName();

                if (translatedGenreName != null){
                    if (!(translatedGenreName.isEmpty() || translatedGenreName.isBlank())){
                        genre.setName(translatedGenreName);
                    }
                }
            }
        }


        movieDTO.setBackdrop(imageRepository.findMovieImageByMovieIdAndType(movieId, Limit.of(1), 1));
        movieDTO.setPoster(imageRepository.findMovieImageByMovieIdAndType(movieId, Limit.of(1), 2));
        movieDTO.setLogo(imageRepository.findMovieImageByMovieIdAndType(movieId, Limit.of(1), 3));
        return movieDTO;
    }
}
