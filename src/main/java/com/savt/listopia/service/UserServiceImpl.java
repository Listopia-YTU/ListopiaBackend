package com.savt.listopia.service;

import com.savt.listopia.exception.APIException;
import com.savt.listopia.exception.ResourceNotFoundException;
import com.savt.listopia.exception.userException.UserException;
import com.savt.listopia.exception.userException.UserNotAuthorizedException;
import com.savt.listopia.exception.userException.UserNotFoundException;
import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.user.MovieComment;
import com.savt.listopia.model.user.PrivateMessage;
import com.savt.listopia.model.user.User;
import com.savt.listopia.payload.dto.MovieCommentDTO;
import com.savt.listopia.payload.dto.MovieFrontDTO;
import com.savt.listopia.payload.dto.PrivateMessageDTO;
import com.savt.listopia.payload.dto.UserDTO;
import com.savt.listopia.repository.MovieCommentRepository;
import com.savt.listopia.repository.MovieRepository;
import com.savt.listopia.repository.PrivateMessageRepository;
import com.savt.listopia.repository.UserRepository;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PrivateMessageRepository privateMessageRepository;
    private final MovieRepository movieRepository;
    private final MovieCommentRepository movieCommentRepository;

    public static <D, T> Page<D> mapEntityPageToDtoPage(Page<T> entities, Class<D> dtoClass, ModelMapper mapper) {
        List<D> dtoList = entities.getContent().stream()
                .map(entity -> mapper.map(entity, dtoClass))
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, entities.getPageable(), entities.getTotalElements());
    }

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PrivateMessageRepository privateMessageRepository, MovieRepository movieRepository, MovieCommentRepository movieCommentRepository) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.privateMessageRepository = privateMessageRepository;
        this.movieRepository = movieRepository;
        this.movieCommentRepository = movieCommentRepository;
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        return modelMapper.map(user, UserDTO.class);
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

    @Transactional
    public Page<MovieFrontDTO> getUserLikedMovies(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> movies = userRepository.findLikedMoviesByUserId(userId, pageable);
        return mapEntityPageToDtoPage(movies, MovieFrontDTO.class, modelMapper);
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
            }
        } else {
            user.getLikedMovies().remove(movie);
            userRepository.save(user);
            movie.setLikeCount(movie.getLikeCount() - 1);
            movieRepository.save(movie);
        }
    }

    @Transactional
    public void MakeFriends(Long userId, Long friendId) {
        User user = userRepository.findById(userId).orElseThrow();
        User friend = userRepository.findById(friendId).orElseThrow();
        user.getFriends().add(friend);
        friend.getFriends().add(user);
        userRepository.save(user);
        userRepository.save(friend);
    }

    @Transactional
    public void UserFriendRequest(Long requestOwnerUserId, UUID requestedUserUuid) {
        User requester = userRepository.findById(requestOwnerUserId).orElseThrow(UserNotFoundException::new);
        User requested = userRepository.findByUuid(requestedUserUuid).orElseThrow(UserNotFoundException::new);
        if (Objects.equals(requester.getId(), requested.getId()))
            return;
        requested.getFriendRequests().add(requester);
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
        PrivateMessage message = new PrivateMessage();
        // message.setFromUserId(fromId);
        message.setFromUser( userRepository.findById(fromId).orElseThrow(UserNotFoundException::new) );
        // message.setToUserId(toId);
        message.setToUser( userRepository.findById(toId).orElseThrow(UserNotFoundException::new) );
        message.setSentAtTimestampSeconds(Instant.now().getEpochSecond());
        message.setMessage(messageStr);
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
        return MovieCommentDTO.builder()
                .id(comment.getId())
                .sentAtTimestampSeconds(comment.getSentAtTimestampSeconds())
                .userUUID(comment.getFromUser().getUuid().toString())
                .movieId(comment.getMovie().getMovieId())
                .message(comment.getMessage())
                .build();
    }

    @Transactional
    public MovieCommentDTO createMovieComment(Long userId, Integer movieId, Boolean isSpoiler, String message) {
        User commented = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Movie movie = movieRepository.findById(movieId).orElseThrow(ResourceNotFoundException::new);

        MovieComment comment = new MovieComment();
        comment.setMovie(movie);
        comment.setFromUser(commented);
        comment.setIsSpoiler(isSpoiler);
        comment.setSentAtTimestampSeconds(Instant.now().getEpochSecond());
        comment.setMessage(message);

        movieCommentRepository.save(comment);

        return movieCommentToDTO(comment);
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
    public MovieCommentDTO updateMovieComment(Long userId, Long commentId, Boolean isSpoiler, String message) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        MovieComment comment = movieCommentRepository.findById(commentId).orElseThrow(ResourceNotFoundException::new);

        if (!comment.getFromUser().equals(user))
            throw new UserNotAuthorizedException();

        comment.setIsSpoiler(isSpoiler);
        comment.setMessage(message);
        movieCommentRepository.save(comment);
        return movieCommentToDTO(comment);
    }

    public MovieCommentDTO getMovieCommentById(Long commentId) {
        return movieCommentToDTO(movieCommentRepository.findById(commentId).orElseThrow(ResourceNotFoundException::new));
    }

    public Page<MovieCommentDTO> getMovieCommentForMovie(Integer movieId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return movieCommentRepository.findByMovie_MovieId(movieId, pageable).map(this::movieCommentToDTO);
    }

    public Page<MovieCommentDTO> getMovieCommentFromUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return movieCommentRepository.findByFromUser_Id(userId, pageable).map(this::movieCommentToDTO);
    }

    public Page<MovieCommentDTO> getMovieCommentForMovieFromUser(Integer movieId, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return movieCommentRepository.findByFromUser_IdAndMovie_MovieId(userId, movieId, pageable).map(this::movieCommentToDTO);
    }

    public Page<MovieCommentDTO> getMovieCommentReported(Boolean isReported, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return movieCommentRepository.findByIsReported(isReported, pageable).map(this::movieCommentToDTO);
    }

}
