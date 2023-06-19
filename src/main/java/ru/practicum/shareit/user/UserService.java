package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.exceptions.EmailRegisteredException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.persistence.EntityNotFoundException;
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
        if (userStorage.findByEmailContainingIgnoreCase(user.getEmail()) != null) {
            throw new EmailRegisteredException("User with such email already registered");
        }
        log.info("Adding user {}", user);
        return userStorage.save(user);
    }

    public User update(User user) {
        log.info("Updating user with {}", user);
        try {
            User savedUser = userStorage.getById(user.getId());
            if (user.getName() != null && !savedUser.getName().equals(user.getName())) {
                savedUser.setName(user.getName());
            }
            if (user.getEmail() != null) {
                if (savedUser.getEmail().equals(user.getEmail())) {
                    return userStorage.save(savedUser);
                } else if (userStorage.findByEmailContainingIgnoreCase(user.getEmail()) != null) {
                    throw new EmailRegisteredException("User with such email already registered");
                } else if (!savedUser.getEmail().equals(user.getEmail())) {
                    savedUser.setEmail(user.getEmail());
                }
            }
            return userStorage.save(savedUser);
        } catch (EntityNotFoundException e) {
            log.info("User with id {} not found.", user.getId());
            throw new UserNotFoundException(String.format("User with id %s not found", user.getId()));
        }
    }

    public User get(Long userId) {
        log.info("Looking for user id {}", userId);
        try {
            User user = userStorage.getById(userId);
            log.info("User found: {}", user);
            return user;
        } catch (EntityNotFoundException e) {
            log.info("User id {} not found", userId);
            throw new UserNotFoundException(String.format("User id %s not found", userId));
        }
    }

    public List<User> findAll() {
        log.info("Getting all users");
        List<User> users = userStorage.findAll();
        log.info("Number of users found {}", users.size());
        return users;
    }

    public void delete(Long userId) {
        log.info("Deleting user id {}", userId);
        userStorage.deleteById(userId);
    }
}
