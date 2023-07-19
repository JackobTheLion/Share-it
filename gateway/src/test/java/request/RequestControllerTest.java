package request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.handler.ErrorHandler;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.request.RequestClient;
import ru.practicum.shareit.request.RequestController;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private RequestClient requestClient;

    private ItemRequestRequestDto itemRequestRequestDto;
    private Long userId = 1L;
    private Long requestId = 1L;
    private Integer from = 0;
    private Integer size = 10;

    @BeforeEach
    public void init() {
        itemRequestRequestDto = ItemRequestRequestDto.builder()
                .description("description")
                .build();
    }

    @SneakyThrows
    @Test
    public void addRequest_Normal() {
        when(requestClient.addRequest(userId, itemRequestRequestDto)).thenReturn(
                new ResponseEntity<>(new ItemRequestDto(), HttpStatus.OK));

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestRequestDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void addRequest_WrongUserId() {
        Long wrongUserId = -999L;

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestRequestDto))
                        .header("X-Sharer-User-Id", wrongUserId))
                .andExpect(status().isBadRequest());

        verify(requestClient, never()).addRequest(anyLong(), any());
    }

    @SneakyThrows
    @Test
    public void addRequest_EmptyDescription() {
        itemRequestRequestDto.setDescription("");

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestRequestDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        itemRequestRequestDto.setDescription("   ");

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestRequestDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(requestClient, never()).addRequest(anyLong(), any());
    }

    @SneakyThrows
    @Test
    public void findRequest_Normal() {
        when(requestClient.findRequest(anyLong(), anyLong())).thenReturn(
                new ResponseEntity<>(new ItemRequestDto(), HttpStatus.OK));

        mockMvc.perform(get("/requests/" + requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void findRequest_WrongUserId() {
        Long wrongUserId = -999L;

        mockMvc.perform(get("/requests/" + requestId)
                        .header("X-Sharer-User-Id", wrongUserId))
                .andExpect(status().isBadRequest());

        verify(requestClient, never()).findRequest(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    public void findRequest_WrongRequestId() {
        Long wrongRequestId = -999L;

        mockMvc.perform(get("/requests/" + wrongRequestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(requestClient, never()).findRequest(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    public void getOwnRequests_Normal() {
        when(requestClient.getOwnRequests(anyLong(), anyInt(), anyInt())).thenReturn(
                new ResponseEntity<>(List.of(itemRequestRequestDto), HttpStatus.OK));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void getOwnRequests_WrongUserId() {
        Long wrongUserId = -999L;

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", wrongUserId)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isBadRequest());

        verify(requestClient, never()).getOwnRequests(anyLong(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    public void getOwnRequests_EmptyFrom() {
        when(requestClient.getOwnRequests(anyLong(), anyInt(), anyInt())).thenReturn(
                new ResponseEntity<>(List.of(itemRequestRequestDto), HttpStatus.OK));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "")
                        .param("size", size.toString()))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void getOwnRequests_EmptySize() {
        when(requestClient.getOwnRequests(anyLong(), anyInt(), anyInt())).thenReturn(
                new ResponseEntity<>(List.of(itemRequestRequestDto), HttpStatus.OK));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", ""))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void getOwnRequests_WrongFromAndSize() {
        Long wrongFrom = -999L;

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", wrongFrom.toString())
                        .param("size", size.toString()))
                .andExpect(status().isBadRequest());

        Long wrongSize = -999L;

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", wrongSize.toString()))
                .andExpect(status().isBadRequest());

        verify(requestClient, never()).getOwnRequests(anyLong(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    public void getRequests_Normal() {
        when(requestClient.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(
                new ResponseEntity<>(List.of(itemRequestRequestDto), HttpStatus.OK));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void getRequests_WrongUserId() {
        Long wrongUserId = -999L;

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", wrongUserId)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isBadRequest());

        verify(requestClient, never()).getAllRequests(anyLong(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    public void getRequests_EmptyFrom() {
        when(requestClient.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(
                new ResponseEntity<>(List.of(itemRequestRequestDto), HttpStatus.OK));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "")
                        .param("size", size.toString()))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void getRequests_EmptySize() {
        when(requestClient.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(
                new ResponseEntity<>(List.of(itemRequestRequestDto), HttpStatus.OK));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", ""))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void getRequests_WrongFromAndSize() {
        Long wrongFrom = -999L;

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", wrongFrom.toString())
                        .param("size", size.toString()))
                .andExpect(status().isBadRequest());

        Long wrongSize = -999L;

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", wrongSize.toString()))
                .andExpect(status().isBadRequest());

        verify(requestClient, never()).getAllRequests(anyLong(), anyInt(), anyInt());
    }

}
