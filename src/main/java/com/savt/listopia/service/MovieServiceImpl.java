package com.savt.listopia.service;

import com.savt.listopia.exception.APIException;
import com.savt.listopia.exception.ResourceAlreadyExistException;
import com.savt.listopia.exception.ResourceNotFoundException;
import com.savt.listopia.model.core.Genre;
import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.translation.GenreTranslation;
import com.savt.listopia.model.translation.MovieTranslation;
import com.savt.listopia.payload.dto.MovieDTO;
import com.savt.listopia.payload.dto.MovieFrontDTO;
import com.savt.listopia.payload.dto.MovieTranslationDTO;
import com.savt.listopia.payload.response.MovieFrontResponse;
import com.savt.listopia.repository.GenreTranslationRepository;
import com.savt.listopia.repository.MovieImageRepository;
import com.savt.listopia.repository.MovieRepository;
import com.savt.listopia.repository.MovieTranslationRepository;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.movies.ExternalIds;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.Translation;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.appendtoresponse.MovieAppendToResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.savt.listopia.config.AppConstants.SUPPORTED_LANGUAGES;

@Service
public class MovieServiceImpl implements MovieService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieImageRepository movieImageRepository;

    @Value("${tmdb.apiKey}")
    private String tmdbKey;

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
            for (Movie movie : movies) {
                MovieTranslation movieTranslation = movieTranslationRepository
                        .findByMovieMovieIdAndLanguage(movie.getMovieId(), language);

                if (movieTranslation == null) {
                    continue;
                }

                String title = movieTranslation.getTitle();

                if (title == null) {
                    continue;
                }

                if (title.isEmpty() || title.isBlank()) {
                    continue;
                }

                movie.setTitle(title);
            }
        }

        List<MovieFrontDTO> movieFrontDTOS = movies.stream()
                .map(product -> modelMapper.map(product, MovieFrontDTO.class))
                .toList();

        movieFrontDTOS.forEach(movieFrontDTO -> movieFrontDTO
                .setPoster(
                        movieImageRepository.findMovieImageByMovieId(movieFrontDTO.getMovieId(), Limit.of(1))
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
    public MovieFrontResponse getFrontMoviesByWord(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String genre, String language, String word) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Movie> pageMovies;


        if (genre != null) {
            pageMovies = movieRepository.findAllByGenreNameAndTitleLikeIgnoreCase(pageDetails, genre, '%' + word + '%');
        } else {
            pageMovies = movieRepository.findAllByTitleLikeIgnoreCase(pageDetails, '%' + word + '%');
        }

        List<Movie> movies = pageMovies.getContent();

        if (!(language.equals("en"))) {
            for (Movie movie : movies) {
                MovieTranslation movieTranslation = movieTranslationRepository
                        .findByMovieMovieIdAndLanguage(movie.getMovieId(), language);

                if (movieTranslation == null) {
                    continue;
                }

                String title = movieTranslation.getTitle();

                if (title == null) {
                    continue;
                }

                if (title.isEmpty() || title.isBlank()) {
                    continue;
                }

                movie.setTitle(title);
            }
        }

        List<MovieFrontDTO> movieFrontDTOS = movies.stream()
                .map(product -> modelMapper.map(product, MovieFrontDTO.class))
                .toList();

        movieFrontDTOS.forEach(movieFrontDTO -> movieFrontDTO
                .setPoster(
                        movieImageRepository.findMovieImageByMovieId(movieFrontDTO.getMovieId(), Limit.of(1))
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

        if (!(language.equals("en"))) {
            MovieTranslation movieTranslation = movieTranslationRepository
                    .findByMovieMovieIdAndLanguage(movieId, language);

            if (movieTranslation != null) {
                String title = movieTranslation.getTitle();

                if (title != null) {
                    if (!(title.isEmpty() || title.isBlank())) {
                        movie.setTitle(title);
                    }
                }

                String overview = movieTranslation.getOverview();

                if (overview != null) {
                    if (!(overview.isEmpty() || overview.isBlank())) {
                        movie.setOverview(overview);
                    }
                }

                String tagline = movieTranslation.getTagline();

                if (tagline != null) {
                    if (!(tagline.isEmpty() || tagline.isBlank())) {
                        movie.setTagline(tagline);
                    }
                }
            }
        }

        MovieDTO movieDTO = modelMapper.map(movie, MovieDTO.class);

        if (!(language.equals("en"))) {
            List<Genre> genres = movieDTO.getGenres();

            for (Genre genre : genres) {
                GenreTranslation genreTranslation = genreTranslationRepository.findGenreTranslationByGenreAndLanguage(genre, language);

                if (genreTranslation == null) {
                    continue;
                }

                String translatedGenreName = genreTranslation.getName();

                if (translatedGenreName == null) {
                    continue;
                }

                if (translatedGenreName.isEmpty() || translatedGenreName.isBlank()) {
                    continue;
                }

                genre.setName(translatedGenreName);
            }
        }

        movieDTO.setBackdrop(movieImageRepository.findMovieImageByMovieIdAndType(movieId, Limit.of(1), 1));
        movieDTO.setPoster(movieImageRepository.findMovieImageByMovieIdAndType(movieId, Limit.of(1), 2));
        movieDTO.setLogo(movieImageRepository.findMovieImageByMovieIdAndType(movieId, Limit.of(1), 3));
        return movieDTO;
    }

    @Override
    public MovieDTO updateMovie(Integer movieId, MovieDTO movieDTO) {
        Movie movieFromDb = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "movieId", movieId));

        Movie movie = modelMapper.map(movieDTO, Movie.class);
        movie.setMovieId(movieId);
        movie.setGenres(movieFromDb.getGenres());
        movie.setPosters(movieFromDb.getPosters());
        movie.setBackdrops(movieFromDb.getBackdrops());
        movie.setLogos(movieFromDb.getLogos());
        movie.setKeywords(movieFromDb.getKeywords());
        movie.setTranslations(movieFromDb.getTranslations());
        movie.setPopularity(movieFromDb.getPopularity());
        movie.setWatchCount(movieFromDb.getWatchCount());
        movie.setLikeCount(movieFromDb.getLikeCount());
        movie.setRatingCount(movieFromDb.getRatingCount());
        movie.setRatingAverage(movieFromDb.getRatingAverage());

        return modelMapper.map(movieRepository.save(movie), MovieDTO.class);
    }

    @Override
    public MovieTranslationDTO addTranslation(Integer movieId, MovieTranslationDTO movieTranslationDTO) {
        Movie movieFromDb = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "movieId", movieId));

        MovieTranslation movieTranslation = modelMapper.map(movieTranslationDTO, MovieTranslation.class);

        String language = movieTranslation.getLanguage();
        if (!(SUPPORTED_LANGUAGES.contains(language))) {
            throw new APIException("Language is not supported");
        }

        MovieTranslation movieTranslationFromDb = movieTranslationRepository.findMovieTranslationByMovieMovieIdAndLanguage(movieId, language, Limit.of(1));

        if (movieTranslationFromDb != null) {
            throw new ResourceAlreadyExistException("MovieTranslation", "language", language);
        }

        movieTranslation.setMovie(movieFromDb);
        movieFromDb.getTranslations().add(movieTranslation);
        movieRepository.save(movieFromDb);
        return modelMapper.map(movieTranslation, MovieTranslationDTO.class);
    }

    @Override
    public MovieTranslationDTO deleteTranslation(Integer movieId, Long translationId) {
        Movie movieFromDb = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "movieId", movieId));

        MovieTranslation movieTranslation = movieTranslationRepository.findMovieTranslationsByTranslationIdAndMovieMovieId(translationId, movieId);

        if (movieTranslation == null) {
            throw new ResourceNotFoundException("MovieTranslation", "translationId", "translationId");
        }

        movieFromDb.getTranslations().remove(movieTranslation);
        movieTranslationRepository.delete(movieTranslation);
        movieRepository.save(movieFromDb);
        return modelMapper.map(movieTranslation, MovieTranslationDTO.class);
    }

    @Override
    public MovieDTO fetchFromExternalDb(Integer movieId) {
        Movie movieFromDb = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "movieId", movieId));

        TmdbApi tmdbApi = new TmdbApi(tmdbKey);
        TmdbMovies movies = tmdbApi.getMovies();

        modelMapper.typeMap(MovieDb.class, Movie.class).addMappings(mapper -> {
            mapper.skip(Movie::setGenres);
        });

        modelMapper.typeMap(Translation.class, MovieTranslation.class).addMappings(mapper -> {
            mapper.map(Translation::getIso6391, MovieTranslation::setLanguage);
        });

        MovieDb movieDb;

        try {
            movieDb = movies.getDetails(movieId, "en", MovieAppendToResponse.TRANSLATIONS
                    , MovieAppendToResponse.IMAGES, MovieAppendToResponse.CREDITS
                    , MovieAppendToResponse.VIDEOS
                    , MovieAppendToResponse.EXTERNAL_IDS
                    , MovieAppendToResponse.KEYWORDS);
        } catch (TmdbException e) {
            throw new APIException("Movie with Id: " + movieId + ", is not found in the TMDB Database");
        }

        Movie movie = modelMapper.map(movieDb, Movie.class);
        movie.setMovieId(movieId);
        movie.setGenres(movieFromDb.getGenres());
        movie.setPosters(movieFromDb.getPosters());
        movie.setBackdrops(movieFromDb.getBackdrops());
        movie.setLogos(movieFromDb.getLogos());
        movie.setKeywords(movieFromDb.getKeywords());
        movie.setTranslations(movieFromDb.getTranslations());
        movie.setPopularity(movieFromDb.getPopularity());
        movie.setWatchCount(movieFromDb.getWatchCount());
        movie.setLikeCount(movieFromDb.getLikeCount());
        movie.setRatingCount(movieFromDb.getRatingCount());
        movie.setRatingAverage(movieFromDb.getRatingAverage());

        ExternalIds externalIds = movieDb.getExternalIds();
        movie.setFacebookId(externalIds.getFacebookId());
        movie.setInstagramId(externalIds.getInstagramId());
        movie.setWikidataId(externalIds.getWikidataId());
        movie.setTwitterId(externalIds.getTwitterId());

        try {
            String trailerKey = movieDb.getVideos().getResults().getFirst().getKey();
            movie.setTrailerKey(trailerKey);
            movie.setTrailerLink("https://www.youtube.com/watch?v=" + trailerKey);
        } catch (Exception e) {
        }

        MovieDTO movieDTO = modelMapper.map(movie, MovieDTO.class);

        movieDTO.setBackdrop(movieImageRepository.findMovieImageByMovieIdAndType(movieId, Limit.of(1), 1));
        movieDTO.setPoster(movieImageRepository.findMovieImageByMovieIdAndType(movieId, Limit.of(1), 2));
        movieDTO.setLogo(movieImageRepository.findMovieImageByMovieIdAndType(movieId, Limit.of(1), 3));
        return movieDTO;
    }

}
