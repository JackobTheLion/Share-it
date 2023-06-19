package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.UserMapper.mapFromDto;
import static ru.practicum.shareit.user.UserMapper.mapToDto;

@RestController
@Slf4j
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto addUser(@NotNull @Validated(ValidationGroups.Create.class) @RequestBody UserDto userDto) {
        log.info("Adding user {}", userDto);
        User user = mapFromDto(userDto);
        log.info("User mapped from DTO: {}", user);
        User addedUser = userService.add(user);
        log.info("User added: {}", addedUser);
        return mapToDto(addedUser);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@NotNull @Validated(ValidationGroups.Update.class) @RequestBody UserDto userDto,
                              @PathVariable Long userId) {

        log.info("Updating user id {} with {}", userId, userDto);
        User user = mapFromDto(userDto);
        user.setId(userId);
        log.info("User mapped from DTO: {}", user);
        User updatedUser = userService.update(user);
        log.info("User updated: {}", updatedUser);
        return mapToDto(updatedUser);
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable Long userId) {
        log.info("Looking for user id {}", userId);
        User user = userService.get(userId);
        log.info("User found: {}", user);
        return mapToDto(user);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Getting all users");
        List<UserDto> users = userService.findAll().stream().map(UserMapper::mapToDto).collect(Collectors.toList());
        log.info("Number of users found {}", users.size());
        return users;
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Deleting user id {}", userId);
        userService.delete(userId);
    }
}
