package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Component
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User add(User user) {
        if (user.getEmail() == null) {
            throw new ValidationException("Email cannot be null");
        }
        log.info("Adding user {}", user);
        return userStorage.add(user);
    }

    public User update(User updatedUser) {
        log.info("Updating user with {}", updatedUser);
        User savedUser = userStorage.get(updatedUser.getId());
        if (savedUser == null) {
            throw new UserNotFoundException(String.format("User id %s not found.", updatedUser.getId()));
        }
        if (updatedUser.getName() != null) {
            savedUser.setName(updatedUser.getName());
        }
        return userStorage.update(updatedUser);
    }

    public User get(Long userId) {
        log.info("Looking for user id {}", userId);
        User user = userStorage.get(userId);
        log.info("User found: {}", user);
        return user;
    }

    public List<User> getAll() {
        log.info("Getting all users");
        List<User> users = userStorage.getAll();
        log.info("Number of users found {}", users.size());
        return users;
    }

    public void delete(Long userId) {
        log.info("Deleting user id {}", userId);
        userStorage.delete(userId);
    }
}
