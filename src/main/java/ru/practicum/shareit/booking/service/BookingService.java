package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.exceptions.BookingNotAloudException;
import ru.practicum.shareit.booking.exceptions.BookingNotFoundException;
import ru.practicum.shareit.booking.exceptions.ItemNotAvailableException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.model.Status.*;

@Service
@Slf4j
public class BookingService {
    private final BookingStorage bookingStorage;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Autowired
    public BookingService(BookingStorage bookingStorage, UserStorage userStorage, ItemStorage itemStorage) {
        this.bookingStorage = bookingStorage;
        this.userStorage = userStorage;
        this.itemStorage = itemStorage;
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        log.info("Adding booking: {}.", booking);

        if (booking.getEndDate().before(booking.getStartDate()) || booking.getStartDate().equals(booking.getEndDate())) {
            log.info("Booking start date should be before booking end date");
            throw new ValidationException("Booking start date should be before booking end date");
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        if (booking.getEndDate().before(now) || booking.getStartDate().before(now)) {
            log.info("Booking cannot start or end in past");
            throw new ValidationException("Booking cannot start or end in past");
        }

        Item item = itemStorage.findById(booking.getItem().getId()).orElseThrow(() -> {
            log.info("Item id {} not found", booking.getItem().getId());
            return new ItemNotFoundException(String.format("Item id %s not found", booking.getItem().getId()));
        });

        if (!item.getIsAvailable()) {
            log.info("Item id {} not available", booking.getItem().getId());
            throw new ItemNotAvailableException(String.format("Item id %s not available", booking.getItem().getId()));
        }

        if (item.getOwnerId().equals(booking.getBooker().getId())) {
            log.info("Booking own item is not aloud.");
            throw new BookingNotAloudException("Booking own item is not aloud.");
        }

        User user = getUser(booking.getBooker().getId());

        if (!isAvailableToBook(booking)) {
            throw new ItemNotAvailableException("Item is already booked for this period.");
        }

        booking.setStatus(WAITING);
        booking.setItem(item);
        booking.setBooker(user);
        bookingStorage.save(booking);
        log.info("Booking saved: {}", booking);
        return booking;
    }

    public Booking findBooking(Long bookingId, Long userId) {
        log.info("Looking for booking id {} by user id {}", bookingId, userId);
        getUser(userId);
        Booking booking = bookingStorage.findById(bookingId).orElseThrow(() -> {
            log.info("Booking id {} not found.", bookingId);
            return new BookingNotFoundException(String.format("Booking id %s not found.", bookingId));
        });
        log.info("Booking found: {}.", booking);

        if (!(booking.getItem().getOwnerId().equals(userId) || booking.getBooker().getId().equals(userId))) {
            log.info("User id {} has no access to booking id {}", userId, booking);
            throw new BookingNotFoundException(String.format("Booking id %s not found.", bookingId));
        }
        return booking;
    }

    public List<Booking> getUserBookings(Long userId, String state) {
        log.info("Looking for bookings of user {} with status {}", userId, state);
        getUser(userId);
        List<Booking> bookings;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        log.info("Now is: {}.", now);
        switch (state) {
            case "ALL":
                bookings = bookingStorage.findByBookerIdOrderByStartDateDesc(userId);
                log.info("Number of bookings: {}", bookings.size());
                return bookings;
            case "CURRENT":
                bookings = bookingStorage.findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(userId, now, now);
                log.info("Number of current bookings: {}", bookings.size());
                return bookings;
            case "PAST":
                bookings = bookingStorage.findByBookerIdAndEndDateBeforeOrderByStartDateDesc(userId, now);
                log.info("Number of past bookings: {}", bookings.size());
                return bookings;
            case "FUTURE":
                bookings = bookingStorage.findByBookerIdAndStartDateAfterOrderByStartDateDesc(userId, now);
                log.info("Number of future bookings: {}", bookings.size());
                return bookings;
            case "WAITING":
                bookings = bookingStorage.findByBookerIdAndStatusEqualsOrderByStartDateDesc(userId, WAITING);
                log.info("Number of waiting bookings: {}", bookings.size());
                return bookings;
            case "REJECTED":
                bookings = bookingStorage.findByBookerIdAndStatusEqualsOrderByStartDateDesc(userId, REJECTED);
                log.info("Number of rejected bookings: {}", bookings.size());
                return bookings;
            default:
                log.info("Incorrect 'state' value: {}", state);
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    public List<Booking> getOwnerBooking(Long userId, String state) {
        log.info("Looking for bookings of owner {} with status {}", userId, state);
        getUser(userId);
        List<Booking> bookings;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        log.info("Now is: {}.", now);
        switch (state) {
            case "ALL":
                bookings = bookingStorage.findByItemOwnerIdOrderByStartDateDesc(userId);
                log.info("Number of bookings: {}", bookings.size());
                return bookings;
            case "CURRENT":
                bookings = bookingStorage.findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(userId, now, now);
                log.info("Number of current bookings: {}", bookings.size());
                return bookings;
            case "PAST":
                bookings = bookingStorage.findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(userId, now);
                log.info("Number of past bookings: {}", bookings.size());
                return bookings;
            case "FUTURE":
                bookings = bookingStorage.findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(userId, now);
                log.info("Number of future bookings: {}", bookings.size());
                return bookings;
            case "WAITING":
                bookings = bookingStorage.findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(userId, WAITING);
                log.info("Number of waiting bookings: {}", bookings.size());
                return bookings;
            case "REJECTED":
                bookings = bookingStorage.findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(userId, REJECTED);
                log.info("Number of rejected bookings: {}", bookings.size());
                return bookings;
            default:
                log.info("Incorrect state value: {}", state);
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Transactional
    public Booking approveBooking(Long userId, Boolean approved, Long bookingId) {
        log.info("Updating booking id {} as {} by user id {}", bookingId, approved, userId);
        Booking booking = bookingStorage.findById(bookingId).orElseThrow(() -> {
            log.info("Booking id {} not found.", bookingId);
            return new BookingNotFoundException(String.format("Booking id %s not found.", bookingId));
        });
        if (!booking.getItem().getOwnerId().equals(userId)) {
            log.info("User id {} has no access to booking id {}", userId, booking);
            throw new BookingNotFoundException(String.format("Booking id %s not found.", bookingId));
        }

        if (!booking.getStatus().equals(APPROVED)) {
            if (approved) {
                booking.setStatus(APPROVED);
                log.info("Booking status set APPROVED. Item is no longer available");
            } else {
                booking.setStatus(REJECTED);
                log.info("Booking status set rejected. Item is no longer available");
            }
        } else {
            log.info("Booking id {} already approved", bookingId);
            throw new ItemNotAvailableException(String.format("Booking id %s already approved", bookingId));
        }
        return booking;
    }

    private boolean isAvailableToBook(Booking booking) {
        List<Booking> bookings = bookingStorage.findByItemId(booking.getItem().getId());
        for (Booking b : bookings) {
            if (!(booking.getEndDate().before(b.getStartDate()) || booking.getStartDate().after(b.getEndDate()))) {
                log.info("Booking not available. Overlap with {}", b);
                return false;
            }
        }
        return true;
    }

    private User getUser(Long userId) {
        return userStorage.findById(userId).orElseThrow(() -> {
            log.info("User id {} not found", userId);
            return new UserNotFoundException(String.format("User id %s not found", userId));
        });
    }
}
