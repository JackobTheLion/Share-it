package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    private BookingService bookingService;

    private BookingRepository bookingRepository;
    private UserRepository userRepository;
    private ItemRepository itemRepository;

    private User user;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        userRepository = mock(UserRepository.class);
        itemRepository = mock(ItemRepository.class);
        bookingService = new BookingService(bookingRepository, userRepository, itemRepository);
        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("test@google.com");
    }

    @Test
    void getBookingsAll() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdOrderByStartDateDesc(any(), any())).thenReturn(Page.empty());
        var result = bookingService.getUserBookings(1L, "ALL", 0, 10);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByBookerIdOrderByStartDateDesc(any(), any());
    }

    @Test
    void getBookingsPast() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndEndDateBeforeOrderByStartDateDesc(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getUserBookings(1L, "PAST", 1, 10);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByBookerIdAndEndDateBeforeOrderByStartDateDesc(any(), any(), any());
    }

    @Test
    void getBookingsFuture() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStartDateAfterOrderByStartDateDesc(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getUserBookings(1L, "FUTURE", 1, 10);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByBookerIdAndStartDateAfterOrderByStartDateDesc(any(), any(), any());
    }

    @Test
    void getBookingsCurrent() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(any(), any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getUserBookings(1L, "CURRENT", 1, 10);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(any(), any(), any(), any());
    }

    @Test
    void getBookingsWaiting() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatusEqualsOrderByStartDateDesc(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getUserBookings(1L, "WAITING", 1, 10);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByBookerIdAndStatusEqualsOrderByStartDateDesc(any(), any(), any());
    }

    @Test
    void getBookingsRejected() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatusEqualsOrderByStartDateDesc(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getUserBookings(1L, "REJECTED", 1, 10);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByBookerIdAndStatusEqualsOrderByStartDateDesc(any(), any(), any());
    }

    @Test
    void getOwnerBookingsAll() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdOrderByStartDateDesc(any(), any())).thenReturn(Page.empty());
        var result = bookingService.getOwnerBooking(1L, "ALL", 0, 10);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByItemOwnerIdOrderByStartDateDesc(any(), any());
    }

    @Test
    void getOwnerBookingsPast() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getOwnerBooking(1L, "PAST", 1, 10);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(any(), any(), any());
    }

    @Test
    void getOwnerBookingsFuture() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getOwnerBooking(1L, "FUTURE", 0, 10);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(any(), any(), any());
    }

    @Test
    void getOwnerBookingsCurrent() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(any(), any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getOwnerBooking(1L, "CURRENT", 0, 10);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(any(), any(), any(), any());
    }

    @Test
    void getOwnerBookingsWaiting() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getOwnerBooking(1L, "WAITING", 0, 10);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(any(), any(), any());
    }

    @Test
    void getOwnerBookingsRejected() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getOwnerBooking(1L, "REJECTED", 0, 10);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(any(), any(), any());
    }

    @Test
    void bookItem() {
        var userId = 1L;
        var user = new User();
        user.setId(2L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        var requestDto = BookingRequestDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusMonths(1))
                .build();
        Item item = new Item();
        item.setId(3L);
        item.setOwnerId(user.getId());
        item.setIsAvailable(true);
        when(itemRepository.findById(requestDto.getItemId())).thenReturn(Optional.of(item));
        var result = bookingService.createBooking(requestDto, userId);
        Assertions.assertNotNull(result);
    }

    @Test
    void getBooking() {
        var bookingId = 2L;
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> bookingService.findBooking(user.getId(), bookingId));
    }

    @Test
    void setBookingApproveStateApproved() {
        var bookingId = 2L;
        var approved = true;
        Item item = new Item();
        item.setId(3L);
        item.setOwnerId(user.getId());
        item.setIsAvailable(true);
        var booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(Status.WAITING);
        booking.setStartDate(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        booking.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
        booking.setBooker(user);
        booking.setItem(item);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        var result = bookingService.approveBooking(user.getId(), approved, booking.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.APPROVED, result.getStatus());
    }

    @Test
    void setBookingApproveStateRejected() {
        var bookingId = 2L;
        var approved = false;
        Item item = new Item();
        item.setId(3L);
        item.setOwnerId(user.getId());
        item.setIsAvailable(true);
        var booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(Status.WAITING);
        booking.setStartDate(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        booking.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
        booking.setBooker(user);
        booking.setItem(item);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        var result = bookingService.approveBooking(user.getId(), approved, booking.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.REJECTED, result.getStatus());
    }
}
