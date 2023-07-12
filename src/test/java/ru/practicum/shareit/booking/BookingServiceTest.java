/*
package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.exceptions.BookingNotAloudException;
import ru.practicum.shareit.booking.exceptions.BookingNotFoundException;
import ru.practicum.shareit.booking.exceptions.ItemNotAvailableException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingService bookingService;
    private User owner;
    private User booker;
    private UserResponseDto bookerDto;
    private Item item;
    private ItemResponseDto itemRequestDto;
    private BookingRequestDto bookingRequestDtoToSave;
    private Booking bookingToSave;
    private Booking savedBooking;
    private BookingResponseDto savedBookingRequestDto;
    private List<Booking> bookings;
    private Timestamp now = Timestamp.valueOf(LocalDateTime.now());
    private LocalDateTime start = LocalDateTime.now().plusHours(1);
    private LocalDateTime end = start.plusHours(1);
    private Long ownerId = 1L;
    private Long bookerId = 2L;
    private Long itemId = 1L;
    private String state;
    private Long bookingId = 1L;
    private int from = 0;
    private int size = 10;
    private PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);

    @BeforeEach
    public void beforeEach() {
        owner = User.builder()
                .id(ownerId)
                .build();

        booker = User.builder()
                .id(bookerId)
                .build();
        bookerDto = UserResponseDto.builder()
                .id(booker.getId())
                .build();

        item = Item.builder()
                .id(itemId)
                .name("name")
                .description("description")
                .ownerId(owner.getId())
                .isAvailable(true)
                .build();

        itemRequestDto = ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .build();

        bookingRequestDtoToSave = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(end)
                .build();

        bookingToSave = Booking.builder()
                .startDate(Timestamp.valueOf(bookingRequestDtoToSave.getStart()))
                .endDate(Timestamp.valueOf(bookingRequestDtoToSave.getEnd()))
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();

        savedBooking = bookingToSave;
        savedBooking.setId(bookingId);

        savedBookingRequestDto = BookingResponseDto.builder()
                .id(savedBooking.getId())
                .start(savedBooking.getStartDate().toLocalDateTime())
                .end(savedBooking.getEndDate().toLocalDateTime())
                .item(itemRequestDto)
                .booker(bookerDto)
                .status(savedBooking.getStatus())
                .build();

        bookings = new ArrayList<>();
        bookings.add(savedBooking);
    }

    @Test
    public void addBooking_Normal() {
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        BookingResponseDto result = bookingService.createBooking(bookingRequestDtoToSave, booker.getId());
        assertEquals(savedBookingRequestDto, result);
    }

    @Test
    public void addBooking_wrongStartDate() {
        bookingRequestDtoToSave.setStart(end.minusDays(1));

        Throwable e1 = assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingRequestDtoToSave, bookerId));
        assertEquals("Booking cannot start or end in past", e1.getMessage());

        bookingRequestDtoToSave.setStart(end);

        Throwable e2 = assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingRequestDtoToSave, bookerId));
        assertEquals("Booking start date should be before booking end date", e2.getMessage());

        verify(itemRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_wrongEndDate() {
        bookingRequestDtoToSave.setEnd(start.minusDays(1));

        Throwable e = assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingRequestDtoToSave, bookerId));
        assertEquals("Booking start date should be before booking end date", e.getMessage());

        verify(itemRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_NoSuchItem() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        Throwable e = assertThrows(ItemNotFoundException.class, () ->
                bookingService.createBooking(bookingRequestDtoToSave, bookerId));
        assertEquals(String.format("Item id %s not found", itemId), e.getMessage());

        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_ItemMotAvailable() {
        item.setIsAvailable(false);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Throwable e = assertThrows(ItemNotAvailableException.class, () ->
                bookingService.createBooking(bookingRequestDtoToSave, bookerId));
        assertEquals(String.format("Item id %s not available", itemId), e.getMessage());

        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_BookingOwnItem() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Throwable e = assertThrows(BookingNotAloudException.class, () ->
                bookingService.createBooking(bookingRequestDtoToSave, item.getOwnerId()));
        assertEquals("Booking own item is not aloud.", e.getMessage());

        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_noSuchBooker() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        Throwable e = assertThrows(UserNotFoundException.class, () ->
                bookingService.createBooking(bookingRequestDtoToSave, bookerId));
        assertEquals(String.format("User id %s not found", bookerId), e.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_ItemBookingOverlap_startBeforeEndWithin() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(itemId)).thenReturn(bookings);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));

        BookingRequestDto otherBooking = BookingRequestDto.builder()
                .itemId(itemId)
                .start(start.minusMinutes(1))
                .end(end.minusMinutes(1))
                .build();

        Throwable e = assertThrows(ItemNotAvailableException.class, () ->
                bookingService.createBooking(otherBooking, bookerId));
        assertEquals("Item is already booked for this period.", e.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_ItemBookingOverlap_startWithinEndWithin() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(itemId)).thenReturn(bookings);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));

        BookingRequestDto otherBooking = BookingRequestDto.builder()
                .itemId(itemId)
                .start(start.plusMinutes(1))
                .end(end.minusMinutes(1))
                .build();

        Throwable e = assertThrows(ItemNotAvailableException.class, () ->
                bookingService.createBooking(otherBooking, bookerId));
        assertEquals("Item is already booked for this period.", e.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_ItemBookingOverlap_startWithinEndAfter() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(itemId)).thenReturn(bookings);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));

        BookingRequestDto otherBooking = BookingRequestDto.builder()
                .itemId(itemId)
                .start(start.plusMinutes(1))
                .end(end.plusMinutes(1))
                .build();

        Throwable e = assertThrows(ItemNotAvailableException.class, () ->
                bookingService.createBooking(otherBooking, bookerId));
        assertEquals("Item is already booked for this period.", e.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void findBooking_Normal() {
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(savedBooking));

        BookingResponseDto result = bookingService.findBooking(bookingId, bookerId);

        assertEquals(savedBookingRequestDto, result);
    }

    @Test
    public void findBooking_NoSuchBooking() {
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        Throwable e = assertThrows(BookingNotFoundException.class, () ->
                bookingService.findBooking(bookingId, bookerId));

        assertEquals(String.format("Booking id %s not found.", bookingId), e.getMessage());
    }

    @Test
    public void findBooking_BookerNoAccessToBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(savedBooking));

        Throwable e = assertThrows(BookingNotFoundException.class, () ->
                bookingService.findBooking(bookingId, 999L));

        assertEquals(String.format("Booking id %s not found.", bookingId), e.getMessage());
    }

    @Test
    public void getUserBookings_StateALL() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByStartDateDesc(bookerId, page)).thenReturn(new PageImpl<>(bookings));
        state = "ALL";

        List<BookingResponseDto> result = bookingService.getUserBookings(bookerId, state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(Timestamp.class), any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStartDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStatusEqualsOrderByStartDateDesc(anyLong(), any(Status.class),
                        any(PageRequest.class));
    }

    @Test
    public void getUserBookings_StateCURRENT() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository
                .findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(Timestamp.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(bookings));

        state = "CURRENT";

        List<BookingResponseDto> result = bookingService.getUserBookings(bookerId, state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));


        verify(bookingRepository, never())
                .findByBookerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class));

        verify(bookingRepository, never())
                .findByBookerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStartDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStatusEqualsOrderByStartDateDesc(anyLong(), any(Status.class), any(PageRequest.class));
    }

    @Test
    public void getUserBookings_StatePAST() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository
                .findByBookerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class)))
                .thenReturn(new PageImpl<>(bookings));

        state = "PAST";

        List<BookingResponseDto> result = bookingService.getUserBookings(bookerId, state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByBookerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(Timestamp.class), any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStartDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStatusEqualsOrderByStartDateDesc(anyLong(), any(Status.class), any(PageRequest.class));
    }

    @Test
    public void getUserBookings_StateFUTURE() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository
                .findByBookerIdAndStartDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class)))
                .thenReturn(new PageImpl<>(bookings));

        state = "FUTURE";

        List<BookingResponseDto> result = bookingService.getUserBookings(bookerId, state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByBookerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(Timestamp.class), any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStatusEqualsOrderByStartDateDesc(anyLong(), any(Status.class), any(PageRequest.class));
    }

    @Test
    public void getUserBookings_StateWAITING() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository
                .findByBookerIdAndStatusEqualsOrderByStartDateDesc(anyLong(), any(Status.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(bookings));

        state = "WAITING";

        List<BookingResponseDto> result = bookingService.getUserBookings(bookerId, state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByBookerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(Timestamp.class), any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStartDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class));
    }

    @Test
    public void getUserBookings_StateREJECTED() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository
                .findByBookerIdAndStatusEqualsOrderByStartDateDesc(anyLong(), any(Status.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(bookings));

        state = "REJECTED";

        List<BookingResponseDto> result = bookingService.getUserBookings(bookerId, state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByBookerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(Timestamp.class), any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStartDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class));
    }

    @Test
    public void getUserBookings_StateDefault() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));

        state = "default state";

        Throwable e = assertThrows(ValidationException.class, () ->
                bookingService.getUserBookings(bookerId, state, from, size));

        assertEquals("Unknown state: UNSUPPORTED_STATUS", e.getMessage());

        verify(bookingRepository, never())
                .findByBookerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(Timestamp.class), any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStartDateAfterOrderByStartDateDesc(anyLong(), any(Timestamp.class),
                        any(PageRequest.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStatusEqualsOrderByStartDateDesc(anyLong(), any(Status.class), any(PageRequest.class));
    }

    @Test
    public void getOwnerBookings_StateALL() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByItemOwnerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(bookings));

        state = "ALL";

        List<BookingResponseDto> result = bookingService.getOwnerBooking(bookerId, state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(Timestamp.class), any(PageRequest.class)); //current
        verify(bookingRepository, never())
                .findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(PageRequest.class)); //past
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(PageRequest.class)); //future
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(anyLong(),
                        any(Status.class), any(PageRequest.class)); //waiting, rejected
    }

    @Test
    public void getOwnerBookings_StateCURRENT() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(),
                any(Timestamp.class), any(Timestamp.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(bookings));

        state = "CURRENT";

        List<BookingResponseDto> result = bookingService.getOwnerBooking(bookerId, state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByItemOwnerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class)); //ALL
        verify(bookingRepository, never())
                .findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(PageRequest.class)); //past
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(PageRequest.class)); //future
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(anyLong(),
                        any(Status.class), any(PageRequest.class)); //waiting, rejected
    }

    @Test
    public void getOwnerBookings_StatePast() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(),
                any(Timestamp.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(bookings));

        state = "PAST";

        List<BookingResponseDto> result = bookingService.getOwnerBooking(bookerId, state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByItemOwnerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class)); //ALL
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(Timestamp.class), any(PageRequest.class)); //current
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(PageRequest.class)); //future
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(anyLong(),
                        any(Status.class), any(PageRequest.class)); //waiting, rejected
    }

    @Test
    public void getOwnerBookings_StateFuture() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(anyLong(),
                any(Timestamp.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(bookings));

        state = "FUTURE";

        List<BookingResponseDto> result = bookingService.getOwnerBooking(bookerId, state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByItemOwnerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class)); //ALL
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(Timestamp.class), any(PageRequest.class)); //current
        verify(bookingRepository, never())
                .findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(PageRequest.class)); //Past
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(anyLong(),
                        any(Status.class), any(PageRequest.class)); //waiting, rejected
    }

    @Test
    public void getOwnerBookings_StateWaiting() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(anyLong(),
                any(Status.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(bookings));

        state = "WAITING";

        List<BookingResponseDto> result = bookingService.getOwnerBooking(bookerId, state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByItemOwnerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class)); //ALL
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(Timestamp.class), any(PageRequest.class)); //current
        verify(bookingRepository, never())
                .findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(PageRequest.class)); //Past
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(PageRequest.class)); //Future
    }

    @Test
    public void getOwnerBookings_StateRejected() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(anyLong(),
                any(Status.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(bookings));

        state = "REJECTED";

        List<BookingResponseDto> result = bookingService.getOwnerBooking(bookerId, state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByItemOwnerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class)); //ALL
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(Timestamp.class), any(PageRequest.class)); //current
        verify(bookingRepository, never())
                .findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(PageRequest.class)); //Past
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(PageRequest.class)); //Future
    }

    @Test
    public void getOwnerBookings_StateDefault() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        state = "default state";

        Throwable e = assertThrows(ValidationException.class, () ->
                bookingService.getOwnerBooking(bookerId, state, from, size));

        assertEquals("Unknown state: UNSUPPORTED_STATUS", e.getMessage());

        verify(bookingRepository, never())
                .findByItemOwnerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class)); //ALL
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(Timestamp.class), any(PageRequest.class)); //current
        verify(bookingRepository, never())
                .findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(PageRequest.class)); //Past
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(anyLong(),
                        any(Timestamp.class), any(PageRequest.class)); //Future
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(anyLong(),
                        any(Status.class), any(PageRequest.class)); //waiting, rejected
    }

    @Test
    public void approveBooking_AcceptNormal() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(savedBooking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        savedBookingRequestDto.setStatus(Status.APPROVED);

        BookingResponseDto result = bookingService.approveBooking(ownerId, true, bookingId);

        assertEquals(savedBookingRequestDto, result);
    }

    @Test
    public void approveBooking_RejectedNormal() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(savedBooking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        savedBookingRequestDto.setStatus(Status.REJECTED);

        BookingResponseDto result = bookingService.approveBooking(ownerId, false, bookingId);

        assertEquals(savedBookingRequestDto, result);
    }

    @Test
    public void approveBooking_NoSuchBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        Throwable e = assertThrows(BookingNotFoundException.class, () ->
                bookingService.approveBooking(ownerId, true, bookingId));

        assertEquals(String.format("Booking id %s not found.", bookingId), e.getMessage());
    }

    @Test
    public void approveBooking_NotByOwner() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(savedBooking));

        Throwable e = assertThrows(BookingNotFoundException.class, () ->
                bookingService.approveBooking(999L, true, bookingId));

        assertEquals(String.format("Booking id %s not found.", bookingId), e.getMessage());
    }

    @Test
    public void approveBooking_AlreadyApproved() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(savedBooking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        savedBooking.setStatus(Status.APPROVED);

        Throwable e = assertThrows(ItemNotAvailableException.class, ()
                -> bookingService.approveBooking(ownerId, true, bookingId));

        assertEquals(String.format("Booking id %s already approved", bookingId), e.getMessage());
    }
}
*/
