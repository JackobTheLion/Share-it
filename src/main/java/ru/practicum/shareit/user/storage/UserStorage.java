package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user, Long userId);

    User getUser(Long userId);

    List<User> getAllUsers();

    void deleteUser(Long userId);
}