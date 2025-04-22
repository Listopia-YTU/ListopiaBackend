package com.savt.listopia.controller;


import com.savt.listopia.config.AppConstants;
import com.savt.listopia.exception.userException.UserNotAuthorizedException;
import com.savt.listopia.exception.userException.UserNotFoundException;
import com.savt.listopia.payload.APIResponse;
import com.savt.listopia.payload.dto.PrivateMessageDTO;
import com.savt.listopia.payload.dto.UserDTO;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String username) {
        LOGGER.info("getUser: {}", username);
        UserDTO dto = userService.getUserByUsername(username);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/change_username")
    public ResponseEntity<APIResponse> ChangeUsername(@Valid @RequestBody ChangeUsernameRequest changeUsernameRequest) {
        Long userId = userService.getCurrentUserIdOrThrow();
        LOGGER.trace("change_username:id: {}", userId);
        userService.ChangeUsername(userId, changeUsernameRequest.getNewUsername());
        return ResponseEntity.ok( APIResponse.builder().success(true).message("username_changed").build() );
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> Me() {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotAuthorizedException::new);
        UserDTO userDTO = userService.getUserById(userId);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/add_friend")
    public ResponseEntity<APIResponse> AddFriend(@Valid @RequestBody UUID uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        userService.UserFriendRequest(userId, uuid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("accept_friend")
    public ResponseEntity<APIResponse> AcceptFriend(@Valid @RequestBody UUID uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        userService.AcceptFriend(userId, uuid);
        return ResponseEntity.ok(APIResponse.builder().success(true).message("friend_added").build());
    }

    @GetMapping("/friend_requests")
    public ResponseEntity<List<UserDTO>> FriendRequests() {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        List<UserDTO> requests = userService.UserFriendRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{uuid}/friends")
    public ResponseEntity<List<UserDTO>> Friends(@PathVariable String uuid) {
        Long userId = userService.getUserIdFromUUID(UUID.fromString(uuid));
        List<UserDTO> friends = userService.UserFriends(userId);
        return ResponseEntity.ok(friends);
    }

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
    public ResponseEntity<List<PrivateMessageDTO>> Received(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
            ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        Page<PrivateMessageDTO> messages = userService.getAllMessagesOfUserReceived(userId, pageNumber, pageSize);
        return ResponseEntity.ok(messages.getContent());
    }

    @GetMapping("/message/sent")
    public ResponseEntity<List<PrivateMessageDTO>> Sent(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        Page<PrivateMessageDTO> messages = userService.getAllMessagesUserSent(userId, pageNumber, pageSize);
        return ResponseEntity.ok(messages.getContent());
    }

    @GetMapping("/message/from/{userUuid}")
    public ResponseEntity<List<PrivateMessageDTO>> MessagesFromUser(
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
        return ResponseEntity.ok(messageDTOS.getContent());
    }

    @GetMapping("/message/to/{userUuid}")
    public ResponseEntity<List<PrivateMessageDTO>> MessagesToUser(
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
        return ResponseEntity.ok(messageDTOS.getContent());
    }

}
