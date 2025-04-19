package com.savt.listopia.controller;


import com.savt.listopia.exception.userException.UserNotFoundException;
import com.savt.listopia.payload.dto.PrivateMessageDTO;
import com.savt.listopia.payload.dto.UserDTO;
import com.savt.listopia.security.request.ChangeUsernameRequest;
import com.savt.listopia.security.request.MessageUserRequest;
import com.savt.listopia.service.UserService;
import jakarta.validation.Valid;
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

    @PostMapping("/change_username")
    public ResponseEntity<?> ChangeUsername(@Valid @RequestBody ChangeUsernameRequest changeUsernameRequest) {
        Long userId = userService.getCurrentUserIdOrThrow();
        LOGGER.trace("change_username:id: {}", userId);
        userService.ChangeUsername(userId, changeUsernameRequest.getNewUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> Me() {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        UserDTO userDTO = userService.getUserById(userId);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/add_friend")
    public ResponseEntity<?> AddFriend(@Valid @RequestBody UUID uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        userService.UserFriendRequest(userId, uuid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("accept_friend")
    public ResponseEntity<?> AcceptFriend(@Valid @RequestBody UUID uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        userService.AcceptFriend(userId, uuid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/friend_requests")
    public ResponseEntity<List<UserDTO>> FriendRequests() {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        List<UserDTO> requests = userService.UserFriendRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/friends")
    public ResponseEntity<List<UserDTO>> Friends() {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        List<UserDTO> friends = userService.UserFriends(userId);
        return ResponseEntity.ok(friends);
    }

    @PostMapping("/message")
    public ResponseEntity<?> Message(@Valid @RequestBody MessageUserRequest request) {
        userService.sendMessage(
                userService.getCurrentUserId().orElseThrow(() -> new UserNotFoundException("user_does_not_exists")),
                userService.getUserIdFromUUID(request.getTo()),
                request.getMessage()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/message/{id}/report")
    public ResponseEntity<?> MessageReport(@PathVariable String id) {
        try  {
            Long msgId = Long.parseLong(id);
            userService.userReportMessage(
                    userService.getCurrentUserId().orElseThrow(() -> new UserNotFoundException("user_does_not_exists")),
                    msgId
            );
            return ResponseEntity.ok().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/message/received")
    public ResponseEntity<List<PrivateMessageDTO>> Received(@RequestBody Integer page) {
        Long userId = userService.getCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException("user_does_not_exists"));
        Page<PrivateMessageDTO> messages = userService.getAllMessagesOfUserReceived(userId, page, 50);
        return ResponseEntity.ok(messages.getContent());
    }

    @GetMapping("/message/sent")
    public ResponseEntity<List<PrivateMessageDTO>> Sent(@RequestBody Integer page) {
        Long userId = userService.getCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException("user_does_not_exists"));
        Page<PrivateMessageDTO> messages = userService.getAllMessagesUserSent(userId, page, 50);
        return ResponseEntity.ok(messages.getContent());
    }

    @GetMapping("/message/from/{userUuid}")
    public ResponseEntity<List<PrivateMessageDTO>> MessagesFromUser(@PathVariable String userUuid, @RequestBody Integer page) {
        Long userId = userService.getCurrentUserId().orElseThrow(
                () -> new UserNotFoundException("user_does_not_exists")
        );
        Page<PrivateMessageDTO> messageDTOS = userService.getAllMessagesReceivedFrom(
                userId,
                userService.getUserIdFromUUID(UUID.fromString(userUuid)),
                page,
                50
        );
        return ResponseEntity.ok(messageDTOS.getContent());
    }

    @GetMapping("/message/to/{userUuid}")
    public ResponseEntity<List<PrivateMessageDTO>> MessagesToUser(@PathVariable String userUuid, @RequestBody Integer page) {
        Long userId = userService.getCurrentUserId().orElseThrow(
                () -> new UserNotFoundException("user_does_not_exists")
        );
        Page<PrivateMessageDTO> messageDTOS = userService.getAllMessagesSentTo(
                userId,
                userService.getUserIdFromUUID(UUID.fromString(userUuid)),
                page,
                50
        );
        return ResponseEntity.ok(messageDTOS.getContent());
    }

}
