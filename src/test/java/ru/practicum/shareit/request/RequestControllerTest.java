package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.handler.ErrorHandler;
import ru.practicum.shareit.request.controller.RequestController;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest
@ContextConfiguration(classes = {RequestController.class, ErrorHandler.class})
public class RequestControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;
    private RequestDto requestToSaveDto;
    private RequestDto savedRequestDto;

    private User requester;
    private LocalDateTime now = LocalDateTime.now();
    private Long userId = 1L;
    private Long wrongUserId = -9999L;
    private int from = 0;
    private int size = 10;
    private List<RequestDto> requests;

    @BeforeEach
    public void beforeEach() {
        requestToSaveDto = RequestDto.builder()
                .description("description")
                .build();

        requester = User.builder()
                .id(userId)
                .name("author name")
                .build();

        savedRequestDto = RequestDto.builder()
                .id(1L)
                .description(requestToSaveDto.getDescription())
                .requesterId(requester.getId())
                .created(now)
                .build();

        requests = new ArrayList<>();
        requests.add(savedRequestDto);
    }

    @SneakyThrows
    @Test
    public void addRequest_Normal() {
        when(requestService.addRequest(any(RequestDto.class))).thenReturn(savedRequestDto);

        String result = mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestToSaveDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(savedRequestDto), result);
    }

    @SneakyThrows
    @Test
    public void addRequest_BlankDescription() {
        requestToSaveDto.setDescription("");

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestToSaveDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        requestToSaveDto.setDescription("   ");

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestToSaveDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).addRequest(any(RequestDto.class));
    }

    @SneakyThrows
    @Test
    public void addRequest_WrongUserId() {
        String result = mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestToSaveDto))
                        .header("X-Sharer-User-Id", wrongUserId))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals("{\"error\":\"addRequest.requesterId: User id should be more than 0\"}", result);
        verify(requestService, never()).addRequest(any(RequestDto.class));
    }

    @SneakyThrows
    @Test
    public void getRequest_Normal() {
        when(requestService.findRequest(savedRequestDto.getId(), userId)).thenReturn(savedRequestDto);

        String result = mockMvc.perform(get("/requests/{requestId}", savedRequestDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(savedRequestDto), result);
    }

    @SneakyThrows
    @Test
    public void getOwnRequests_Normal() {
        List<RequestDto> requests = new ArrayList<>();
        requests.add(savedRequestDto);

        when(requestService.findUserRequest(userId, from, size)).thenReturn(requests);

        String result = mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(requests), result);
    }

    @SneakyThrows
    @Test
    public void getOwnRequests_Empty() {
        String result = mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals("[]", result);
    }

    @SneakyThrows
    @Test
    public void getAllRequests_Normal() {
        when(requestService.findAllRequests(userId, from, size)).thenReturn(requests);

        String result = mockMvc.perform(get("/requests/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(requests), result);
    }
}
