package com.savt.listopia.controller.admin;

import com.savt.listopia.exception.userException.UserNotFoundException;
import com.savt.listopia.model.user.User;
import com.savt.listopia.model.user.UserRole;
import com.savt.listopia.payload.dto.UserDTO;
import com.savt.listopia.repository.UserRepository;
import com.savt.listopia.service.AuthService;
import com.savt.listopia.service.UserService;
import com.savt.listopia.util.UUIDParser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user")
public class AdminUserController {
    private final AuthService authService;
    private final UserService userService;
    private final UserRepository userRepository;

    public AdminUserController(AuthService authService, UserService userService, UserRepository userRepository) {
        this.authService = authService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @DeleteMapping("/uuid/{uuid}")
    public void deleteAccount(@PathVariable("uuid") String uuid) {
        authService.requireRoleOrThrow(UserRole.ADMIN);

        Long userId = userService.getUserIdFromUUID(UUIDParser.parse(uuid));
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        userService.deleteAccount(user.getId());
    }

    @PutMapping("/{uuid}")
    public void editAccount(
            @PathVariable("uuid") String uuid,
            @RequestBody UserDTO user
        ) {
        authService.requireRoleOrThrow(UserRole.ADMIN);
        Long userId = userService.getUserIdFromUUID(UUIDParser.parse(uuid));
        userService.editUser(userId, user);
    }

}
