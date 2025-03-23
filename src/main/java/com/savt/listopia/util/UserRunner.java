package com.savt.listopia.util;

import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.user.User;
import com.savt.listopia.payload.dto.MovieFrontDTO;
import com.savt.listopia.payload.dto.UserDTO;
import com.savt.listopia.repository.MovieRepository;
import com.savt.listopia.repository.UserRepository;
import com.savt.listopia.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class UserRunner implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRunner.class);
    @Autowired
    UserRepository userRepository;
    @Autowired
    MovieRepository movieRepository;
    @Autowired
    UserService userService;

    @Override
    public void run(String... args) {
        User user1 = new User();
        user1.setEmail("test@mail.com");
        user1.setUsername("user1");

        User user2 = new User();
        user2.setEmail("test2@gmail.com");
        user2.setUsername("user2");

        Movie movie1 = new Movie();
        movie1.setMovieId(1);

        Movie movie2 = new Movie();
        movie2.setMovieId(2);

        userRepository.save(user1);
        userRepository.save(user2);
        movieRepository.save(movie1);
        movieRepository.save(movie2);

        UserDTO userDTO = userService.getUserById(1L);
        LOGGER.info("userDTO: {}", userDTO);
        userService.likeMovie(user1.getId(), movie2, true);
        List<MovieFrontDTO> liked = userService.getUserLikedMovies(user1.getId());
        LOGGER.info("liked: {}", liked);

        userService.MakeFriends(user1.getId(), user2.getId());

        // Message examples.

        userService.sendMessage(user1.getId(), user2.getId(), "naberm amkasdvmksaas <script>alert(1);</script>");
        userService.sendMessage(user2.getId(), user1.getId(), "REPORT ETT!!");
        userService.markPrivateMessageReported(2L);

        LOGGER.info("user1 received: {}", userService.getAllMessagesOfUserReceived(user1.getId()));
        LOGGER.info("user1 sent: {}", userService.getAllMessagesUserSent(user1.getId()));

        LOGGER.info("user2 received: {}", userService.getAllMessagesOfUserReceived(user2.getId()));
        LOGGER.info("user2 sent: {}", userService.getAllMessagesUserSent(user2.getId()));

        LOGGER.info("user1 received from user2: {}", userService.getAllMessagesReceivedFrom(user1.getId(), user2.getId()));
        LOGGER.info("user2 received from user1: {}", userService.getAllMessagesReceivedFrom(user2.getId(), user1.getId()));

        LOGGER.info("user1 sent to user2: {}", userService.getAllMessagesSentTo(user1.getId(), user2.getId()));
        LOGGER.info("user2 sent to user1: {}", userService.getAllMessagesSentTo(user2.getId(), user1.getId()));

        LOGGER.info("reported messages: {}", userService.getAllReportedMessages());

        LOGGER.info("is reported 1: {}", userService.isPrivateMessageReported(1L));
        LOGGER.info("is reported 2: {}", userService.isPrivateMessageReported(2L));

    }
}