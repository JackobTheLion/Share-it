package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest
@ContextConfiguration(classes = UserController.class)
public class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User userToSave;

    @Test
    public void addUser_Normal() throws Exception {
        userToSave = User.builder()
                .name("name")
                .email("email@email.ru")
                .build();

        when(userService.add(userToSave)).thenReturn(userToSave);

        String result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToSave)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService, times(1)).add(userToSave);
        assertEquals(objectMapper.writeValueAsString(userToSave), result);
    }

    @Test
    public void addUser_WrongEmail() throws Exception {
        userToSave = User.builder()
                .name("name")
                .email("email")
                .build();

        when(userService.add(userToSave)).thenReturn(userToSave);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToSave)))
                .andExpect(status().isBadRequest());

        verify(userService, times(0)).add(any(User.class));

        userToSave.setEmail("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToSave)))
                .andExpect(status().isBadRequest());

        verify(userService, times(0)).add(any(User.class));

        userToSave.setEmail(" ");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToSave)))
                .andExpect(status().isBadRequest());

        verify(userService, times(0)).add(any(User.class));
    }

    @Test
    public void addUser_WrongName() throws Exception {
        userToSave = User.builder()
                .name("")
                .email("email@email.com")
                .build();

        when(userService.add(userToSave)).thenReturn(userToSave);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToSave)))
                .andExpect(status().isBadRequest());

        verify(userService, times(0)).add(any(User.class));

        userToSave.setEmail(" ");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToSave)))
                .andExpect(status().isBadRequest());

        verify(userService, times(0)).add(any(User.class));
    }

    @SneakyThrows
    @Test
    public void updateUser_emailUpdate_Normal() {
        UserDto updatedUserDto = UserDto.builder()
                .email("newemail@email.com")
                .build();
        Long userId = 1L;

        User userToReturn = User.builder()
                .id(userId)
                .name("name")
                .email(updatedUserDto.getEmail())
                .build();

        when(userService.update(any(User.class))).thenReturn(userToReturn);

        String result = mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService, times(1)).update(any(User.class));
        assertEquals(objectMapper.writeValueAsString(userToReturn), result);
    }

    @SneakyThrows
    @Test
    public void updateUser_nameUpdate_Normal() {
        UserDto updatedUserDto = UserDto.builder()
                .name("new name")
                .build();
        Long userId = 1L;

        User userToReturn = User.builder()
                .id(userId)
                .name(updatedUserDto.getName())
                .email("newemail@email.com")
                .build();

        when(userService.update(any(User.class))).thenReturn(userToReturn);

        String result = mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService, times(1)).update(any(User.class));
        assertEquals(objectMapper.writeValueAsString(userToReturn), result);
    }

    @SneakyThrows
    @Test
    public void updateUser_emailUpdate_Null() {
        Long userId = 1L;

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(null)))
                .andExpect(status().isBadRequest());

        verify(userService, times(0)).update(any(User.class));
    }

    @SneakyThrows
    @Test
    public void getUser_Normal() {
        Long userId = 1L;

        User userToReturn = User.builder()
                .id(userId)
                .name("name")
                .email("email@email.com")
                .build();

        when(userService.get(userId)).thenReturn(userToReturn);

        String result = mockMvc.perform(get("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService, times(1)).get(userId);
        assertEquals(objectMapper.writeValueAsString(userToReturn), result);
    }

    @SneakyThrows
    @Test
    public void getAllUser_Normal() {
        User userToReturn = User.builder()
                .id(1L)
                .name("name")
                .email("email@email.com")
                .build();

        List<User> users = new ArrayList<>();
        users.add(userToReturn);

        when(userService.findAll()).thenReturn(users);

        String result = mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService, times(1)).findAll();
        assertEquals(objectMapper.writeValueAsString(users), result);
    }

    @SneakyThrows
    @Test
    public void getAllUser_Empty() {
        List<User> users = new ArrayList<>();

        when(userService.findAll()).thenReturn(users);

        String result = mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService, times(1)).findAll();
        assertEquals(objectMapper.writeValueAsString(users), result);
    }

    @SneakyThrows
    @Test
    public void deleteUser_Normal() {
        mockMvc.perform(delete("/users/{userId}", anyLong())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService, times(1)).delete(anyLong());
    }
}