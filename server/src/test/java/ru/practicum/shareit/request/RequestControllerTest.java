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
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
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
    private ItemRequestRequestDto requestToSaveDto;
    private ItemRequestResponseDto savedItemRequestRequestDto;

    private User requester;
    private LocalDateTime now = LocalDateTime.now();
    private Long userId = 1L;
    private Long wrongUserId = -9999L;
    private Integer from = 0;
    private Integer size = 10;
    private List<ItemRequestResponseDto> requests;

    @BeforeEach
    public void beforeEach() {
        requestToSaveDto = ItemRequestRequestDto.builder()
                .description("description")
                .build();

        requester = User.builder()
                .id(userId)
                .name("author name")
                .build();

        savedItemRequestRequestDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description(requestToSaveDto.getDescription())
                .created(now)
                .build();

        requests = new ArrayList<>();
        requests.add(savedItemRequestRequestDto);
    }

    @SneakyThrows
    @Test
    public void addRequest_Normal() {
        when(requestService.addRequest(any(ItemRequestRequestDto.class))).thenReturn(savedItemRequestRequestDto);

        String result = mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestToSaveDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(savedItemRequestRequestDto), result);
    }

    @SneakyThrows
    @Test
    public void getRequest_Normal() {
        when(requestService.findRequest(savedItemRequestRequestDto.getId(), userId)).thenReturn(savedItemRequestRequestDto);

        String result = mockMvc.perform(get("/requests/{requestId}", savedItemRequestRequestDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(savedItemRequestRequestDto), result);
    }

    @SneakyThrows
    @Test
    public void getOwnRequests_Normal() {
        List<ItemRequestResponseDto> requests = new ArrayList<>();
        requests.add(savedItemRequestRequestDto);

        when(requestService.findUserRequest(userId, from, size)).thenReturn(requests);

        String result = mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("from", from.toString())
                        .param("size", size.toString())
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
                        .param("from", from.toString())
                        .param("size", size.toString())
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
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(requests), result);
    }
}
