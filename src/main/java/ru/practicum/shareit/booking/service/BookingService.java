package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.exceptions.BookingNotAloudException;
import ru.practicum.shareit.booking.exceptions.BookingNotFoundException;
import ru.practicum.shareit.booking.exceptions.ItemNotAvailableException;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.model.Status.*;

@Service
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, UserRepository userRepository, ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long bookerId) {
        log.info("Adding booking: {} by user {}", bookingRequestDto, bookerId);
        Booking booking = BookingMapper.mapFromDto(bookingRequestDto, bookerId, WAITING);
        log.info("Booking mapped: {}.", booking);

        if (booking.getEndDate().before(booking.getStartDate()) || booking.getStartDate().equals(booking.getEndDate())) {
            log.error("Booking start date should be before booking end date");
            throw new ValidationException("Booking start date should be before booking end date");
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        if (booking.getEndDate().before(now) || booking.getStartDate().before(now)) {
            log.error("Booking cannot start or end in past");
            throw new ValidationException("Booking cannot start or end in past");
        }

        Item item = itemRepository.findById(booking.getItem().getId()).orElseThrow(() -> {
            log.error("Item id {} not found", booking.getItem().getId());
            return new ItemNotFoundException(String.format("Item id %s not found", booking.getItem().getId()));
        });

        if (!item.getIsAvailable()) {
            log.error("Item id {} not available", booking.getItem().getId());
            throw new ItemNotAvailableException(String.format("Item id %s not available", booking.getItem().getId()));
        }

        if (item.getOwnerId().equals(booking.getBooker().getId())) {
            log.error("Booking own item is not aloud.");
            throw new BookingNotAloudException("Booking own item is not aloud.");
        }

        User user = getUser(booking.getBooker().getId());

        if (!isAvailableToBook(booking)) {
            throw new ItemNotAvailableException("Item is already booked for this period.");
        }

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking saved: {}", savedBooking);
        BookingResponseDto savedBookingRequestDto = BookingMapper.mapToDto(savedBooking, user, item);
        log.info("Booking mapped to DTO: {}", savedBookingRequestDto);
        return savedBookingRequestDto;
    }

    public BookingResponseDto findBooking(Long bookingId, Long bookerId) {
        log.info("Looking for booking id {} by user id {}", bookingId, bookerId);
        getUser(bookerId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            log.error("Booking id {} not found.", bookingId);
            return new BookingNotFoundException(String.format("Booking id %s not found.", bookingId));
        });
        log.info("Booking found: {}.", booking);

        if (!(booking.getItem().getOwnerId().equals(bookerId) || booking.getBooker().getId().equals(bookerId))) {
            log.error("User id {} has no access to booking id {}", bookerId, booking);
            throw new BookingNotFoundException(String.format("Booking id %s not found.", bookingId));
        }
        BookingResponseDto bookingRequestDto = BookingMapper.mapToDto(booking);
        log.info("Booking mapped to DTO: {}", bookingRequestDto);
        return bookingRequestDto;
    }

    public List<BookingResponseDto> getUserBookings(Long bookerId, String state, int from, int size) {
        log.info("Looking for bookings of user {} with status {}", bookerId, state);
        getUser(bookerId);
        Page<Booking> bookings;
        final PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        log.info("Now is: {}.", now);
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByBookerIdOrderByStartDateDesc(bookerId, page);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(bookerId,
                        now, now, page);
                break;
            case "PAST":
                bookings = bookingRepository.findByBookerIdAndEndDateBeforeOrderByStartDateDesc(bookerId, now, page);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerIdAndStartDateAfterOrderByStartDateDesc(bookerId, now, page);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatusEqualsOrderByStartDateDesc(bookerId, WAITING, page);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatusEqualsOrderByStartDateDesc(bookerId, REJECTED, page);
                break;
            default:
                log.error("Incorrect 'state' value: {}", state);
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookings.map(BookingMapper::mapToDto).getContent();
    }

    public List<BookingResponseDto> getOwnerBooking(Long userId, String state, int from, int size) {
        log.info("Looking for bookings of owner {} with status {}", userId, state);
        getUser(userId);
        Page<Booking> bookings;
        final PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        log.info("Now is: {}.", now);
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDateDesc(userId, page);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(
                        userId, now, now, page);
                break;
            case "PAST":
                bookings = bookingRepository.findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(userId, now, page);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(userId, now, page);
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(userId, WAITING, page);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(userId, REJECTED, page);
                break;
            default:
                log.error("Incorrect state value: {}", state);
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookings.map(BookingMapper::mapToDto).getContent();
    }

    @Transactional
    public BookingResponseDto approveBooking(Long ownerId, Boolean approved, Long bookingId) {
        log.info("Updating booking id {} as {} by user id {}", bookingId, approved, ownerId);
        getUser(ownerId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            log.error("Booking id {} not found.", bookingId);
            return new BookingNotFoundException(String.format("Booking id %s not found.", bookingId));
        });
        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            log.error("User id {} has no access to booking id {}", ownerId, booking);
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
            log.error("Booking id {} already approved", bookingId);
            throw new ItemNotAvailableException(String.format("Booking id %s already approved", bookingId));
        }
        return BookingMapper.mapToDto(booking);
    }

    private boolean isAvailableToBook(Booking booking) {
        List<Booking> bookings = bookingRepository.findByItemId(booking.getItem().getId());
        for (Booking b : bookings) {
            if (!(booking.getEndDate().before(b.getStartDate()) || booking.getStartDate().after(b.getEndDate()))) {
                log.info("Booking not available. Overlap with {}", b);
                return false;
            }
        }
        return true;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.error("User id {} not found", userId);
            return new UserNotFoundException(String.format("User id %s not found", userId));
        });
    }
}
