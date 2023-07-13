package ru.practicum.shareit.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.mapper.UserMapper.mapFromDto;
import static ru.practicum.shareit.user.mapper.UserMapper.mapToDto;

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
    public UserResponseDto addUser(@NotNull @Validated(ValidationGroups.Create.class) @RequestBody UserRequestDto userRequestDto) {
        log.info("Adding user {}", userRequestDto);
        User user = mapFromDto(userRequestDto);
        log.info("User mapped from DTO: {}", user);
        User addedUser = userService.add(user);
        log.info("User added: {}", addedUser);
        return mapToDto(addedUser);
    }

    @PatchMapping("/{userId}")
    public UserResponseDto updateUser(@NotNull @Validated(ValidationGroups.Update.class) @RequestBody UserRequestDto userRequestDto,
                                      @PathVariable Long userId) {

        log.info("Updating user id {} with {}", userId, userRequestDto);
        User user = mapFromDto(userRequestDto, userId);
        log.info("User mapped from DTO: {}", user);
        User updatedUser = userService.update(user);
        log.info("User updated: {}", updatedUser);
        return mapToDto(updatedUser);
    }

    @GetMapping("/{userId}")
    public UserResponseDto getUser(@PathVariable Long userId) {
        log.info("Looking for user id {}", userId);
        User user = userService.get(userId);
        log.info("User found: {}", user);
        return mapToDto(user);
    }

    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        log.info("Getting all users");
        List<UserResponseDto> users = userService.findAll().stream().map(UserMapper::mapToDto).collect(Collectors.toList());
        log.info("Number of users found {}", users.size());
        return users;
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Deleting user id {}", userId);
        userService.delete(userId);
    }
}