package ru.practicum.shareit.user.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.exceptions.EmailRegisteredException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Qualifier("inMem")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long id = 0L;

    @Override
    public User addUser(User user) {
        isEmailExist(user);
        user.setId(getId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user, Long userId) {
        User saveduser = getUser(userId);
        if (user.getEmail() != null && !saveduser.getEmail().equals(user.getEmail())) {
            isEmailExist(user);
            saveduser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            saveduser.setName(user.getName());
        }
        return saveduser;
    }

    @Override
    public User getUser(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new UserNotFoundException(String.format("User id %s not found.", userId));
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteUser(Long userId) {
        getUser(userId);
        users.remove(userId);
    }

    private Long getId() {
        id++;
        return id;
    }

    private void isEmailExist(User user) {
        for (User u : users.values()) {
            if (user.getEmail().equals(u.getEmail())) {
                throw new EmailRegisteredException("User with such email already registered");
            }
        }
    }
}
