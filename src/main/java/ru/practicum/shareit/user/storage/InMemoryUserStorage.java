package ru.practicum.shareit.user.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.exceptions.EmailRegisteredException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
@Qualifier("inMem")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private Long id = 0L;

    @Override
    public User add(User user) {
        isEmailExist(user);
        user.setId(getId());
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        return user;
    }

    @Override
    public User update(User user) {
        User saveduser = get(user.getId());
        if (user.getEmail() != null && !user.getEmail().equals(saveduser.getEmail())) {
            isEmailExist(user);
            emails.remove(saveduser.getEmail());
            saveduser.setEmail(user.getEmail());
            emails.add(saveduser.getEmail());
        }
        if (user.getName() != null) {
            saveduser.setName(user.getName());
        }
        return saveduser;
    }

    @Override
    public User get(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new UserNotFoundException(String.format("User id %s not found.", userId));
        }
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(Long userId) {
        get(userId);
        User removedUser = users.remove(userId);
        emails.remove(removedUser.getEmail());
    }

    private Long getId() {
        id++;
        return id;
    }

    private void isEmailExist(User user) {
        if (emails.contains(user.getEmail())) {
            throw new EmailRegisteredException(String.format("Email: %s already exists", user.getEmail()));
        }
    }
}
