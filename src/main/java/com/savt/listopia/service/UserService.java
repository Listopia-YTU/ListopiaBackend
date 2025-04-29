package com.savt.listopia.service;

import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.user.*;
import com.savt.listopia.payload.dto.*;

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
    void changePassword(Long userId, String password);
    void changeBiography(Long userId, String biography);

    @Transactional
    Page<MovieFrontDTO> getUserLikedMovies(Long userId, int page, int size);
    @Transactional
    void likeMovie(Long userId, Movie movie, Boolean liked);

    Page<MovieFrontDTO> getUserWatchlist(Long userId, int pageNumber, int pageSize);
    void userAddToWatchlist(Long userId, Integer movieId);
    void userDeleteFromWatchlist(Long userId, Integer movieId);

    Page<MovieFrontDTO> getUserWatched(Long userId, int pageNumber, int pageSize);
    void userAddToWatched(Long userId, Integer movieId);
    void userDeleteFromWatched(Long userId, Integer movieId);

    void makeFriends(Long receiverId, Long senderId);
    void userSentRequestTo(Long requestOwnerUserId, UUID requestedUserUuid);
    void userAcceptedFriend(Long accepterId, UUID requestId);
    void userCancelFriendRequest(Long userId, UUID cancelledUserUuid);
    void userRejectedFriend(Long userId, UUID friendUUID);
    void userRemovedFriend(Long userId, UUID friendUUID);
    Page<UserFriendRequestDTO> getUserFriendRequestsReceived(Long userId, int page, int size);
    Page<UserFriendRequestDTO> getUserFriendRequestsSent(Long userId, int page, int size);
    Page<UserDTO> getUserFriends(Long userId, int page, int size);

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

    void createNotification(Long userId, NotificationType type, String content);
    NotificationDTO getNotification(Long userId, Long notificationId);
    Page<NotificationDTO> getUserNotifications(Long userId, int pageNumber, int pageSize);
    void userNotifiedBefore(Long userId, Long time);
    void userNotified(Long userId, Long notificationId);

    void createUserActivity(Long userId, UserActivityType type, String content);
    Page<UserActivityDTO> getUserActivities(Long userId, int pageNumber, int pageSize);
}
