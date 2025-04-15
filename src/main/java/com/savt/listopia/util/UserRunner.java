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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class UserRunner implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRunner.class);

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final UserService userService;

    public UserRunner(UserRepository userRepository, MovieRepository movieRepository, UserService userService) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.userService = userService;
    }

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

        int page = 0;
        int size = 10;

        // Message examples.
        userService.sendMessage(user1.getId(), user2.getId(), "naberm amkasdvmksaas <script>alert(1);</script>");
        userService.sendMessage(user2.getId(), user1.getId(), "REPORT ETT!!");
        userService.markPrivateMessageReported(2L);

        LOGGER.info("user1 received: {}", userService.getAllMessagesOfUserReceived(user1.getId(), page, size).getContent());
        LOGGER.info("user1 sent: {}", userService.getAllMessagesUserSent(user1.getId(), page, size).getContent());

        LOGGER.info("user2 received: {}", userService.getAllMessagesOfUserReceived(user2.getId(), page, size).getContent());
        LOGGER.info("user2 sent: {}", userService.getAllMessagesUserSent(user2.getId(), page, size).getContent());

        LOGGER.info("user1 received from user2: {}", userService.getAllMessagesReceivedFrom(user1.getId(), user2.getId(), page, size).getContent());
        LOGGER.info("user2 received from user1: {}", userService.getAllMessagesReceivedFrom(user2.getId(), user1.getId(), page, size).getContent());

        LOGGER.info("user1 sent to user2: {}", userService.getAllMessagesSentTo(user1.getId(), user2.getId(), page, size).getContent());
        LOGGER.info("user2 sent to user1: {}", userService.getAllMessagesSentTo(user2.getId(), user1.getId(), page, size).getContent());

        LOGGER.info("reported messages: {}", userService.getAllReportedMessages(page, size).getContent());

        LOGGER.info("is reported 1: {}", userService.isPrivateMessageReported(1L));
        LOGGER.info("is reported 2: {}", userService.isPrivateMessageReported(2L));


        // MovieComment

        userService.createMovieComment(user1.getId(), movie1.getMovieId(), false, "selam bebek <script />");

        LOGGER.info("MovieComment - getMovieCommentById: {}", userService.getMovieCommentById(1L));
        LOGGER.info("MovieComment - getMovieCommentForMovie: {}", userService.getMovieCommentForMovie(movie1.getMovieId(), page, size).getContent());
        LOGGER.info("MovieComment - getMovieCommentFromUser: {}", userService.getMovieCommentFromUser(user1.getId(), page, size).getContent());
        LOGGER.info("MovieComment - getMovieCommentForMovieFromUser: {}", userService.getMovieCommentForMovieFromUser(movie1.getMovieId(), user1.getId(), page, size).getContent());
        LOGGER.info("MovieComment - getMovieCommentIsReported: {}", userService.getMovieCommentReported(false, page, size).getContent());

        userService.deleteMovieComment(1L);
        // crash :( çünkü yok hahaha :)
        // LOGGER.info("MovieComment - getMovieCommentById (after deleted): {}", userService.getMovieCommentById(1L));
    }
}