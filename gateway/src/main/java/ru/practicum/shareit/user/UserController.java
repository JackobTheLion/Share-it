package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.constraints.NotNull;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@NotNull @Validated(ValidationGroups.Create.class)
                                             @RequestBody UserRequestDto userRequestDto) {
        log.info("Adding user {}", userRequestDto);
        ResponseEntity<Object> response = userClient.addUser(userRequestDto);
        log.info("Response: {}", response);
        return response;
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@NotNull @Validated(ValidationGroups.Update.class)
                                             @RequestBody UserRequestDto userRequestDto,
                                             @PathVariable Long userId) {

        log.info("Updating user id {} with {}", userId, userRequestDto);
        ResponseEntity<Object> response = userClient.updateUser(userId, userRequestDto);
        log.info("Response: {}", response);
        return response;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable Long userId) {
        log.info("Looking for user id {}", userId);
        ResponseEntity<Object> response = userClient.getUser(userId);
        log.info("Response: {}", response);
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Getting all users");
        ResponseEntity<Object> response = userClient.getAllUsers();
        log.info("Response: {}", response);
        return response;
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long userId) {
        log.info("Deleting user id {}", userId);
        ResponseEntity<Object> response = userClient.deleteUser(userId);
        log.info("Response: {}", response);
        return response;
    }
}
