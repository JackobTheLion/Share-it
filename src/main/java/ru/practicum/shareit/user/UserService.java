package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ValidationException;
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

    public User addUser(User user) {
        if (user.getEmail() == null) {
            throw new ValidationException("Email cannot be null");
        }
        log.info("Adding user {}", user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user, Long userId) {
        log.info("Updating user id {} with {}", userId, user);
        return userStorage.updateUser(user, userId);
    }

    public User getUser(Long userId) {
        log.info("Looking for user id {}", userId);
        User user = userStorage.getUser(userId);
        log.info("User found: {}", user);
        return user;
    }

    public List<User> getAllUsers() {
        log.info("Getting all users");
        List<User> users = userStorage.getAllUsers();
        log.info("Number of users found {}", users.size());
        return users;
    }

    public void deleteUser(Long userId) {
        log.info("Deleting user id {}", userId);
        userStorage.deleteUser(userId);
    }
}
