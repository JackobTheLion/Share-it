package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.handler.ErrorHandler;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserRequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
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
    private BookingService bookingService;
    private BookingRequestDto bookingToSave;
    private BookingRequestDto savedBookingRequestDto;
    private List<BookingRequestDto> bookings;
    private ItemDto itemDto;
    private Long itemId = 1L;
    private UserRequestDto userRequestDto;
    private Long userId = 1L;
    private Integer from = 0;
    private Integer size = 10;
    private LocalDateTime start = LocalDateTime.now().plusHours(1);
    private LocalDateTime end = start.plusHours(1);

    @BeforeEach
    public void beforeEach() {
        userRequestDto = UserRequestDto.builder()
                .id(userId)
                .build();

        itemDto = ItemDto.builder()
                .id(itemId)
                .build();

        bookingToSave = BookingRequestDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        savedBookingRequestDto = BookingRequestDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(itemDto)
                .booker(userRequestDto)
                .status(Status.WAITING)
                .build();

        bookings = new ArrayList<>();
        bookings.add(savedBookingRequestDto);
    }

    @SneakyThrows
    @Test
    public void addBooking_Normal() {
        when(bookingService.createBooking(any(BookingRequestDto.class), anyLong())).thenReturn(savedBookingRequestDto);

        String result = mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingToSave))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(savedBookingRequestDto), result);
    }

    @SneakyThrows
    @Test
    public void getBooking_Normal() {
        when(bookingService.findBooking(savedBookingRequestDto.getId(), userId)).thenReturn(savedBookingRequestDto);

        String result = mockMvc.perform(get("/bookings/{bookingId}", savedBookingRequestDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(savedBookingRequestDto), result);
    }

    @SneakyThrows
    @Test
    public void getUserBookings_Normal() {
        when(bookingService.getUserBookings(userId, "ALL", from, size)).thenReturn(bookings);

        String result = mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("state", "ALL")
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookings), result);
    }

    @SneakyThrows
    @Test
    public void getOwnerBooking_Normal() {
        when(bookingService.getOwnerBooking(userId, "ALL", from, size)).thenReturn(bookings);

        String result = mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("state", "ALL")
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookings), result);
    }

    @SneakyThrows
    @Test
    public void updateBooking_Normal() {
        BookingRequestDto updatedBooking = savedBookingRequestDto;
        updatedBooking.setStatus(Status.APPROVED);
        when(bookingService.approveBooking(userId, true, savedBookingRequestDto.getId())).thenReturn(updatedBooking);

        String result = mockMvc.perform(patch("/bookings/{bookingId}", savedBookingRequestDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(updatedBooking), result);
    }

}
