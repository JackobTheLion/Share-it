package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader("X-Sharer-User-Id") @Min(value = 1,
            message = "User ID must be more than 0") Long userId,
                                             @RequestBody @Validated BookingRequestDto bookingRequestDto) {
        log.info("Creating booking {}, userId={}", bookingRequestDto, userId);
        ResponseEntity<Object> response = bookingClient.bookItem(userId, bookingRequestDto);
        log.info("Response: {}", response);
        return response;
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") @Min(value = 1,
            message = "User ID must be more than 0") Long userId,
                                             @PathVariable @Min(value = 0,
                                                     message = "Booking ID must be more than 0") Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        ResponseEntity<Object> response = bookingClient.getBooking(userId, bookingId);
        log.info("Response: {}", response);
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") @Min(value = 0,
            message = "User ID must be more than 0") Long userId,
                                                  @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                  @Positive @RequestParam(defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        ResponseEntity<Object> response = bookingClient.getUserBookings(userId, state, from, size);
        log.info("Response: {}", response);
        return response;
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBooking(@RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
            message = "User id should be more than 0") Long ownerId,
                                                  @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                  @RequestParam(defaultValue = "0") @Min(value = 0,
                                                          message = "Parameter 'from' must be more than 0") int from,
                                                  @RequestParam(defaultValue = "10") @Min(value = 0,
                                                          message = "Parameter 'size' must be more than 0") int size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Looking for bookings of owner {} with state {}", ownerId, stateParam);
        ResponseEntity<Object> response = bookingClient.getOwnerBookings(ownerId, state, from, size);
        log.info("Response: {}", response);
        return response;
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateBooking(@PathVariable @Min(value = 1,
            message = "Booking id should be more than 0") Long bookingId,
                                                @RequestParam Boolean approved,
                                                @RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
                                                        message = "User id should be more than 0") Long ownerId) {
        log.info("Updating booking id {} as '{}' by user {}", bookingId, approved, ownerId);
        ResponseEntity<Object> response = bookingClient.updateBooking(ownerId, approved, bookingId);
        log.info("Response: {}", response);
        return response;
    }
}
