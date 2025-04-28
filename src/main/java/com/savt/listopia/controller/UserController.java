package com.savt.listopia.controller;


import com.savt.listopia.config.AppConstants;
import com.savt.listopia.exception.APIException;
import com.savt.listopia.exception.userException.UserNotAuthorizedException;
import com.savt.listopia.exception.userException.UserNotFoundException;
import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.payload.APIResponse;
import com.savt.listopia.payload.dto.*;
import com.savt.listopia.payload.request.ChangeBiography;
import com.savt.listopia.payload.request.ChangePassword;
import com.savt.listopia.repository.MovieRepository;
import com.savt.listopia.security.request.ChangeUsernameRequest;
import com.savt.listopia.security.request.MessageUserRequest;
import com.savt.listopia.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final MovieRepository movieRepository;

    public UserController(UserService userService, MovieRepository movieRepository) {
        this.userService = userService;
        this.movieRepository = movieRepository;
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String username) {
        LOGGER.info("getUser: {}", username);
        UserDTO dto = userService.getUserByUsername(username);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/username")
    public ResponseEntity<APIResponse> ChangeUsername(@Valid @RequestBody ChangeUsernameRequest changeUsernameRequest) {
        Long userId = userService.getCurrentUserIdOrThrow();
        LOGGER.trace("change_username:id: {}", userId);
        userService.ChangeUsername(userId, changeUsernameRequest.getNewUsername());
        return ResponseEntity.ok( APIResponse.builder().success(true).message("username_changed").build() );
    }

    @PutMapping("/password")
    public ResponseEntity<APIResponse> changePassword(@Valid @RequestBody ChangePassword req) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotAuthorizedException::new);
        userService.changePassword(userId, req.getPassword());
        return ResponseEntity.ok( APIResponse.success("password_changed") );
    }

    @PutMapping("/biography")
    public ResponseEntity<APIResponse> changeBiography(@Valid @RequestBody ChangeBiography req) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotAuthorizedException::new);
        userService.changeBiography(userId, req.getBiography());
        return ResponseEntity.ok( APIResponse.success("biography_changed") );
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> Me() {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotAuthorizedException::new);
        UserDTO userDTO = userService.getUserById(userId);
        return ResponseEntity.ok(userDTO);
    }

    //////
    //// FRIEND
    //////

    @PostMapping("/friend/add/{uuid}")
    public ResponseEntity<APIResponse> AddFriend(@Valid @PathVariable String uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotAuthorizedException::new);
        userService.UserFriendRequest(userId, UUID.fromString(uuid));
        return ResponseEntity.ok(APIResponse.builder().success(true).message("friend_request_sent").build());
    }

    @PostMapping("/friend/accept/{uuid}")
    public ResponseEntity<APIResponse> AcceptFriend(@Valid @PathVariable String uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        userService.AcceptFriend(userId, UUID.fromString(uuid));
        return ResponseEntity.ok(APIResponse.builder().success(true).message("friend_added").build());
    }

    @PostMapping("/friend/reject/{uuid}")
    public ResponseEntity<APIResponse> rejectFriend(@Valid @PathVariable String uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        userService.rejectFriend(userId, UUID.fromString(uuid));
        return ResponseEntity.ok(APIResponse.builder().success(true).message("friend_rejected").build());
    }

    @DeleteMapping("/friend/remove/{uuid}")
    public ResponseEntity<APIResponse> removeFriend(@Valid @PathVariable String uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        userService.removeFriend(userId, UUID.fromString(uuid));
        return ResponseEntity.ok(APIResponse.builder().success(true).message("friend_rejected").build());
    }

    @GetMapping("/friend/requests/received")
    public ResponseEntity<Page<UserDTO>> userFriendRequestsReceived(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        Page<UserDTO> requests = userService.getUserFriendRequestsReceived(userId, pageNumber, pageSize);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/friend/requests/sent")
    public ResponseEntity<Page<UserDTO>> userFriendRequestsSent(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        Page<UserDTO> requests = userService.getUserFriendRequestsSent(userId, pageNumber, pageSize);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/uuid/{uuid}/friends")
    public ResponseEntity<Page<UserDTO>> Friends(
            @PathVariable String uuid,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        return ResponseEntity.ok(
                userService.UserFriends(userService.getUserIdFromUUID(UUID.fromString(uuid)),
                pageNumber, pageSize)
        );
    }

    @GetMapping("/uuid/{uuid}/liked_movies")
    public ResponseEntity<Page<MovieFrontDTO>> getUserLikedMovies(
            @PathVariable UUID uuid,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        Long userId = userService.getUserIdFromUUID(uuid);
        Page<MovieFrontDTO> likedMovies = userService.getUserLikedMovies(userId, pageNumber, pageSize);
        return ResponseEntity.ok(likedMovies);
    }

    //////
    //// MOVIE
    //////

    @PutMapping("/movie/{movieId}/like")
    public ResponseEntity<APIResponse> likeMovie(
            @PathVariable Integer movieId,
            @RequestParam Boolean liked
    ) {
        Long userId = userService.getCurrentUserIdOrThrow();

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new APIException("movie_not_found"));

        userService.likeMovie(userId, movie, liked);

        return ResponseEntity.ok(APIResponse.builder().message("movie_like_updated").success(true).build());
    }

    @GetMapping("/uuid/{uuid}/watchlist")
    public ResponseEntity<Page<MovieFrontDTO>> getUserWatchlist(
            @PathVariable String uuid,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        Long userId = userService.getUserIdFromUUID(UUID.fromString(uuid));
        Page<MovieFrontDTO> movieFrontDTOPage = userService.getUserWatchlist(userId, pageNumber, pageSize);
        return ResponseEntity.ok(movieFrontDTOPage);
    }

    @PostMapping("/movie/{movieId}/watchlist")
    public ResponseEntity<APIResponse> addToWatchlist(
            @PathVariable(name = "movieId") Integer movieId
    ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        userService.userAddToWatchlist(userId, movieId);
        return ResponseEntity.ok(APIResponse.success("added_to_watchlist"));
    }

    @DeleteMapping("/movie/{movieId}/watchlist")
    public ResponseEntity<APIResponse> removeFromWatchlist(
            @PathVariable(name = "movieId") Integer movieId
    ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        userService.userDeleteFromWatchlist(userId, movieId);
        return ResponseEntity.ok(APIResponse.success("deleted_from_watchlist"));
    }

    @GetMapping("/uuid/{uuid}/watched")
    public ResponseEntity<Page<MovieFrontDTO>> getUserWatched(
            @PathVariable String uuid,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        Long userId = userService.getUserIdFromUUID(UUID.fromString(uuid));
        Page<MovieFrontDTO> movieFrontDTOPage = userService.getUserWatched(userId, pageNumber, pageSize);
        return ResponseEntity.ok(movieFrontDTOPage);
    }

    @PostMapping("/movie/{movieId}/watched")
    public ResponseEntity<APIResponse> addToWatched(
            @PathVariable(name = "movieId") Integer movieId
    ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        userService.userAddToWatched(userId, movieId);
        return ResponseEntity.ok(APIResponse.success("added_to_watched"));
    }

    @DeleteMapping("/movie/{movieId}/watched")
    public ResponseEntity<APIResponse> removeFromWatched(
            @PathVariable(name = "movieId") Integer movieId
    ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        userService.userDeleteFromWatched(userId, movieId);
        return ResponseEntity.ok(APIResponse.success("deleted_from_watched"));
    }

    //////
    //// MESSAGE
    //////

    @PostMapping("/message")
    public ResponseEntity<APIResponse> Message(@Valid @RequestBody MessageUserRequest request) {
        userService.sendMessage(
                userService.getCurrentUserId().orElseThrow(() -> new UserNotFoundException("user_does_not_exists")),
                userService.getUserIdFromUUID(request.getTo()),
                request.getMessage()
        );
        return ResponseEntity.ok( APIResponse.builder().success(true).message("sent_message").build() );
    }

    @PostMapping("/message/{id}/report")
    public ResponseEntity<APIResponse> MessageReport(@PathVariable Long id) {
        userService.userReportMessage(
                userService.getCurrentUserIdOrThrow(),
                id
        );
        return ResponseEntity.ok(APIResponse.builder().success(true).message("message_reported").build());
    }

    @GetMapping("/message/received")
    public ResponseEntity<Page<PrivateMessageDTO>> Received(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
            ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        Page<PrivateMessageDTO> messages = userService.getAllMessagesOfUserReceived(userId, pageNumber, pageSize);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/message/sent")
    public ResponseEntity<Page<PrivateMessageDTO>> Sent(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        Page<PrivateMessageDTO> messages = userService.getAllMessagesUserSent(userId, pageNumber, pageSize);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/message/received/{userUuid}")
    public ResponseEntity<Page<PrivateMessageDTO>> MessagesFromUser(
            @PathVariable String userUuid,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
            ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        Page<PrivateMessageDTO> messageDTOS = userService.getAllMessagesReceivedFrom(
                userId,
                userService.getUserIdFromUUID(UUID.fromString(userUuid)),
                pageNumber,
                pageSize
        );
        return ResponseEntity.ok(messageDTOS);
    }

    @GetMapping("/message/sent/{userUuid}")
    public ResponseEntity<Page<PrivateMessageDTO>> MessagesToUser(
            @PathVariable String userUuid,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
            ){
        Long userId = userService.getCurrentUserIdOrThrow();
        Page<PrivateMessageDTO> messageDTOS = userService.getAllMessagesSentTo(
                userId,
                userService.getUserIdFromUUID(UUID.fromString(userUuid)),
                pageNumber,
                pageSize
        );
        return ResponseEntity.ok(messageDTOS);
    }

    //////
    //// NOTIFICATIONS
    //////

    @GetMapping("/notifications/{notificationId}")
    public ResponseEntity<NotificationDTO> getNotification(@PathVariable String notificationId) {
        Long userId = userService.getCurrentUserIdOrThrow();
        Long id = Long.valueOf(notificationId);
        NotificationDTO dto = userService.getNotification(userId, id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/notifications")
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        Page<NotificationDTO> dto = userService.getUserNotifications(
                userId,
                pageNumber,
                pageSize
        );
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/notifications")
    public ResponseEntity<APIResponse> notifiedUser(
            @RequestParam(name = "notifiedBefore") Long timeBefore
    ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        userService.userNotifiedBefore(userId, timeBefore);
        return ResponseEntity.ok(APIResponse.success("user_notified"));
    }

    @PutMapping("/notifications/{notificationId}")
    public ResponseEntity<APIResponse> notifiedUser(
            @PathVariable String notificationId) {
        Long userId = userService.getCurrentUserIdOrThrow();
        Long id = Long.valueOf(notificationId);
        userService.userNotified(userId, id);
        return ResponseEntity.ok(APIResponse.success("notified"));
    }

    //////
    //// ACTIVITY
    //////

    @GetMapping("/uuid/{uuid}/activity")
    public ResponseEntity<Page<UserActivityDTO>> getUserActivity(
            @PathVariable String uuid,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        Long userId = userService.getUserIdFromUUID(UUID.fromString(uuid));
        Page<UserActivityDTO> activityDTOS = userService.getUserActivities(userId, pageNumber, pageSize);
        return ResponseEntity.ok(activityDTOS);
    }

}
