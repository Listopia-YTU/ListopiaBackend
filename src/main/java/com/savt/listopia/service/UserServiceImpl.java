package com.savt.listopia.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.savt.listopia.exception.APIException;
import com.savt.listopia.exception.ResourceNotFoundException;
import com.savt.listopia.exception.userException.UserException;
import com.savt.listopia.exception.userException.UserNotAuthorizedException;
import com.savt.listopia.exception.userException.UserNotFoundException;
import com.savt.listopia.mapper.*;
import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.user.*;
import com.savt.listopia.payload.dto.*;
import com.savt.listopia.repository.*;
import com.savt.listopia.security.auth.AuthenticationToken;
import com.savt.listopia.util.PasswordUtil;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PrivateMessageRepository privateMessageRepository;
    private final MovieRepository movieRepository;
    private final MovieCommentRepository movieCommentRepository;
    private final MovieCommentMapper movieCommentMapper;
    private final UserMapper userMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final MovieFrontMapper movieFrontMapper;
    private final MovieImageRepository movieImageRepository;
    private final UserActivityRepository userActivityRepository;
    private final UserActivityMapperImpl userActivityMapperImpl;
    private final ObjectMapper objectMapper;
    private final MovieServiceImpl movieServiceImpl;

    public static <D, T> Page<D> mapEntityPageToDtoPage(Page<T> entities, Class<D> dtoClass, ModelMapper mapper) {
        List<D> dtoList = entities.getContent().stream()
                .map(entity -> mapper.map(entity, dtoClass))
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, entities.getPageable(), entities.getTotalElements());
    }

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PrivateMessageRepository privateMessageRepository, MovieRepository movieRepository, MovieCommentRepository movieCommentRepository, MovieCommentMapper movieCommentMapper, UserMapper userMapper, NotificationRepository notificationRepository, NotificationMapper notificationMapper, MovieFrontMapper movieFrontMapper, MovieImageRepository movieImageRepository, UserActivityRepository userActivityRepository, UserActivityMapperImpl userActivityMapperImpl, ObjectMapper objectMapper, MovieServiceImpl movieServiceImpl) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.privateMessageRepository = privateMessageRepository;
        this.movieRepository = movieRepository;
        this.movieCommentRepository = movieCommentRepository;
        this.movieCommentMapper = movieCommentMapper;
        this.userMapper = userMapper;
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.movieFrontMapper = movieFrontMapper;
        this.movieImageRepository = movieImageRepository;
        this.userActivityRepository = userActivityRepository;
        this.userActivityMapperImpl = userActivityMapperImpl;
        this.objectMapper = objectMapper;
        this.movieServiceImpl = movieServiceImpl;
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        return userMapper.toDTO(user);
        // return modelMapper.map(user, UserDTO.class);
    }

    public User registerUser(String firstname, String lastName, String email, String username, String plainPassword) {
        if (userRepository.existsByUsername(username))
            throw new APIException("username_already_exists");

        if (userRepository.existsByEmail(email))
            throw new APIException("email_already_exists");

        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstname);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setHashedPassword(hashedPassword);
        user.setCreatedAt( System.currentTimeMillis() );
        user.setLastOnline( System.currentTimeMillis() );

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new APIException("username_or_email_already_exists");
        }

        return user;
    }

    public User getUserByEmailPassword(String email, String plainPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("mail_not_found");
        }

        User user = userOpt.get();

        if (verifyUserPassword(user, plainPassword))
            return user;

        throw new UserNotAuthorizedException("password_not_correct");
    }

    public boolean verifyUserPassword(User user, String enteredPassword) {
        if (user == null)
            return false;

        return PasswordUtil.verifyPassword(enteredPassword, user.getHashedPassword());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("user_does_not_exists"));
        return modelMapper.map(user, UserDTO.class);
    }

    public Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return Optional.empty();

        if (authentication instanceof AuthenticationToken authenticationToken) {
            return Optional.of(authenticationToken.getPrincipal().getUserId());
        }

        return Optional.empty();
    }

    @Override
    public Long getCurrentUserIdOrThrow() {
        return getCurrentUserId().orElseThrow(UserNotAuthorizedException::new);
    }

    public UUID getUUIDFromUserId(Long userId) {
        return userRepository.findById(userId).orElseThrow().getUuid();
    }

    public void ChangeUsername(Long userId, String username) {
        if ( userRepository.existsByUsername(username) )
            throw new UserException("username_exists");

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.setUsername(username);
        userRepository.save(user);
    }

    @Override
    public Long getUserIdFromUUID(UUID uuid) {
        return userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("user_does_not_exists"))
                .getId();
    }

    @Override
    public void changePassword(Long userId, String password) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.setHashedPassword( PasswordUtil.hashPassword(password) );
        userRepository.save(user);
    }

    @Override
    public void changeBiography(Long userId, String biography) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.setBiography(biography);
        userRepository.save(user);
    }

    @Transactional
    public Page<MovieFrontDTO> getUserLikedMovies(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> movies = userRepository.findLikedMoviesByUserId(userId, pageable);
        return movieFrontMapper.toDTOPage(movies, movieImageRepository);
        // return mapEntityPageToDtoPage(movies, MovieFrontDTO.class, modelMapper);
    }

    @Transactional
    public void likeMovie(Long userId, Movie movie, Boolean liked) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (movie == null || !movieRepository.existsById(movie.getMovieId())) {
            throw new APIException("movie_not_found");
        }

        if (liked) {
            if (!user.getLikedMovies().contains(movie)) {
                user.getLikedMovies().add(movie);
                userRepository.save(user);
                movie.setLikeCount(movie.getLikeCount() + 1);
                movieRepository.save(movie);
                try {
                    MovieFrontDTO dto = movieFrontMapper.toDTO(movie, movieImageRepository);
                    String json = objectMapper.writeValueAsString(dto);
                    createUserActivity(user.getId(), UserActivityType.MOVIE_LIKED, json);
                } catch (JsonProcessingException e) {
                    LOGGER.error("error creating json object for MovieFrontDTO movieId: {}", movie.getMovieId());
                }

            }
        } else {
            user.getLikedMovies().remove(movie);
            userRepository.save(user);
            movie.setLikeCount(movie.getLikeCount() - 1);
            movieRepository.save(movie);
        }
    }

    @Override
    public Page<MovieFrontDTO> getUserWatchlist(Long userId, int pageNumber, int pageSize) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> movies = userRepository.findWatchlistByUserId(user.getId(), pageable);
        return movieFrontMapper.toDTOPage(movies, movieImageRepository);
    }

    @Override
    public void userAddToWatchlist(Long userId, Integer movieId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Movie movie = movieRepository.findById(movieId).orElseThrow(ResourceNotFoundException::new);

        if (user.getWatchlist().contains(movie))
            return;

        user.getWatchlist().add(movie);
        userRepository.save(user);

        try {
            MovieFrontDTO movieFrontDTO = movieFrontMapper.toDTO(movie, movieImageRepository);
            String json = objectMapper.writeValueAsString(movieFrontDTO);
            createUserActivity(user.getId(), UserActivityType.MOVIE_ADD_WATCHLIST, json);
        } catch (JsonProcessingException e) {
            LOGGER.error("failed to convert Movie to movieFrontDTO, movieId: {}", movie.getMovieId());
        }
    }

    @Override
    public void userDeleteFromWatchlist(Long userId, Integer movieId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Movie movie = movieRepository.findById(movieId).orElseThrow(ResourceNotFoundException::new);

        if (!user.getWatchlist().contains(movie))
            return;

        user.getWatchlist().remove(movie);
        userRepository.save(user);
    }

    @Override
    public Page<MovieFrontDTO> getUserWatched(Long userId, int pageNumber, int pageSize) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> movies = userRepository.findWatchedListByUserId(user.getId(), pageable);
        return movieFrontMapper.toDTOPage(movies, movieImageRepository);
    }

    @Override
    public void userAddToWatched(Long userId, Integer movieId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Movie movie = movieRepository.findById(movieId).orElseThrow(ResourceNotFoundException::new);

        if (user.getWatchedList().contains(movie))
            return;

        user.getWatchedList().add(movie);
        userRepository.save(user);

        try {
            MovieFrontDTO movieFrontDTO = movieFrontMapper.toDTO(movie, movieImageRepository);
            String json = objectMapper.writeValueAsString(movieFrontDTO);
            createUserActivity(user.getId(), UserActivityType.MOVIE_ADD_WATCHED, json);
        } catch (JsonProcessingException e) {
            LOGGER.error("failed to convert Movie to movieFrontDTO, movieId: {}", movie.getMovieId());
        }
    }

    @Override
    public void userDeleteFromWatched(Long userId, Integer movieId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Movie movie = movieRepository.findById(movieId).orElseThrow(ResourceNotFoundException::new);

        if (!user.getWatchedList().contains(movie))
            return;

        user.getWatchedList().remove(movie);
        userRepository.save(user);
    }

    @Transactional
    public void MakeFriends(Long userId, Long friendId) {
        User user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);
        User friend = userRepository.findById(friendId).orElseThrow(ResourceNotFoundException::new);

        user.getFriends().add(friend);
        friend.getFriends().add(user);

        userRepository.save(user);
        userRepository.save(friend);

        try {
            String json = objectMapper.writeValueAsString( userMapper.toDTO(user) );
            createNotification(friend.getId(), NotificationType.BECOME_FRIENDS, json);
        } catch (JsonProcessingException e) {
            LOGGER.error("error creating json object for UserDTO commentId: {}", user.getUuid());
        }

        try {
            String json = objectMapper.writeValueAsString( userMapper.toDTO(friend) );
            createUserActivity(user.getId(), UserActivityType.BECOME_FRIENDS, json);
        } catch (JsonProcessingException e) {
            LOGGER.error("error creating json object for UserDTO commentId: {}", friend.getUuid());
        }

        try {
            String json = objectMapper.writeValueAsString( userMapper.toDTO(user) );
            createUserActivity(friend.getId(), UserActivityType.BECOME_FRIENDS, json);
        } catch (JsonProcessingException e) {
            LOGGER.error("error creating json object for UserDTO commentId: {}", user.getUuid());
        }

        LOGGER.info("users become friends: {} - {}", user.getUuid(), friend.getUuid());
    }

    @Transactional
    public void UserFriendRequest(Long requestOwnerUserId, UUID requestedUserUuid) {
        User requester = userRepository.findById(requestOwnerUserId).orElseThrow(UserNotFoundException::new);
        User requested = userRepository.findByUuid(requestedUserUuid).orElseThrow(UserNotFoundException::new);
        if (Objects.equals(requester.getId(), requested.getId()))
            return;
        requested.getFriendRequests().add(requester);
        createNotification(requested.getId(), NotificationType.FRIEND_REQUEST, requester.getUuid().toString());
        userRepository.save(requested);
    }

    @Transactional
    public void AcceptFriend(Long accepterId, UUID acceptedUUID) {
        User accepter = userRepository.findById(accepterId).orElseThrow(UserNotFoundException::new);
        User accepted = userRepository.findByUuid(acceptedUUID).orElseThrow(UserNotFoundException::new);
        if ( accepter.getFriendRequests().contains(accepted) ) {
            MakeFriends( accepter.getId(), accepted.getId() );
            accepter.getFriendRequests().remove( accepted );
            userRepository.save(accepter);
        }
    }

    @Override
    @Transactional
    public void rejectFriend(Long userId, UUID friendUUID) {
        User accepter = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        User accepted = userRepository.findByUuid(friendUUID).orElseThrow(UserNotFoundException::new);
        if ( accepter.getFriendRequests().contains(accepted) ) {
            accepter.getFriendRequests().remove(accepted);
            userRepository.save(accepter);
        }
    }

    @Override
    @Transactional
    public void removeFriend(Long userId, UUID friendUUID) {
        User remover = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        User lonely = userRepository.findByUuid(friendUUID).orElseThrow(UserNotFoundException::new);
        if ( remover.getFriends().contains(lonely) ) {
            remover.getFriends().remove(lonely);
            userRepository.save(remover);
        }
        if ( lonely.getFriends().contains(remover) ) {
            lonely.getFriends().remove(remover);
            userRepository.save(lonely);
        }
    }

    @Transactional
    public Page<UserDTO> UserFriendRequests(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findFriendRequestsByUserId(userId, pageable);
        return mapEntityPageToDtoPage(users, UserDTO.class, modelMapper);
    }

    @Transactional
    public Page<UserDTO> UserFriends(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findFriendsByUserId(userId, pageable);
        return mapEntityPageToDtoPage(userPage, UserDTO.class, modelMapper);
    }

    @Override
    @Transactional
    public void userReportMessage(Long userId, Long messageId) {
         PrivateMessage msg = privateMessageRepository.findById(messageId).orElseThrow(() -> new UserNotFoundException("message_does_not_exists"));
         if (msg.getToUser().getId().equals(userId)) {
             markPrivateMessageReported(messageId);
         }
    }

    @Transactional
    public void sendMessage(Long fromId, Long toId, String messageStr) {
        User sender = userRepository.findById(fromId).orElseThrow(UserNotFoundException::new);
        User receiver = userRepository.findById(toId).orElseThrow(UserNotFoundException::new);
        PrivateMessage message = new PrivateMessage();
        message.setFromUser( sender );
        message.setToUser( receiver );
        message.setSentAtTimestampSeconds(Instant.now().getEpochSecond());
        message.setMessage(messageStr);
        createNotification(receiver.getId(), NotificationType.NEW_MESSAGE, sender.getUuid().toString());
        privateMessageRepository.save(message);
    }

    @Transactional
    public void markPrivateMessageReported(Long messageId) {
        PrivateMessage msg = privateMessageRepository.findById(messageId).orElseThrow(ResourceNotFoundException::new);
        msg.setIsReported(true);
        privateMessageRepository.save(msg);
    }

    public Boolean isPrivateMessageReported(Long messageId) {
        return privateMessageRepository.findPrivateMessageById(messageId).orElseThrow(ResourceNotFoundException::new).getIsReported();
    }

    public Page<PrivateMessageDTO> getAllReportedMessages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return privateMessageRepository.findAllByIsReportedTrue(pageable)
                .map(this::privateMessageToDTO);
    }

    private PrivateMessageDTO privateMessageToDTO(PrivateMessage message) {
        PrivateMessageDTO dto = new PrivateMessageDTO();
        dto.setId(message.getId());
        // dto.setFromUserUUID(getUUIDFromUserId(message.getFromUserId()).toString());
        dto.setFromUserUUID( message.getFromUser().getUuid().toString() );
        // dto.setToUserUUID(getUUIDFromUserId(message.getToUserId()).toString());
        dto.setToUserUUID( message.getToUser().getUuid().toString() );
        dto.setSentAtTimestampSeconds(message.getSentAtTimestampSeconds());
        dto.setMessage(message.getMessage());
        return dto;
    }

    public Page<PrivateMessageDTO> getAllMessagesOfUserReceived(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return privateMessageRepository.findAllByToUserId(userId, pageable)
                .map(this::privateMessageToDTO);
    }

    public Page<PrivateMessageDTO> getAllMessagesUserSent(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return privateMessageRepository.findAllByFromUserId(userId, pageable)
                .map(this::privateMessageToDTO);
    }

    public Page<PrivateMessageDTO> getAllMessagesSentTo(Long userId, Long toId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return privateMessageRepository.findAllByFromUserIdAndToUserId(userId, toId, pageable)
                .map(this::privateMessageToDTO);
    }

    public Page<PrivateMessageDTO> getAllMessagesReceivedFrom(Long userId, Long fromId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return privateMessageRepository.findAllByFromUserIdAndToUserId(fromId, userId, pageable)
                .map(this::privateMessageToDTO);
    }

    public MovieCommentDTO movieCommentToDTO(MovieComment comment) {
        return movieCommentMapper.toDTO(comment);
    }

    @Transactional
    public MovieCommentDTO createMovieComment(Long userId, Integer movieId, Boolean isSpoiler, String messageU) {
        User commented = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Movie movie = movieRepository.findById(movieId).orElseThrow(ResourceNotFoundException::new);

        String message = messageU.trim();

        if (message.isEmpty())
            throw new APIException("message_cannot_be_null");

        MovieComment comment = new MovieComment();
        comment.setMovie(movie);
        comment.setFromUser(commented);
        comment.setIsSpoiler(isSpoiler);
        comment.setSentAtTimestampSeconds(Instant.now().getEpochSecond());
        comment.setMessage(message);

        movieCommentRepository.save(comment);

        MovieCommentDTO dto = movieCommentToDTO(comment);

        try {
            String json = objectMapper.writeValueAsString(dto);
            createUserActivity(commented.getId(), UserActivityType.MOVIE_COMMENT, json);
        } catch (JsonProcessingException e) {
            LOGGER.error("error creating json object for MovieCommentDTO commentId: {}", dto.getCommentId());
        }

        return dto;
    }

    @Transactional
    public void deleteMovieComment(Long requestedId, Long commentId) {
        User user = userRepository.findById(requestedId).orElseThrow(UserNotAuthorizedException::new);
        MovieComment comment = movieCommentRepository.findById(commentId).orElseThrow(ResourceNotFoundException::new);
        if ( comment.getFromUser().equals(user) )
            movieCommentRepository.deleteById(commentId);
        else
            throw new UserNotAuthorizedException();
    }

    @Override
    public void reportMovieComment(Long commentId) {
        Optional<MovieComment> commentOpt = movieCommentRepository.findById(commentId);
        if ( commentOpt.isPresent() ) {
            MovieComment comment = commentOpt.get();
            comment.setIsReported(true);
            movieCommentRepository.save(comment);
        }
    }

    @Override
    public MovieCommentDTO updateMovieComment(Long userId, Long commentId, Boolean isSpoiler, String messageU) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        MovieComment comment = movieCommentRepository.findById(commentId).orElseThrow(ResourceNotFoundException::new);

        if (!comment.getFromUser().equals(user))
            throw new UserNotAuthorizedException();

        String message = messageU.trim();

        if (message.isEmpty())
            throw new APIException("message_cannot_be_null");

        comment.setIsSpoiler(isSpoiler);
        comment.setMessage(message);
        comment.setIsUpdated(true);
        movieCommentRepository.save(comment);
        return movieCommentToDTO(comment);
    }

    public MovieCommentDTO getMovieCommentById(Long commentId) {
        return movieCommentToDTO(movieCommentRepository.findById(commentId).orElseThrow(ResourceNotFoundException::new));
    }

    public Page<MovieCommentDTO> getMovieCommentForMovie(Integer movieId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        Page<MovieComment> dto = movieCommentRepository.findByMovie_MovieId(movieId, pageable);
        return movieCommentMapper.toDTOPage(dto);
    }

    public Page<MovieCommentDTO> getMovieCommentFromUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return movieCommentMapper.toDTOPage(movieCommentRepository.findByFromUser_Id(userId, pageable));
    }

    public Page<MovieCommentDTO> getMovieCommentForMovieFromUser(Integer movieId, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return movieCommentMapper.toDTOPage(movieCommentRepository.findByFromUser_IdAndMovie_MovieId(userId, movieId, pageable));
    }

    public Page<MovieCommentDTO> getMovieCommentReported(Boolean isReported, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return movieCommentMapper.toDTOPage( movieCommentRepository.findByIsReported(isReported, pageable));
    }

    @Override
    public void createNotification(Long userId, NotificationType type, String content) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("user_not_found"));
        Notification notification = new Notification();

        notification.setUser(user);
        notification.setType(type);
        notification.setContent(content);
        notification.setTime( System.currentTimeMillis() );

        Notification res = notificationRepository.save(notification);
        notificationMapper.toDTO(res);
    }

    @Override
    public NotificationDTO getNotification(Long userId, Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findByIdAndUserId(notificationId, userId);
        return notificationMapper.toDTO(notificationOpt.orElseThrow(() -> new ResourceNotFoundException("notification_not_found")));
    }

    @Override
    public Page<NotificationDTO> getUserNotifications(Long userId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "time"));
        Page<Notification> dto = notificationRepository.findByUserId(userId, pageable);
        return notificationMapper.toDTOPage(dto);
    }

    @Override
    public void userNotifiedBefore(Long userId, Long time) {
        int page = 0;
        int size = 500;
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationsPage;

        // Loop through the pages and apply the update
        do {
            notificationsPage = notificationRepository.findByUserIdAndTimeBefore(userId, time, pageable);

            if (!notificationsPage.isEmpty()) {
                notificationRepository.setLikedTrueBeforeTime(time, pageable.getPageSize());
            }

            page++;
            pageable = PageRequest.of(page, size);

        } while (notificationsPage.hasNext());
    }

    @Override
    public void userNotified(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId).orElseThrow(()->
            new ResourceNotFoundException("notification_not_found")
        );
        notification.setNotified(true);
        notificationRepository.save(notification);
    }

    @Override
    public void createUserActivity(Long userId, UserActivityType type, String content) {
        return;
        /*
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("user_not_found"));

        UserActivity activity = new UserActivity();
        activity.setUser(user);
        activity.setType(type);
        activity.setTime(System.currentTimeMillis());

        userActivityRepository.save(activity);
         */
    }

    @Override
    public Page<UserActivityDTO> getUserActivities(Long userId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "time"));
        Page<UserActivity> page = userActivityRepository.findByUserId(userId, pageable);
        return userActivityMapperImpl.toDTOPage(page);
    }

}
