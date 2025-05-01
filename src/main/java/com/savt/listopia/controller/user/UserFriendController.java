package com.savt.listopia.controller.user;

import com.savt.listopia.config.AppConstants;
import com.savt.listopia.exception.userException.UserNotAuthorizedException;
import com.savt.listopia.exception.userException.UserNotFoundException;
import com.savt.listopia.payload.dto.UserFriendRequestDTO;
import com.savt.listopia.payload.response.APIResponse;
import com.savt.listopia.service.UserService;
import com.savt.listopia.service.user.UserFriendService;
import com.savt.listopia.util.UUIDParser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user/friend")
public class UserFriendController {
    private final UserService userService;
    private final UserFriendService userFriendService;

    public UserFriendController(UserService userService, UserFriendService userFriendService) {
        this.userService = userService;
        this.userFriendService = userFriendService;
    }

    @PostMapping("/add/{uuid}")
    public ResponseEntity<APIResponse> AddFriend(@Valid @PathVariable String uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotAuthorizedException::new);
        userFriendService.userSentOrAcceptFriendRequest(userId, UUIDParser.parse(uuid));
        return ResponseEntity.ok(APIResponse.builder().success(true).message("friend_request_sent").build());
    }

    @PostMapping("/reject/{uuid}")
    public ResponseEntity<APIResponse> rejectFriend(@Valid @PathVariable String uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        userFriendService.userRejectedFriend(userId, UUIDParser.parse(uuid));
        return ResponseEntity.ok(APIResponse.builder().success(true).message("friend_rejected").build());
    }

    @DeleteMapping("/remove/{uuid}")
    public ResponseEntity<APIResponse> removeFriend(@Valid @PathVariable String uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        userFriendService.userRemovedFriend(userId, UUIDParser.parse(uuid));
        return ResponseEntity.ok(APIResponse.builder().success(true).message("friend_rejected").build());
    }

    @GetMapping("/requests/received")
    public ResponseEntity<Page<UserFriendRequestDTO>> userFriendRequestsReceived(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        Page<UserFriendRequestDTO> requests = userFriendService.getUserFriendRequestsReceived(userId, pageNumber, pageSize);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<Page<UserFriendRequestDTO>> userFriendRequestsSent(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        Page<UserFriendRequestDTO> requests = userFriendService.getUserFriendRequestsSent(userId, pageNumber, pageSize);
        return ResponseEntity.ok(requests);
    }

    @DeleteMapping("/request/{uuid}")
    public ResponseEntity<APIResponse> removeFriendRequest(@Valid @PathVariable String uuid) {
        Long userId = userService.getCurrentUserId().orElseThrow(UserNotFoundException::new);
        userFriendService.userCancelFriendRequest(userId, UUIDParser.parse(uuid));
        return ResponseEntity.ok(APIResponse.builder().success(true).message("friend_request_cancelled").build());
    }
}
