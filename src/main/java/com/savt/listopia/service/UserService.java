package com.savt.listopia.service;

import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.user.MovieComment;
import com.savt.listopia.model.user.User;
import com.savt.listopia.payload.dto.MovieCommentDTO;
import com.savt.listopia.payload.dto.MovieFrontDTO;
import com.savt.listopia.payload.dto.PrivateMessageDTO;
import com.savt.listopia.payload.dto.UserDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    UserDTO getUserByUsername(String username);
    User registerUser(String firstname, String lastName, String email, String username, String plainPassword);
    User getUserByEmailPassword(String email, String plainPassword);
    boolean verifyUserPassword(User user, String enteredPassword);
    UserDTO getUserById(Long id);
    Optional<Long> getCurrentUserId();
    Long getCurrentUserIdOrThrow();
    UUID getUUIDFromUserId(Long userId);
    void ChangeUsername(Long userId, String username);
    Long getUserIdFromUUID(UUID uuid);

    @Transactional
    List<MovieFrontDTO> getUserLikedMovies(Long userId);
    @Transactional
    void likeMovie(Long userId, Movie movie, Boolean liked);

    @Transactional
    void MakeFriends(Long userId, Long friendId);
    @Transactional
    void UserFriendRequest(Long requestOwnerUserId, UUID requestedUserUuid);
    @Transactional
    void AcceptFriend(Long accepterId, UUID acceptedUUID);
    @Transactional
    List<UserDTO> UserFriendRequests(Long userId);
    @Transactional
    List<UserDTO> UserFriends(Long userId);

    @Transactional
    void userReportMessage(Long userId, Long messageId);
    @Transactional
    void sendMessage(Long fromId, Long toId, String messageUnsafe);
    @Transactional
    void markPrivateMessageReported(Long messageId);
    Boolean isPrivateMessageReported(Long messageId);
    Page<PrivateMessageDTO> getAllReportedMessages(int page, int size);
    Page<PrivateMessageDTO> getAllMessagesOfUserReceived(Long userId, int page, int size);
    Page<PrivateMessageDTO> getAllMessagesUserSent(Long userId, int page, int size);
    Page<PrivateMessageDTO> getAllMessagesSentTo(Long userId, Long toId, int page, int size);
    Page<PrivateMessageDTO> getAllMessagesReceivedFrom(Long userId, Long fromId, int page, int size);

    MovieCommentDTO movieCommentToDTO(MovieComment comment);
    @Transactional
    MovieCommentDTO createMovieComment(Long userId, Integer movieId, Boolean isSpoiler, String messageUnsafe);
    @Transactional
    void deleteMovieComment(Long requestedId, Long commentId);
    @Transactional
    void reportMovieComment(Long commentId);
    @Transactional
    MovieCommentDTO updateMovieComment(Long userId, Long commentId, Boolean isSpoiler, String messageUnsafe);
    MovieCommentDTO getMovieCommentById(Long commentId);
    Page<MovieCommentDTO> getMovieCommentForMovie(Integer movieId, int page, int size);
    Page<MovieCommentDTO> getMovieCommentFromUser(Long userId, int page, int size);
    Page<MovieCommentDTO> getMovieCommentForMovieFromUser(Integer movieId, Long userId, int page, int size);
    Page<MovieCommentDTO> getMovieCommentReported(Boolean isReported, int page, int size);
}
