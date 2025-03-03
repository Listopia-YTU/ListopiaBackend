package com.savt.listopia.util;

import com.savt.listopia.model.core.image.ImageType;
import com.savt.listopia.model.core.image.MovieImage;
import com.savt.listopia.model.core.image.PersonImage;
import com.savt.listopia.model.movie.*;
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
import org.springframework.boot.CommandLineRunner;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

public class DatabaseFetcher {
    @Value("${tmdb.apiKey}")
    private String tmdbKey;

    private ModelMapper modelMapper = new ModelMapper();
    private TmdbApi tmdbApi = new TmdbApi(tmdbKey);

    public CommandLineRunner initData(MovieRepository movieRepository, GenreRepository genreRepository, ImageRepository imageRepository, PersonRepository personRepository, MovieCastRepository movieCastRepository, MovieCrewRepository movieCrewRepository) {

        return args -> {
//            URL url = new URL("https://image.tmdb.org/t/p/w500/wigZBAmNrIhxp2FNGOROUAeHvdh.jpg");
////            BufferedImage image = ImageIO.read(url);
////            File outputFile = new File("downloaded_image.jpg");
////            ImageIO.write(image, "jpg", outputFile);
//
//
//            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
//            FileOutputStream fileOutputStream = new FileOutputStream("test.jpg");
//            FileChannel fileChannel = fileOutputStream.getChannel();
//            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

            TmdbMovies movies = tmdbApi.getMovies();
            MovieDb movieDb;
            TmdbPeople people = tmdbApi.getPeople();
            PersonDb personDb;

            modelMapper.typeMap(Translation.class, MovieTranslation.class).addMappings(mapper -> {
                mapper.map(Translation::getIso6391, MovieTranslation::setLanguage);
            });

            modelMapper.typeMap(MovieDb.class, Movie.class).addMappings(mapper -> {
                mapper.skip(Movie::setGenres);
            });

            modelMapper.typeMap(PersonDb.class, Person.class).addMappings(mapper -> {
                mapper.skip(Person::setMovieCrew);
                mapper.skip(Person::setMovieCast);
            });
////
//            modelMapper.typeMap(PersonDb.class,Person.class).addMappings(mapper -> {
//                mapper.skip(Person::setPersonId);
//            });

//            modelMapper.typeMap(info.movito.themoviedbapi.model.movies.Crew.class, MovieCrew.class).addMappings(mapper -> {
//                mapper.skip(MovieCrew::setMovie);
//            });
//
//            modelMapper.typeMap(info.movito.themoviedbapi.model.movies.Cast.class, MovieCast.class).addMappings(mapper -> {
//                mapper.skip(MovieCast::setMovie);
//            });


            // Genres

            TmdbGenre tmdbGenre = tmdbApi.getGenre();
            List<Genre> genresFromTmdb = tmdbGenre.getMovieList("en");

            List<com.savt.listopia.model.core.Genre> genres = genresFromTmdb.stream()
                    .map(g -> modelMapper.map(g, com.savt.listopia.model.core.Genre.class))
                    .toList();

            String[] langs = {"en", "de", "it", "fr", "es", "ru", "cs", "pt", "pl", "hu", "nl", "lt", "tr", "he", "el", "zh", "da", "ro", "ko", "uk", "pt", "fi", "bg", "sk", "sv", "es", "ja", "ka", "lv", "hr", "ca", "th", "et"};

            for (int y = 0; y < 33; y++) {
                genresFromTmdb = tmdbGenre.getMovieList(langs[y]);
                for (int j = 0; j < genres.size(); j++) {
                    com.savt.listopia.model.core.Genre genre = genres.get(j);
                    List<GenreTranslation> genreTranslations = genre.getTranslations();
                    GenreTranslation genreTranslation = new GenreTranslation();
                    genreTranslation.setLanguage(langs[y]);
                    genreTranslation.setName(genresFromTmdb.get(j).getName());
                    genreTranslation.setGenre(genre);
                    genreTranslations.add(genreTranslation);
                    genre.setTranslations(genreTranslations);
                }
            }

            genreRepository.saveAll(genres);

            System.out.println("SAVED GENRES");

            // Movies

            int i;

            for (i = 1; i < 50; i++) {
                try {
                    movieDb = movies.getDetails(i, "en", MovieAppendToResponse.TRANSLATIONS
                            , MovieAppendToResponse.IMAGES, MovieAppendToResponse.CREDITS
                            , MovieAppendToResponse.VIDEOS
                            , MovieAppendToResponse.EXTERNAL_IDS
                            , MovieAppendToResponse.KEYWORDS);
                } catch (TmdbException e) {
                    continue;
                }

                if (movieDb.getPopularity() < 20) {
                    continue;
                }


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

                String trailerKey = movieDb.getVideos().getResults().getFirst().getKey();
                movie.setTrailerKey(trailerKey);
                movie.setTrailerLink("https://www.youtube.com/watch?v=" + trailerKey);
                movieRepository.save(movie);


//                for (MovieCast movieCastForImage : movieCast){
//                    String filePath = movieCastForImage.getProfilePath();
//                    if (filePath == null){
//                        continue;
//                    }
//
//                    URL url = new URL("https://image.tmdb.org/t/p/original" + filePath);
//                    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
//                    FileOutputStream fileOutputStream = new FileOutputStream("images/casts" + filePath);
//                    FileChannel fileChannel = fileOutputStream.getChannel();
//                    fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
//                }


                for (MovieImage movieImage : backdrops) {
                    String filePath = movieImage.getFilePath();
                    URL url = new URL("https://image.tmdb.org/t/p/original" + filePath);
                    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream("images/backdrops" + filePath);
                    FileChannel fileChannel = fileOutputStream.getChannel();
                    fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                    break;
                }

                for (MovieImage movieImage : posters) {
                    String filePath = movieImage.getFilePath();
                    URL url = new URL("https://image.tmdb.org/t/p/original" + filePath);
                    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream("images/posters" + filePath);
                    FileChannel fileChannel = fileOutputStream.getChannel();
                    fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                    break;
                }

                for (MovieImage movieImage : logos) {
                    String filePath = movieImage.getFilePath();
                    URL url = new URL("https://image.tmdb.org/t/p/original" + filePath);
                    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream("images/logos" + filePath);
                    FileChannel fileChannel = fileOutputStream.getChannel();
                    fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                    break;
                }

            }

            System.out.println("SAVED MOVIES");

            // Persons

            for (i = 1; i < 10; i++) {
                try {
                    personDb = people.getDetails(i, "en", PersonAppendToResponse.TRANSLATIONS,
                            PersonAppendToResponse.IMAGES, PersonAppendToResponse.MOVIE_CREDITS);
                } catch (TmdbException e) {
                    continue;
                }

                Person person = modelMapper.map(personDb, Person.class);

                List<info.movito.themoviedbapi.model.people.Translation> translations = personDb.getTranslations().getTranslations();

                List<PersonTranslation> personTranslations = translations.stream()
                        .map(translation -> modelMapper.map(translation, PersonTranslation.class))
                        .toList();


                int translationIndex = 0;

                for (info.movito.themoviedbapi.model.people.Translation translation : translations) {
                    info.movito.themoviedbapi.model.people.Data data = translation.getData();
                    PersonTranslation personTranslation = personTranslations.get(translationIndex);
                    personTranslation.setBiography(translation.getData().getBiography());
                    personTranslation.setLanguage(translation.getIso6391());
                    translationIndex++;
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

                List<MovieCast> movieCasts = person.getMovieCast();

                for (info.movito.themoviedbapi.model.people.credits.MovieCast mc : movieCastsOfPerson) {
                    MovieCast movieCast = movieCastRepository.getMovieCastByCastId(mc.getCreditId());

                    if (movieCast != null) {
                        movieCast.setPerson(person);
                        movieCasts.add(movieCast);
                    }
                }

                List<info.movito.themoviedbapi.model.people.credits.MovieCrew> movieCrewsOfPerson =
                        personDb.getMovieCredits().getCrew();


                List<MovieCrew> movieCrews = person.getMovieCrew();

                for (info.movito.themoviedbapi.model.people.credits.MovieCrew mc : movieCrewsOfPerson) {
                    MovieCrew movieCrew = movieCrewRepository.getMovieCrewByCrewId(mc.getCreditId());

                    if (movieCrew != null) {
                        movieCrew.setPerson(person);
                        movieCrews.add(movieCrew);
                    }
                }

                person.setMovieCast(movieCasts);
                personRepository.save(person);
                movieCastRepository.saveAll(movieCasts);
                movieCrewRepository.saveAll(movieCrews);

                for (PersonImage personImage : profiles) {
                    String filePath = personImage.getFilePath();
                    URL url = new URL("https://image.tmdb.org/t/p/original" + filePath);
                    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream("images/people" + filePath);
                    FileChannel fileChannel = fileOutputStream.getChannel();
                    fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                    break;
                }

            }

            System.out.println("SAVED PERSONS");

            System.out.println("DATA INITIALIZATION FINISHED");
        };
    }

}
