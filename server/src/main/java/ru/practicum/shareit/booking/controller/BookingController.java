package ru.practicum.shareit.booking.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponseDto bookItem(@RequestBody BookingRequestDto bookingRequestDto,
                                       @RequestHeader(value = "X-Sharer-User-Id") Long bookerId) {

        log.info("Adding booking: {} by user {}", bookingRequestDto, bookerId);
        BookingResponseDto savedBookingRequestDto = bookingService.createBooking(bookingRequestDto, bookerId);
        log.info("Booking added: {}", savedBookingRequestDto);
        return savedBookingRequestDto;
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBooking(@PathVariable Long bookingId,
                                         @RequestHeader(value = "X-Sharer-User-Id") Long bookerId) {

        log.info("Looking for booking id {} by user id {}", bookingId, bookerId);
        BookingResponseDto bookingRequestDto = bookingService.findBooking(bookingId, bookerId);
        log.info("Booking found: {}", bookingRequestDto);
        return bookingRequestDto;
    }

    @GetMapping
    public List<BookingResponseDto> getUserBookings(@RequestParam String state,
                                                    @RequestHeader(value = "X-Sharer-User-Id") Long bookerId,
                                                    @RequestParam Integer from,
                                                    @RequestParam Integer size) {

        log.info("Looking for bookings of user {} with state {}", bookerId, state);
        List<BookingResponseDto> bookings = bookingService.getUserBookings(bookerId, state, from, size);
        log.info("Bookings found: {}.", bookings);
        return bookings;
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBooking(@RequestParam String state,
                                                    @RequestHeader(value = "X-Sharer-User-Id") Long bookerId,
                                                    @RequestParam int from,
                                                    @RequestParam int size) {

        log.info("Looking for bookings of owner {} with state {}", bookerId, state);
        List<BookingResponseDto> bookings = bookingService.getOwnerBooking(bookerId, state, from, size);
        log.info("Bookings found: {}.", bookings);
        return bookings;
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateBooking(@PathVariable Long bookingId,
                                            @RequestParam Boolean approved,
                                            @RequestHeader(value = "X-Sharer-User-Id") Long ownerId) {

        log.info("Updating booking id {} as {} by user id {}", bookingId, approved, ownerId);
        BookingResponseDto updatedBooking = bookingService.approveBooking(ownerId, approved, bookingId);
        log.info("Booking updated: {}", updatedBooking);
        return updatedBooking;
    }
}
