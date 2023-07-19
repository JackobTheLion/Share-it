package booking;

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
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.handler.ErrorHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {BookingController.class, ErrorHandler.class})
public class BookingControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    private BookingRequestDto bookingRequestDto;
    private Long userId = 1L;
    private Long bookingId = 1L;
    private Integer from = 0;
    private Integer size = 10;
    private String state = "all";

    @BeforeEach
    public void init() {
        bookingRequestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();
    }

    @SneakyThrows
    @Test
    public void addBooking_Normal() {
        when(bookingClient.bookItem(anyLong(), any(BookingRequestDto.class))).thenReturn(
                new ResponseEntity<>(new BookingRequestDto(), HttpStatus.OK));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void addBooking_wrongUserId() {
        Long wrongUserId = -999L;

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto))
                        .header("X-Sharer-User-Id", wrongUserId))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    public void addBooking_EmptyStartDate() {
        bookingRequestDto.setStart(null);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    public void addBooking_EmptyEndDate() {
        bookingRequestDto.setEnd(null);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    public void addBooking_WrongItemId() {
        bookingRequestDto.setItemId(null);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        bookingRequestDto.setItemId(-999L);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    public void getBooking_Normal() {
        when(bookingClient.getBooking(anyLong(), anyLong())).thenReturn(
                new ResponseEntity<>(new BookingRequestDto(), HttpStatus.OK));

        mockMvc.perform(get("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void getBooking_WrongUserId() {
        Long wrongUserId = -999L;

        mockMvc.perform(get("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", wrongUserId))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getBooking(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    public void getBooking_WrongBookingId() {
        Long wrongBookingId = -999L;

        mockMvc.perform(get("/bookings/" + wrongBookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getBooking(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    public void getUserBooking_Normal() {
        when(bookingClient.getBooking(anyLong(), anyLong())).thenReturn(
                new ResponseEntity<>(List.of(bookingRequestDto), HttpStatus.OK));

        mockMvc.perform(get("/bookings")
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void getUserBooking_StateIsNullOrEmpty() {
        when(bookingClient.getBooking(anyLong(), anyLong())).thenReturn(
                new ResponseEntity<>(List.of(bookingRequestDto), HttpStatus.OK));
        String stateNull = null;

        mockMvc.perform(get("/bookings")
                        .param("state", stateNull)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        String stateEmpty = "";

        mockMvc.perform(get("/bookings")
                        .param("state", stateEmpty)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void getUserBooking_WrongFromOrSize() {
        Integer wrongFrom = -999;

        mockMvc.perform(get("/bookings")
                        .param("state", state)
                        .param("from", wrongFrom.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        Integer wrongSize = -999;

        mockMvc.perform(get("/bookings")
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", wrongSize.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getUserBookings(anyLong(), any(BookingState.class), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    public void getUserBooking_WrongUserId() {
        Long wrongUserId = -999L;

        mockMvc.perform(get("/bookings")
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", wrongUserId))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getUserBookings(anyLong(), any(BookingState.class), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    public void getOwnerBooking_Normal() {
        when(bookingClient.getBooking(anyLong(), anyLong())).thenReturn(
                new ResponseEntity<>(List.of(bookingRequestDto), HttpStatus.OK));

        mockMvc.perform(get("/bookings/owner")
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void getOwnerBooking_StateIsNullOrEmpty() {
        when(bookingClient.getBooking(anyLong(), anyLong())).thenReturn(
                new ResponseEntity<>(List.of(bookingRequestDto), HttpStatus.OK));
        String stateNull = null;

        mockMvc.perform(get("/bookings/owner")
                        .param("state", stateNull)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        String stateEmpty = "";

        mockMvc.perform(get("/bookings")
                        .param("state", stateEmpty)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void getOwnerBooking_WrongFromOrSize() {
        Integer wrongFrom = -999;

        mockMvc.perform(get("/bookings/owner")
                        .param("state", state)
                        .param("from", wrongFrom.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        Integer wrongSize = -999;

        mockMvc.perform(get("/bookings")
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", wrongSize.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getUserBookings(anyLong(), any(BookingState.class), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    public void getOwnerBooking_WrongUserId() {
        Long wrongUserId = -999L;

        mockMvc.perform(get("/bookings/owner")
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", wrongUserId))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getUserBookings(anyLong(), any(BookingState.class), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    public void updateBooking_Normal() {
        when(bookingClient.updateBooking(anyLong(), anyBoolean(), anyLong())).thenReturn(
                new ResponseEntity<>(bookingRequestDto, HttpStatus.OK));

        mockMvc.perform(patch("/bookings/" + bookingId)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void updateBooking_WrongUserId() {
        Long wrongUserId = -999L;

        mockMvc.perform(patch("/bookings/" + bookingId)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", wrongUserId))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).updateBooking(anyLong(), anyBoolean(), anyLong());
    }

    @SneakyThrows
    @Test
    public void updateBooking_WrongBookingId() {
        Long wrongBookingId = -999L;

        mockMvc.perform(patch("/bookings/" + wrongBookingId)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).updateBooking(anyLong(), anyBoolean(), anyLong());
    }

    @SneakyThrows
    @Test
    public void updateBooking_WrongApproved() {
        mockMvc.perform(patch("/bookings/" + bookingId)
                        .param("approved", "WRONG PARA")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).updateBooking(anyLong(), anyBoolean(), anyLong());
    }


}
