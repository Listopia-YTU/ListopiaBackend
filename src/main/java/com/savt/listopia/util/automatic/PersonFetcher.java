package com.savt.listopia.util.automatic;

import com.savt.listopia.model.core.image.ImageType;
import com.savt.listopia.model.core.image.PersonImage;
import com.savt.listopia.model.movie.MovieCast;
import com.savt.listopia.model.movie.MovieCrew;
import com.savt.listopia.model.people.Person;
import com.savt.listopia.model.translation.PersonTranslation;
import com.savt.listopia.repository.MovieCastRepository;
import com.savt.listopia.repository.MovieCrewRepository;
import com.savt.listopia.repository.PersonRepository;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbPeople;
import info.movito.themoviedbapi.model.core.image.Artwork;
import info.movito.themoviedbapi.model.people.PersonDb;
import info.movito.themoviedbapi.model.people.PersonImages;
import info.movito.themoviedbapi.model.people.Translation;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.appendtoresponse.PersonAppendToResponse;
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
import java.util.ArrayList;
import java.util.List;

@Configuration
public class PersonFetcher {
    @Value("${tmdb.apiKey}")
    private String tmdbKey;

    //    @Bean
    public CommandLineRunner initPersons(PersonRepository personRepository, MovieCastRepository movieCastRepository, MovieCrewRepository movieCrewRepository) {
        return args -> {
            ModelMapper modelMapper = new ModelMapper();
            TmdbApi tmdbApi = new TmdbApi(tmdbKey);
            TmdbPeople people = tmdbApi.getPeople();
            PersonDb personDb;

            int i;

            for (i = 1; i < 100; i++) {
                try {
                    personDb = people.getDetails(i, "en", PersonAppendToResponse.TRANSLATIONS,
                            PersonAppendToResponse.IMAGES, PersonAppendToResponse.MOVIE_CREDITS);
                } catch (TmdbException e) {
                    continue;
                }

                Person person = modelMapper.map(personDb, Person.class);

                List<Translation> translations = personDb.getTranslations().getTranslations()
                        .stream().filter(t -> t.getIso6391().equals("tr"))
                        .toList();

                List<PersonTranslation> personTranslations = new ArrayList<>();


                for (info.movito.themoviedbapi.model.people.Translation translation : translations) {
                    info.movito.themoviedbapi.model.people.Data data = translation.getData();
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
                }catch (Exception e){

                }

//                downloadImage(profiles);

            }

            System.out.println("SAVED PERSONS");
        };

    }

    private void downloadImage(List<PersonImage> profiles) throws IOException {
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
}
