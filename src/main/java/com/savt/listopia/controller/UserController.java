package com.savt.listopia.controller;


import com.savt.listopia.payload.dto.UserDTO;
import com.savt.listopia.security.request.ChangeUsernameRequest;
import com.savt.listopia.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
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
        Long userId = userService.getCurrentUserId().orElseThrow();
        userService.ChangeUsername(userId, changeUsernameRequest.getNewUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> Me() {
        Long userId = userService.getCurrentUserId().orElseThrow();
        UserDTO userDTO = userService.getUserById(userId);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/add_friend")
    public ResponseEntity<?> AddFriend(@Valid @RequestBody UUID uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow();
        userService.UserFriendRequest(userId, uuid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("accept_friend")
    public ResponseEntity<?> AcceptFriend(@Valid @RequestBody UUID uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow();
        userService.AcceptFriend(userId, uuid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/friend_requests")
    public ResponseEntity<List<UserDTO>> FriendRequests() {
        Long userId = userService.getCurrentUserId().orElseThrow();
        List<UserDTO> requests = userService.UserFriendRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/friends")
    public ResponseEntity<List<UserDTO>> Friends() {
        Long userId = userService.getCurrentUserId().orElseThrow();
        List<UserDTO> friends = userService.UserFriends(userId);
        return ResponseEntity.ok(friends);
    }

}
