package com.savt.listopia.util;

import com.savt.listopia.model.core.image.ImageType;
import com.savt.listopia.model.core.image.MovieImage;
import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.movie.MovieCast;
import com.savt.listopia.model.movie.MovieCrew;
import com.savt.listopia.model.movie.MovieKeyword;
import com.savt.listopia.model.translation.MovieTranslation;
import com.savt.listopia.repository.MovieRepository;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.core.Genre;
import info.movito.themoviedbapi.model.core.image.Artwork;
import info.movito.themoviedbapi.model.movies.*;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.appendtoresponse.MovieAppendToResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

@Configuration
public class MovieFetcher {
    @Value("${tmdb.apiKey}")
    private String tmdbKey;

    @Value("${backdrops.path}")
    private String backdropsPath;

    @Value("${posters.path}")
    private String postersPath;

    @Value("${logos.path}")
    private String logosPath;

    public CommandLineRunner initMovies(MovieRepository movieRepository) {

        return args -> {
            ModelMapper modelMapper = new ModelMapper();
            TmdbApi tmdbApi = new TmdbApi(tmdbKey);

            modelMapper.typeMap(MovieDb.class, Movie.class).addMappings(mapper -> {
                mapper.skip(Movie::setGenres);
            });

            modelMapper.typeMap(Translation.class, MovieTranslation.class).addMappings(mapper -> {
                mapper.map(Translation::getIso6391, MovieTranslation::setLanguage);
            });

            TmdbMovies movies = tmdbApi.getMovies();
            MovieDb movieDb;
            int i;

            for (i = 1; i < 10000; i++) {
                try {
                    movieDb = movies.getDetails(i, "en", MovieAppendToResponse.TRANSLATIONS
                            , MovieAppendToResponse.IMAGES, MovieAppendToResponse.CREDITS
                            , MovieAppendToResponse.VIDEOS
                            , MovieAppendToResponse.EXTERNAL_IDS
                            , MovieAppendToResponse.KEYWORDS);
                } catch (TmdbException e) {
                    continue;
                }

//                if (movieDb.getPopularity() < 20) {
//                    continue;
//                }

                Movie movie = modelMapper.map(movieDb, Movie.class);
                movie.setMovieId(movieDb.getId());
                movie.setWatchCount(0);
                movie.setLikeCount(0);
                movie.setRatingAverage(0.0);
                movie.setRatingCount(0);

                List<Translation> translations = movieDb.getTranslations().getTranslations();

                List<MovieTranslation> movieTranslations = translations.stream()
                        .map(translation -> modelMapper.map(translation, MovieTranslation.class))
                        .toList();


                int translationIndex = 0;

                for (Translation translation : translations) {
                    Data data = translation.getData();
                    MovieTranslation movieTranslation = movieTranslations.get(translationIndex);
                    movieTranslation.setTitle(data.getTitle());
                    movieTranslation.setOverview(data.getOverview());
                    movieTranslation.setTagline(data.getTagline());
                    translationIndex++;
                }

                movieTranslations.forEach(t -> t.setMovie(movie));
                movie.setTranslations(movieTranslations);

                Images images = movieDb.getImages();
                List<Artwork> backdropsArtwork = images.getBackdrops();
                List<Artwork> logosArtwork = images.getLogos();
                List<Artwork> postersArtwork = images.getPosters();

                List<MovieImage> backdrops = backdropsArtwork.stream()
                        .map(a -> modelMapper.map(a, MovieImage.class))
                        .toList();

                backdrops.forEach(b -> b.setMovie(movie));
                backdrops.forEach(b -> b.setType(ImageType.BACKDROP.getId()));

                List<MovieImage> posters = postersArtwork.stream()
                        .map(a -> modelMapper.map(a, MovieImage.class))
                        .toList();

                posters.forEach(b -> b.setMovie(movie));
                posters.forEach(b -> b.setType(ImageType.POSTER.getId()));

                List<MovieImage> logos = logosArtwork.stream()
                        .map(a -> modelMapper.map(a, MovieImage.class))
                        .toList();

                logos.forEach(b -> b.setMovie(movie));
                logos.forEach(b -> b.setType(ImageType.LOGO.getId()));

                movie.setBackdrops(backdrops);
                movie.setPosters(posters);
                movie.setLogos(logos);
                List<Genre> genresFromDb = movieDb.getGenres();
                List<com.savt.listopia.model.core.Genre> genresOfMovie = genresFromDb.stream()
                        .map(g -> modelMapper.map(g, com.savt.listopia.model.core.Genre.class))
                        .toList();

                movie.setGenres(genresOfMovie);

                List<MovieCast> movieCast = movieDb.getCredits().getCast().stream()
                        .map(c -> modelMapper.map(c, MovieCast.class))
                        .toList();

                movieCast.forEach(c -> c.setMovie(movie));

                List<MovieCrew> movieCrew = movieDb.getCredits().getCrew().stream()
                        .map(c -> modelMapper.map(c, MovieCrew.class))
                        .toList();

                movieCrew.forEach(c -> c.setMovie(movie));


                movie.setMovieCast(movieCast);
                movie.setMovieCrew(movieCrew);

                ExternalIds externalIds = movieDb.getExternalIds();
                movie.setFacebookId(externalIds.getFacebookId());
                movie.setInstagramId(externalIds.getInstagramId());
                movie.setWikidataId(externalIds.getWikidataId());
                movie.setTwitterId(externalIds.getTwitterId());

                List<MovieKeyword> keywords = movieDb.getKeywords().getKeywords().stream()
                        .map(k -> modelMapper.map(k, MovieKeyword.class))
                        .toList();

                keywords.forEach(k -> k.setMovie(movie));
                movie.setKeywords(keywords);

                try {
                    String trailerKey = movieDb.getVideos().getResults().getFirst().getKey();
                    movie.setTrailerKey(trailerKey);
                    movie.setTrailerLink("https://www.youtube.com/watch?v=" + trailerKey);
                } catch (Exception e) {
                }


                movieRepository.save(movie);

//                downloadMovieCastImages(movieCast);
//                downloadBackdrops(backdrops);
//                downloadPosters(posters);
//                downloadLogos(logos);
            }
        };
    }

    public void downloadMovieCastImages(List<MovieCast> movieCast) throws IOException {
        for (MovieCast movieCastForImage : movieCast) {
            String filePath = movieCastForImage.getProfilePath();

            if (filePath == null) {
                continue;
            }

            URL url = new URL("https://image.tmdb.org/t/p/original" + filePath);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream("images/casts" + filePath);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    public void downloadBackdrops(List<MovieImage> backdrops) throws IOException {
        for (MovieImage movieImage : backdrops) {
            String filePath = movieImage.getFilePath();
            URL url = new URL("https://image.tmdb.org/t/p/original" + filePath);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(backdropsPath + filePath);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            break;
        }
    }

    public void downloadPosters(List<MovieImage> posters) throws IOException {
        for (MovieImage movieImage : posters) {
            String filePath = movieImage.getFilePath();
            URL url = new URL("https://image.tmdb.org/t/p/original" + filePath);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(postersPath + filePath);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            break;
        }
    }

    public void downloadLogos(List<MovieImage> logos) throws IOException {
        for (MovieImage movieImage : logos) {
            String filePath = movieImage.getFilePath();
            URL url = new URL("https://image.tmdb.org/t/p/original" + filePath);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(logosPath + filePath);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            break;
        }
    }


}
