package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.exceptions.EmailRegisteredException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    public void addUser_Normal() {
        User userToSave = User.builder()
                .name("name")
                .email("email@email.ru")
                .build();

        User expectedUser = User.builder()
                .name(userToSave.getName())
                .email(userToSave.getEmail())
                .build();

        when(userRepository.save(userToSave)).thenReturn(expectedUser);

        User savedUser = userService.add(userToSave);
        verify(userRepository, times(1)).save(userToSave);
        assertEquals(expectedUser, savedUser);
    }

    @Test
    public void updateUser_updateName_Normal() {
        User updatedUser = User.builder()
                .id(1L)
                .name("updated name")
                .build();

        User savedUser = User.builder()
                .id(updatedUser.getId())
                .name("name")
                .email("email@email.ru")
                .build();

        User expectedUser = User.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .email(savedUser.getEmail())
                .build();

        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        User result = userService.update(updatedUser);
        verify(userRepository, times(1)).findById(updatedUser.getId());
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(expectedUser, result);
    }

    @Test
    public void userUpdate_wrongId() {
        User user = User.builder().id(9999L).build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        Throwable e = assertThrows(UserNotFoundException.class, () -> userService.update(user));

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(0)).save(any(User.class));
        assertEquals(String.format("User with id %s not found", user.getId()), e.getMessage());
    }

    @Test
    public void updateUser_updateName_nullName() {
        User updatedUser = User.builder()
                .id(1L)
                .name(null)
                .build();

        User savedUser = User.builder()
                .id(updatedUser.getId())
                .name("name")
                .email("email@email.ru")
                .build();

        User expectedUser = User.builder()
                .id(updatedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .build();

        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        User result = userService.update(updatedUser);
        verify(userRepository, times(1)).findById(updatedUser.getId());
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(expectedUser, result);
    }

    @Test
    public void updateUser_updateName_sameName() {
        User updatedUser = User.builder()
                .id(1L)
                .name("name")
                .build();

        User savedUser = User.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .email("email@email.ru")
                .build();

        User expectedUser = User.builder()
                .id(updatedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .build();

        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        User result = userService.update(updatedUser);
        verify(userRepository, times(1)).findById(updatedUser.getId());
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(expectedUser, result);
    }

    @Test
    public void updateUser_updateEmail_Normal() {
        User updatedUser = User.builder()
                .id(1L)
                .email("newEmail@email.ru")
                .build();

        User savedUser = User.builder()
                .id(updatedUser.getId())
                .name("name")
                .email("email@email.ru")
                .build();

        User expectedUser = User.builder()
                .id(updatedUser.getId())
                .name(savedUser.getName())
                .email(updatedUser.getEmail())
                .build();

        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.of(savedUser));
        when(userRepository.save(expectedUser)).thenReturn(expectedUser);

        User result = userService.update(updatedUser);
        verify(userRepository, times(1)).findById(updatedUser.getId());
        verify(userRepository, times(1)).findByEmailIgnoreCase(updatedUser.getEmail());
        verify(userRepository, times(1)).save(expectedUser);
        assertEquals(expectedUser, result);
    }

    @Test
    public void updateUser_updateEmail_Null() {
        User updatedUser = User.builder()
                .id(1L)
                .email(null)
                .build();

        User savedUser = User.builder()
                .id(updatedUser.getId())
                .name("name")
                .email("email@email.ru")
                .build();

        User expectedUser = User.builder()
                .id(updatedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .build();

        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        User result = userService.update(updatedUser);
        verify(userRepository, times(1)).findById(updatedUser.getId());
        verify(userRepository, times(0)).findByEmailIgnoreCase(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(expectedUser, result);
    }

    @Test
    public void updateUser_updateEmail_emailExists() {
        User updatedUser = User.builder()
                .id(1L)
                .email("otherEmail@email.ru")
                .build();

        User savedUser = User.builder()
                .id(updatedUser.getId())
                .name("name")
                .email("email@email.ru")
                .build();

        User otherSavedUser = User.builder()
                .name("name")
                .email(updatedUser.getEmail())
                .build();

        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.of(savedUser));
        when(userRepository.findByEmailIgnoreCase(updatedUser.getEmail())).thenReturn(otherSavedUser);

        Throwable e = assertThrows(EmailRegisteredException.class, () -> userService.update(updatedUser));
        verify(userRepository, times(1)).findById(updatedUser.getId());
        verify(userRepository, times(1)).findByEmailIgnoreCase(anyString());
        verify(userRepository, times(0)).save(any(User.class));
        assertEquals("User with such email already registered", e.getMessage());
    }

    @Test
    public void updateUser_updateEmail_sameExists() {
        User updatedUser = User.builder()
                .id(1L)
                .email("email@email.ru")
                .build();

        User savedUser = User.builder()
                .id(updatedUser.getId())
                .name("name")
                .email(updatedUser.getEmail())
                .build();

        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.of(savedUser));
        when(userRepository.save(savedUser)).thenReturn(savedUser);

        User result = userService.update(updatedUser);
        verify(userRepository, times(1)).findById(updatedUser.getId());
        verify(userRepository, times(0)).findByEmailIgnoreCase(anyString());
        verify(userRepository, times(1)).save(any(User.class));

        assertEquals(savedUser, result);
    }

    @Test
    public void getUser_Normal() {
        Long userId = 1L;

        User expectedUser = User.builder()
                .id(userId)
                .name("name")
                .email("email@email.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User savedUser = userService.get(userId);

        verify(userRepository, times(1)).findById(userId);
        assertEquals(expectedUser, savedUser);
    }

    @Test
    public void getUser_NoSuchUser() {
        Long userId = 9999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Throwable e = assertThrows(UserNotFoundException.class, () -> userService.get(userId));

        verify(userRepository, times(1)).findById(userId);
        assertEquals(String.format("User id %s not found", userId), e.getMessage());
    }

    @Test
    public void findAll_Normal() {
        List<User> expectedUsers = new ArrayList<>();
        User expectedUser = User.builder()
                .id(1L)
                .name("name")
                .email("email@email.ru")
                .build();
        expectedUsers.add(expectedUser);

        when(userRepository.findAll()).thenReturn(expectedUsers);

        List<User> savedUsers = userService.findAll();

        verify(userRepository, times(1)).findAll();
        assertEquals(expectedUsers, savedUsers);
        assertEquals(expectedUser, expectedUsers.get(0));
    }

    @Test
    public void findAll_Empty() {
        List<User> savedUsers = userService.findAll();

        verify(userRepository, times(1)).findAll();
        assertTrue(savedUsers.isEmpty());
    }

    @Test
    public void delete_Normal() {
        User userToDelete = User.builder()
                .id(1L)
                .name("name")
                .email("email@email.ru")
                .build();

        when(userRepository.findById(userToDelete.getId())).thenReturn(Optional.of(userToDelete));
        User deletedUser = userService.delete(userToDelete.getId());

        assertEquals(userToDelete, deletedUser);
    }

    @Test
    public void delete_NoSuchUser() {
        Long userToDeleteId = 1L;

        when(userRepository.findById(userToDeleteId)).thenReturn(Optional.empty());
        Throwable e = assertThrows(UserNotFoundException.class, () -> userService.delete(userToDeleteId));

        assertEquals(String.format("User id %s not found", userToDeleteId), e.getMessage());
    }
}
