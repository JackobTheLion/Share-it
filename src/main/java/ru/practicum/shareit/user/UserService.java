package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.exceptions.EmailRegisteredException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Component
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User add(User user) {
        if (user.getEmail() == null) {
            throw new ValidationException("Email cannot be null");
        }
        log.info("Adding user {}", user);
        return userRepository.save(user);
    }

    public User update(User user) {
        log.info("Updating user with {}", user);
        User savedUser = userRepository.findById(user.getId()).orElseThrow(() -> {
            log.info("User with id {} not found.", user.getId());
            throw new UserNotFoundException(String.format("User with id %s not found", user.getId()));
        });

        if (user.getName() != null && !savedUser.getName().equals(user.getName())) {
            savedUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            if (savedUser.getEmail().equals(user.getEmail())) {
                return userRepository.save(savedUser);
            } else if (userRepository.findByEmailContainingIgnoreCase(user.getEmail()) != null) {
                throw new EmailRegisteredException("User with such email already registered");
            } else if (!savedUser.getEmail().equals(user.getEmail())) {
                savedUser.setEmail(user.getEmail());
            }
        }
        return userRepository.save(savedUser);
    }

    public User get(Long userId) {
        log.info("Looking for user id {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.info("User id {} not found", userId);
            throw new UserNotFoundException(String.format("User id %s not found", userId));
        });
        log.info("User found: {}", user);
        return user;
    }

    public List<User> findAll() {
        log.info("Getting all users");
        List<User> users = userRepository.findAll();
        log.info("Number of users found {}", users.size());
        return users;
    }

    public void delete(Long userId) {
        log.info("Deleting user id {}", userId);
        userRepository.deleteById(userId);
    }
}
