package user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserRequestDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = UserController.class)
public class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserClient userClient;

    private UserRequestDto userToSave;

    @Test
    public void addUser_WrongEmail() throws Exception {
        userToSave = UserRequestDto.builder()
                .name("name")
                .email("email")
                .build();

        when(userClient.addUser(userToSave)).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToSave)))
                .andExpect(status().isBadRequest());

        verify(userClient, times(0)).addUser(any(UserRequestDto.class));

        userToSave.setEmail("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToSave)))
                .andExpect(status().isBadRequest());

        verify(userClient, times(0)).addUser(any(UserRequestDto.class));

        userToSave.setEmail(" ");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToSave)))
                .andExpect(status().isBadRequest());

        verify(userClient, times(0)).addUser(any(UserRequestDto.class));
    }

    @Test
    public void addUser_WrongName() throws Exception {
        userToSave = UserRequestDto.builder()
                .name("")
                .email("email@email.com")
                .build();

        when(userClient.addUser(userToSave)).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToSave)))
                .andExpect(status().isBadRequest());

        verify(userClient, times(0)).addUser(any(UserRequestDto.class));

        userToSave.setEmail(" ");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToSave)))
                .andExpect(status().isBadRequest());

        verify(userClient, times(0)).addUser(any(UserRequestDto.class));
    }
}
