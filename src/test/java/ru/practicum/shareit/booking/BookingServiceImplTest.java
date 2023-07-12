package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
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
        when(bookingRepository.findByBookerId(any(), any())).thenReturn(Page.empty());
        var result = bookingService.getBookings(1L, BookingState.ALL, Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByBooker_Id(any(), any());
    }

    @Test
    void getBookingsPast() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBooker_IdAndEndIsBefore(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getBookings(1L, BookingState.PAST, Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByBooker_IdAndEndIsBefore(any(), any(), any());
    }

    @Test
    void getBookingsFuture() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBooker_IdAndStartIsAfter(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getBookings(1L, BookingState.FUTURE, Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByBooker_IdAndStartIsAfter(any(), any(), any());
    }

    @Test
    void getBookingsCurrent() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findCurrentForDate(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getBookings(1L, BookingState.CURRENT, Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findCurrentForDate(any(), any(), any());
    }

    @Test
    void getBookingsWaiting() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findPending(any(), any())).thenReturn(Page.empty());
        var result = bookingService.getBookings(1L, BookingState.WAITING, Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findPending(any(), any());
    }

    @Test
    void getBookingsRejected() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findCanceled(any(), any())).thenReturn(Page.empty());
        var result = bookingService.getBookings(1L, BookingState.REJECTED, Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findCanceled(any(), any());
    }

    @Test
    void getOwnerBookingsAll() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItem_Owner_Id(any(), any())).thenReturn(Page.empty());
        var result = bookingService.getOwnerBookings(1L, BookingState.ALL, Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByItem_Owner_Id(any(), any());
    }

    @Test
    void getOwnerBookingsPast() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItem_Owner_IdAndEndIsBefore(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getOwnerBookings(1L, BookingState.PAST, Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByItem_Owner_IdAndEndIsBefore(any(), any(), any());
    }

    @Test
    void getOwnerBookingsFuture() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByItem_Owner_IdAndStartIsAfter(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getOwnerBookings(1L, BookingState.FUTURE, Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByItem_Owner_IdAndStartIsAfter(any(), any(), any());
    }

    @Test
    void getOwnerBookingsCurrent() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findOwnerCurrentForDate(any(), any(), any())).thenReturn(Page.empty());
        var result = bookingService.getOwnerBookings(1L, BookingState.CURRENT, Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findOwnerCurrentForDate(any(), any(), any());
    }

    @Test
    void getOwnerBookingsWaiting() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findOwnerPending(any(), any())).thenReturn(Page.empty());
        var result = bookingService.getOwnerBookings(1L, BookingState.WAITING, Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findOwnerPending(any(), any());
    }

    @Test
    void getOwnerBookingsRejected() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findOwnerCanceled(any(), any())).thenReturn(Page.empty());
        var result = bookingService.getOwnerBookings(1L, BookingState.REJECTED, Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findOwnerCanceled(any(), any());
    }

    @Test
    void bookItem() {
        var userId = 1L;
        var user = new User();
        user.setId(2L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        var requestDto = new BookItemRequestDto(1L, LocalDateTime.now(), LocalDateTime.now());
        Item item = new Item();
        item.setId(3L);
        item.setOwner(user);
        item.setAvailable(true);
        when(itemRepository.findById(requestDto.getItemId())).thenReturn(Optional.of(item));
        var result = bookingService.bookItem(userId, requestDto);
        Assertions.assertNotNull(result);
    }

    @Test
    void getBooking() {
        var bookingId = 2L;
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> bookingService.getBooking(user.getId(), bookingId));
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
        booking.setId(3L);
        booking.setStatus(Status.WAITING);
        booking.setStartDate(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        booking.setBooker(user);
        booking.setItem(item);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByIdAndItem_Owner_Id(bookingId, user.getId())).thenReturn(Optional.of(booking));
        var result = bookingService.setBookingApproveState(user.getId(), bookingId, approved);
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
        booking.setId(3L);
        booking.setStatus(Status.WAITING);
        booking.setStartDate(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        booking.setBooker(user);
        booking.setItem(item);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByIdAndItem_Owner_Id(bookingId, user.getId())).thenReturn(Optional.of(booking));
        var result = bookingService.setBookingApproveState(user.getId(), bookingId, approved);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.REJECTED, result.getStatus());
    }
}
