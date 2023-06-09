package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    User add(User user);

    User update(User user);

    User get(Long userId);

    List<User> getAll();

    void delete(Long userId);
}