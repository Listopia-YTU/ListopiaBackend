package com.savt.listopia.util;

import com.savt.listopia.exception.APIException;
import com.savt.listopia.exception.ResourceNotFoundException;
import com.savt.listopia.model.core.image.ImageType;
import com.savt.listopia.model.core.image.MovieImage;
import com.savt.listopia.model.core.image.PersonImage;
import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.movie.MovieCast;
import com.savt.listopia.model.movie.MovieCrew;
import com.savt.listopia.model.movie.MovieKeyword;
import com.savt.listopia.model.people.Person;
import com.savt.listopia.model.translation.GenreTranslation;
import com.savt.listopia.model.translation.MovieTranslation;
import com.savt.listopia.model.translation.PersonTranslation;
import com.savt.listopia.repository.*;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbGenre;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbPeople;
import info.movito.themoviedbapi.model.core.Genre;
import info.movito.themoviedbapi.model.core.image.Artwork;
import info.movito.themoviedbapi.model.movies.*;
import info.movito.themoviedbapi.model.people.PersonDb;
import info.movito.themoviedbapi.model.people.PersonImages;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.appendtoresponse.MovieAppendToResponse;
import info.movito.themoviedbapi.tools.appendtoresponse.PersonAppendToResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FetchUtil {
    private final GenreTranslationRepository genreTranslationRepository;
    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final MovieCrewRepository movieCrewRepository;
    private final MovieCastRepository movieCastRepository;
    private final MovieImageRepository movieImageRepository;
    private final ModelMapper modelMapper;
    private final PersonRepository personRepository;

    @Value("${tmdb.apiKey}")
    private String tmdbKey;

    @Value("${backdrops.path}")
    private String backdropsPath;

    @Value("${posters.path}")
    private String postersPath;

    @Value("${logos.path}")
    private String logosPath;

    public FetchUtil(GenreTranslationRepository genreTranslationRepository, GenreRepository genreRepository, MovieRepository movieRepository, MovieCrewRepository movieCrewRepository, MovieCastRepository movieCastRepository, ModelMapper modelMapper, MovieImageRepository movieImageRepository, PersonRepository personRepository) {
        this.genreTranslationRepository = genreTranslationRepository;
        this.genreRepository = genreRepository;
        this.movieRepository = movieRepository;
        this.movieCrewRepository = movieCrewRepository;
        this.movieCastRepository = movieCastRepository;
        this.modelMapper = modelMapper;
        this.movieImageRepository = movieImageRepository;
        this.personRepository = personRepository;
    }

    public void fetchGenres() throws TmdbException {
        TmdbApi tmdbApi = new TmdbApi(tmdbKey);

        TmdbGenre tmdbGenre = tmdbApi.getGenre();
        List<Genre> genresFromTmdb;

        try {
            genresFromTmdb = tmdbGenre.getMovieList("en");
        } catch (TmdbException e) {
            throw new TmdbException(e.getMessage());
        }

        List<com.savt.listopia.model.core.Genre> genres = genresFromTmdb.stream()
                .map(g -> modelMapper.map(g, com.savt.listopia.model.core.Genre.class))
                .toList();

        genreRepository.saveAll(genres);
    }

    public void fetchGenreTranslations(String language) throws TmdbException {
        TmdbApi tmdbApi = new TmdbApi(tmdbKey);

        TmdbGenre tmdbGenre = tmdbApi.getGenre();
        List<Genre> genresFromTmdb;

        try {
            genresFromTmdb = tmdbGenre.getMovieList(language);
        } catch (TmdbException e) {
            throw new TmdbException(e.getMessage());
        }

        List<com.savt.listopia.model.core.Genre> genres = genresFromTmdb.stream()
                .map(g -> modelMapper.map(g, com.savt.listopia.model.core.Genre.class))
                .toList();

        for (int j = 0; j < genres.size(); j++) {
            com.savt.listopia.model.core.Genre genre = genres.get(j);
            GenreTranslation genreTranslation = new GenreTranslation();
            genreTranslation.setLanguage(language);
            genreTranslation.setName(genresFromTmdb.get(j).getName());
            genreTranslation.setGenre(genre);

            try {
                genreTranslationRepository.save(genreTranslation);
            } catch (Exception e) {
                throw new APIException("Fetch genres first!");
            }
        }
    }

    public void fetchMovies(Integer startId, Integer endId, Integer minPopularity, Boolean fetchAllImages, Boolean fetchKeywords) {
        TmdbApi tmdbApi = new TmdbApi(tmdbKey);

        modelMapper.typeMap(MovieDb.class, Movie.class).addMappings(mapper -> {
            mapper.skip(Movie::setGenres);
        });

        modelMapper.typeMap(Translation.class, MovieTranslation.class).addMappings(mapper -> {
            mapper.map(Translation::getIso6391, MovieTranslation::setLanguage);
        });

        TmdbMovies movies = tmdbApi.getMovies();
        MovieDb movieDb;

        for (int i = startId; i <= endId; i++) {
            try {
                movieDb = movies.getDetails(i, "en", MovieAppendToResponse.TRANSLATIONS
                        , MovieAppendToResponse.IMAGES, MovieAppendToResponse.CREDITS
                        , MovieAppendToResponse.VIDEOS
                        , MovieAppendToResponse.EXTERNAL_IDS
                        , MovieAppendToResponse.KEYWORDS);
            } catch (TmdbException e) {
                continue;
            }

            if (movieDb.getPopularity() < minPopularity) {
                continue;
            }

            Movie movie = modelMapper.map(movieDb, Movie.class);
            movie.setMovieId(movieDb.getId());
            movie.setWatchCount(0);
            movie.setLikeCount(0);
            movie.setRatingAverage(0.0);
            movie.setRatingCount(0);

            List<Translation> translations = movieDb.getTranslations().getTranslations()
                    .stream().filter(t -> t.getIso6391().equals("tr"))
                    .toList();

            List<MovieTranslation> movieTranslations = new ArrayList<>();

            for (Translation translation : translations) {
                Data data = translation.getData();
                MovieTranslation movieTranslation = new MovieTranslation();
                movieTranslation.setLanguage(translation.getIso6391());
                movieTranslation.setTitle(data.getTitle());
                movieTranslation.setOverview(data.getOverview());
                movieTranslation.setTagline(data.getTagline());
                movieTranslations.add(movieTranslation);
            }

            movieTranslations.forEach(t -> t.setMovie(movie));
            movie.setTranslations(movieTranslations);

            Images images = movieDb.getImages();

            List<Artwork> backdropsArtwork;
            List<Artwork> logosArtwork;
            List<Artwork> postersArtwork;

            if (fetchAllImages) {
                backdropsArtwork = images.getBackdrops();
                logosArtwork = images.getLogos();
                postersArtwork = images.getPosters();
            } else {
                backdropsArtwork = new ArrayList<>();
                if (!(images.getBackdrops().isEmpty())) {
                    backdropsArtwork.add(images.getBackdrops().getFirst());
                }

                postersArtwork = new ArrayList<>();
                if (!(images.getPosters().isEmpty())) {
                    postersArtwork.add(images.getPosters().getFirst());
                }

                logosArtwork = new ArrayList<>();
                if (!(images.getLogos().isEmpty())) {
                    logosArtwork.add(images.getLogos().getFirst());
                }
            }

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

            ExternalIds externalIds = movieDb.getExternalIds();
            movie.setFacebookId(externalIds.getFacebookId());
            movie.setInstagramId(externalIds.getInstagramId());
            movie.setWikidataId(externalIds.getWikidataId());
            movie.setTwitterId(externalIds.getTwitterId());

            if (fetchKeywords) {
                List<MovieKeyword> keywords = movieDb.getKeywords().getKeywords().stream()
                        .map(k -> modelMapper.map(k, MovieKeyword.class))
                        .toList();

                keywords.forEach(k -> k.setMovie(movie));

                movie.setKeywords(keywords);
            }

            String trailerKey = movieDb.getVideos().getResults().getFirst().getKey();
            if (trailerKey != null){
                movie.setTrailerKey(trailerKey);
                movie.setTrailerLink("https://www.youtube.com/watch?v=" + trailerKey);
            }

            try {
                movieRepository.save(movie);
            } catch (Exception e) {
                throw new APIException("Fetch genres first!");
            }

            movieCrewRepository.saveAll(movieCrew);
            movieCastRepository.saveAll(movieCast);
        }
    }

    public void downloadMovieCastImages(Integer movieId) throws IOException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "movieId", movieId));

        List<MovieCast> movieCast = movieCastRepository.findAllByMovieMovieId(movieId);

        Files.createDirectories(Paths.get("images/casts"));

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

    public void downloadBackdrops(Integer movieId) throws IOException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "movieId", movieId));

        List<MovieImage> backdrops = movieImageRepository.findAllByTypeAndMovieMovieId(ImageType.BACKDROP.getId(), movieId);

        Files.createDirectories(Paths.get(backdropsPath));

        for (MovieImage movieImage : backdrops) {
            String filePath = movieImage.getFilePath();
            URL url = new URL("https://image.tmdb.org/t/p/original" + filePath);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(backdropsPath + filePath);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    public void downloadPosters(Integer movieId) throws IOException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "movieId", movieId));

        List<MovieImage> posters = movieImageRepository.findAllByTypeAndMovieMovieId(ImageType.POSTER.getId(), movieId);

        Files.createDirectories(Paths.get(postersPath));

        for (MovieImage movieImage : posters) {
            String filePath = movieImage.getFilePath();
            URL url = new URL("https://image.tmdb.org/t/p/original" + filePath);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(postersPath + filePath);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    public void downloadLogos(Integer movieId) throws IOException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "movieId", movieId));

        List<MovieImage> logos = movieImageRepository.findAllByTypeAndMovieMovieId(ImageType.LOGO.getId(), movieId);

        Files.createDirectories(Paths.get(logosPath));

        for (MovieImage movieImage : logos) {
            String filePath = movieImage.getFilePath();
            URL url = new URL("https://image.tmdb.org/t/p/original" + filePath);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(logosPath + filePath);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    public void fetchPersons(Integer startId, Integer endId, Boolean downloadImages) throws IOException {
        TmdbApi tmdbApi = new TmdbApi(tmdbKey);
        TmdbPeople people = tmdbApi.getPeople();
        PersonDb personDb;

        for (int i = startId; i <= endId; i++) {
            try {
                personDb = people.getDetails(i, "en", PersonAppendToResponse.TRANSLATIONS,
                        PersonAppendToResponse.IMAGES, PersonAppendToResponse.MOVIE_CREDITS);
            } catch (TmdbException e) {
                continue;
            }

            Person person = modelMapper.map(personDb, Person.class);

            List<info.movito.themoviedbapi.model.people.Translation> translations = personDb.getTranslations().getTranslations()
                    .stream().filter(t -> t.getIso6391().equals("tr"))
                    .toList();

            List<PersonTranslation> personTranslations = new ArrayList<>();

            for (info.movito.themoviedbapi.model.people.Translation translation : translations) {
                PersonTranslation personTranslation = new PersonTranslation();
                personTranslation.setBiography(translation.getData().getBiography());
                personTranslation.setLanguage(translation.getIso6391());
                personTranslations.add(personTranslation);
            }

            personTranslations.forEach(t -> t.setPerson(person));

            person.setTranslations(personTranslations);

            PersonImages images = personDb.getImages();
            List<Artwork> profilesArtwork = images.getProfiles();

            List<PersonImage> profiles = profilesArtwork.stream()
                    .map(a -> modelMapper.map(a, PersonImage.class))
                    .toList();

            profiles.forEach(b -> b.setPerson(person));
            profiles.forEach(b -> b.setType(ImageType.PROFILES.getId()));

            person.setProfiles(profiles);

            List<info.movito.themoviedbapi.model.people.credits.MovieCast> movieCastsOfPerson =
                    personDb.getMovieCredits().getCast();

            List<MovieCast> movieCasts = new ArrayList<>();

            for (info.movito.themoviedbapi.model.people.credits.MovieCast mc : movieCastsOfPerson) {
                MovieCast movieCast = movieCastRepository.getMovieCastById(mc.getId());

                if (movieCast != null) {
                    movieCast.setPerson(person);
                    movieCasts.add(movieCast);
                    person.getMovieCasts().add(movieCast);
                }
            }

            List<info.movito.themoviedbapi.model.people.credits.MovieCrew> movieCrewsOfPerson =
                    personDb.getMovieCredits().getCrew();

            List<MovieCrew> movieCrews = new ArrayList<>();

            for (info.movito.themoviedbapi.model.people.credits.MovieCrew mc : movieCrewsOfPerson) {
                MovieCrew movieCrew = movieCrewRepository.getMovieCrewByCrewId(mc.getId());

                if (movieCrew != null) {
                    movieCrew.setPerson(person);
                    movieCrews.add(movieCrew);
                    person.getMovieCrews().add(movieCrew);
                }
            }

            try {
                personRepository.save(person);
                movieCastRepository.saveAll(movieCasts);
                movieCrewRepository.saveAll(movieCrews);
            } catch (Exception e){
            }

            if (downloadImages) {
                downloadPersonProfiles(profiles);
            }
        }
    }

    private void downloadPersonProfiles(List<PersonImage> profiles) throws IOException {
        Files.createDirectories(Paths.get("images/people"));

        for (PersonImage personImage : profiles) {
            String filePath = personImage.getFilePath();
            URL url = new URL("https://image.tmdb.org/t/p/original" + filePath);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream("images/people" + filePath);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

}
