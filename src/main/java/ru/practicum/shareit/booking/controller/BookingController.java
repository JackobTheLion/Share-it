package ru.practicum.shareit.booking.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingMapper.mapFromDto;
import static ru.practicum.shareit.booking.BookingMapper.mapToDto;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    private BookingDto createBooking(@RequestBody @Validated BookingDto bookingDto,
                                     @RequestHeader(value = "X-Sharer-User-Id") Long bookerId) {

        isIdValid(bookerId);
        log.info("Adding booking: {} by user {}", bookingDto, bookerId);
        Booking booking = bookingService.createBooking(mapFromDto(bookingDto, bookerId));
        log.info("Booking added: {}", booking);
        return mapToDto(booking);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable Long bookingId,
                                 @RequestHeader(value = "X-Sharer-User-Id") Long bookerId) {

        isIdValid(bookerId);
        log.info("Looking for booking id {} by user id {}", bookingId, bookerId);
        Booking booking = bookingService.findBooking(bookingId, bookerId);
        log.info("Booking found: {}", booking);
        return mapToDto(booking);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestParam(required = false, defaultValue = "ALL") String state,
                                            @RequestHeader(value = "X-Sharer-User-Id") Long bookerId) {


        isIdValid(bookerId);
        log.info("Looking for bookings of user {} with state {}", bookerId, state);
        List<Booking> bookings = bookingService.getUserBookings(bookerId, state);
        log.info("Bookings found: {}.", bookings.size());
        return bookings.stream().map(BookingMapper::mapToDto).collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBooking(@RequestParam(required = false, defaultValue = "ALL") String state,
                                            @RequestHeader(value = "X-Sharer-User-Id") Long bookerId) {

        isIdValid(bookerId);
        log.info("Looking for bookings of owner {} with state {}", bookerId, state);
        List<Booking> bookings = bookingService.getOwnerBooking(bookerId, state);
        log.info("Bookings found: {}.", bookings.size());
        return bookings.stream().map(BookingMapper::mapToDto).collect(Collectors.toList());
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateBooking(@PathVariable Long bookingId,
                                    @RequestParam Boolean approved,
                                    @RequestHeader(value = "X-Sharer-User-Id") Long bookerId) {

        isIdValid(bookerId);
        log.info("Updating booking id {} as {} by user id {}", bookingId, approved, bookerId);
        Booking booking = bookingService.approveBooking(bookerId, approved, bookingId);
        return mapToDto(booking);
    }

    private void isIdValid(Long bookerId) {
        if (bookerId <= 0) {
            log.info("User id is invalid");
            throw new ValidationException("User id should be more than 0");
        }
    }
}
