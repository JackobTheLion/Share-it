package ru.practicum.shareit.booking.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponseDto createBooking(@RequestBody @Validated BookingRequestDto bookingRequestDto,
                                            @RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
                                            message = "User id should be more than 0") Long bookerId) {

        log.info("Adding booking: {} by user {}", bookingRequestDto, bookerId);
        BookingResponseDto savedBookingRequestDto = bookingService.createBooking(bookingRequestDto, bookerId);
        log.info("Booking added: {}", savedBookingRequestDto);
        return savedBookingRequestDto;
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBooking(@PathVariable Long bookingId,
                                        @RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
                                         message = "User id should be more than 0") Long bookerId) {

        log.info("Looking for booking id {} by user id {}", bookingId, bookerId);
        BookingResponseDto bookingRequestDto = bookingService.findBooking(bookingId, bookerId);
        log.info("Booking found: {}", bookingRequestDto);
        return bookingRequestDto;
    }

    @GetMapping
    public List<BookingResponseDto> getUserBookings(@RequestParam(required = false, defaultValue = "ALL") String state,
                                                   @RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
                                                    message = "User id should be more than 0") Long bookerId,
                                                   @RequestParam(defaultValue = "0") @Min(value = 0,
                                                    message = "Parameter 'from' must be more than 0") int from,
                                                   @RequestParam(defaultValue = "10") @Min(value = 0,
                                                    message = "Parameter 'size' must be more than 0") int size) {

        log.info("Looking for bookings of user {} with state {}", bookerId, state);
        List<BookingResponseDto> bookings = bookingService.getUserBookings(bookerId, state, from, size);
        log.info("Bookings found: {}.", bookings);
        return bookings;
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBooking(@RequestParam(required = false, defaultValue = "ALL") String state,
                                                   @RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
                                                    message = "User id should be more than 0") Long bookerId,
                                                   @RequestParam(defaultValue = "0") @Min(value = 0,
                                                    message = "Parameter 'from' must be more than 0") int from,
                                                   @RequestParam(defaultValue = "10") @Min(value = 0,
                                                    message = "Parameter 'size' must be more than 0") int size) {

        log.info("Looking for bookings of owner {} with state {}", bookerId, state);
        List<BookingResponseDto> bookings = bookingService.getOwnerBooking(bookerId, state, from, size);
        log.info("Bookings found: {}.", bookings);
        return bookings;
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateBooking(@PathVariable Long bookingId,
                                           @RequestParam Boolean approved,
                                           @RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
                                            message = "User id should be more than 0") Long bookerId) {

        log.info("Updating booking id {} as {} by user id {}", bookingId, approved, bookerId);
        BookingResponseDto updatedBooking = bookingService.approveBooking(bookerId, approved, bookingId);
        log.info("Booking updated: {}", updatedBooking);
        return updatedBooking;
    }
}
