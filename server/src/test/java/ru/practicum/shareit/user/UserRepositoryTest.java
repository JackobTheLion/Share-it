package ru.practicum.shareit.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    public void addUsers() {
        user = User.builder()
                .name("name")
                .email("email@email.com")
                .build();

        userRepository.save(user);
    }

    @AfterEach
    public void cleaDb() {
        userRepository.deleteAll();
    }

    @Test
    public void findByEmailIgnoreCase_Normal() {
        User excpectedUser = User.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();

        User savedUser = userRepository.findByEmailIgnoreCase(user.getEmail());
        assertEquals(excpectedUser.getEmail(), savedUser.getEmail());
        assertEquals(excpectedUser.getName(), savedUser.getName());
    }

    @Test
    public void findByEmailIgnoreCase_NoSuchEmail() {
        User savedUser = userRepository.findByEmailIgnoreCase("No such email");
        assertNull(savedUser);
    }
}