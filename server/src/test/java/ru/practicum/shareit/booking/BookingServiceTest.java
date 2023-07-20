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
    private Item item;
    private BookingRequestDto bookingRequestDtoToSave;
    private Booking savedBooking;
    private BookingResponseDto savedBookingRequestDto;
    private List<Booking> bookings;

    @BeforeEach
    public void beforeEach() {
        owner = User.builder()
                .id(1L)
                .build();

        booker = User.builder()
                .id(2L)
                .build();
        UserResponseDto bookerDto = UserResponseDto.builder()
                .id(booker.getId())
                .build();

        item = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .ownerId(owner.getId())
                .isAvailable(true)
                .build();

        ItemResponseDto itemRequestDto = ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .build();

        bookingRequestDtoToSave = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        savedBooking = Booking.builder()
                .startDate(Timestamp.valueOf(bookingRequestDtoToSave.getStart()))
                .endDate(Timestamp.valueOf(bookingRequestDtoToSave.getEnd()))
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();
        savedBooking.setId(1L);

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
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        BookingResponseDto result = bookingService.createBooking(bookingRequestDtoToSave, booker.getId());
        assertEquals(savedBookingRequestDto, result);
    }

    @Test
    public void addBooking_wrongStartDate() {
        bookingRequestDtoToSave.setStart(LocalDateTime.now().minusDays(1));

        Throwable e1 = assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingRequestDtoToSave, booker.getId()));
        assertEquals("Booking cannot start or end in past", e1.getMessage());

        bookingRequestDtoToSave.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDtoToSave.setEnd(bookingRequestDtoToSave.getStart());

        Throwable e2 = assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingRequestDtoToSave, booker.getId()));
        assertEquals("Booking start date should be before booking end date", e2.getMessage());

        verify(itemRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_wrongEndDate() {
        bookingRequestDtoToSave.setEnd(bookingRequestDtoToSave.getStart().minusDays(1));

        Throwable e = assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingRequestDtoToSave, booker.getId()));
        assertEquals("Booking start date should be before booking end date", e.getMessage());

        verify(itemRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_NoSuchItem() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        Throwable e = assertThrows(ItemNotFoundException.class, () ->
                bookingService.createBooking(bookingRequestDtoToSave, booker.getId()));
        assertEquals(String.format("Item id %s not found", item.getId()), e.getMessage());

        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_ItemMotAvailable() {
        item.setIsAvailable(false);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        Throwable e = assertThrows(ItemNotAvailableException.class, () ->
                bookingService.createBooking(bookingRequestDtoToSave, booker.getId()));
        assertEquals(String.format("Item id %s not available", item.getId()), e.getMessage());

        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_BookingOwnItem() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        Throwable e = assertThrows(BookingNotAloudException.class, () ->
                bookingService.createBooking(bookingRequestDtoToSave, item.getOwnerId()));
        assertEquals("Booking own item is not aloud.", e.getMessage());

        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_noSuchBooker() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.empty());

        Throwable e = assertThrows(UserNotFoundException.class, () ->
                bookingService.createBooking(bookingRequestDtoToSave, booker.getId()));
        assertEquals(String.format("User id %s not found", booker.getId()), e.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_ItemBookingOverlap_startBeforeEndWithin() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(item.getId())).thenReturn(bookings);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        BookingRequestDto otherBooking = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(savedBooking.getStartDate().toLocalDateTime().minusMinutes(1))
                .end(savedBooking.getEndDate().toLocalDateTime().minusMinutes(1))
                .build();

        Throwable e = assertThrows(ItemNotAvailableException.class, () ->
                bookingService.createBooking(otherBooking, booker.getId()));
        assertEquals("Item is already booked for this period.", e.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_ItemBookingOverlap_startWithinEndWithin() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(item.getId())).thenReturn(bookings);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        BookingRequestDto otherBooking = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(savedBooking.getStartDate().toLocalDateTime().plusMinutes(1))
                .end(savedBooking.getEndDate().toLocalDateTime().minusMinutes(1))
                .build();

        Throwable e = assertThrows(ItemNotAvailableException.class, () ->
                bookingService.createBooking(otherBooking, booker.getId()));
        assertEquals("Item is already booked for this period.", e.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void addBooking_ItemBookingOverlap_startWithinEndAfter() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(item.getId())).thenReturn(bookings);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        BookingRequestDto otherBooking = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Throwable e = assertThrows(ItemNotAvailableException.class, () ->
                bookingService.createBooking(otherBooking, booker.getId()));
        assertEquals("Item is already booked for this period.", e.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void findBooking_Normal() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(savedBooking.getId())).thenReturn(Optional.of(savedBooking));

        BookingResponseDto result = bookingService.findBooking(savedBooking.getId(), booker.getId());

        assertEquals(savedBookingRequestDto, result);
    }

    @Test
    public void findBooking_NoSuchBooking() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(savedBooking.getId())).thenReturn(Optional.empty());

        Throwable e = assertThrows(BookingNotFoundException.class, () ->
                bookingService.findBooking(savedBooking.getId(), booker.getId()));

        assertEquals(String.format("Booking id %s not found.", savedBooking.getId()), e.getMessage());
    }

    @Test
    public void findBooking_BookerNoAccessToBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(savedBooking.getId())).thenReturn(Optional.of(savedBooking));

        Throwable e = assertThrows(BookingNotFoundException.class, () ->
                bookingService.findBooking(savedBooking.getId(), 999L));

        assertEquals(String.format("Booking id %s not found.", savedBooking.getId()), e.getMessage());
    }

    @Test
    public void getUserBookings_StateALL() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByStartDateDesc(any(), any())).thenReturn(new PageImpl<>(bookings));
        String state = "ALL";
        int from = 0;
        int size = 10;

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), state, from, size);

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

        String state = "CURRENT";
        int from = 0;
        int size = 10;

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), state, from, size);

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

        String state = "PAST";
        int from = 0;
        int size = 10;

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), state, from, size);

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

        String state = "FUTURE";
        int from = 0;
        int size = 10;

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), state, from, size);

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

        String state = "WAITING";
        int from = 0;
        int size = 10;

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), state, from, size);

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

        String state = "REJECTED";
        int from = 0;
        int size = 10;

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), state, from, size);

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

        String state = "default state";
        int from = 0;
        int size = 10;

        Throwable e = assertThrows(ValidationException.class, () ->
                bookingService.getUserBookings(booker.getId(), state, from, size));

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

        String state = "ALL";
        int from = 0;
        int size = 10;

        List<BookingResponseDto> result = bookingService.getOwnerBooking(booker.getId(), state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderById(anyLong(),
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
        when(bookingRepository.findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderById(anyLong(),
                any(Timestamp.class), any(Timestamp.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(bookings));

        String state = "CURRENT";
        int from = 0;
        int size = 10;

        List<BookingResponseDto> result = bookingService.getOwnerBooking(booker.getId(), state, from, size);

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

        String state = "PAST";
        int from = 0;
        int size = 10;

        List<BookingResponseDto> result = bookingService.getOwnerBooking(booker.getId(), state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByItemOwnerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class)); //ALL
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderById(anyLong(),
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

        String state = "FUTURE";
        int from = 0;
        int size = 10;

        List<BookingResponseDto> result = bookingService.getOwnerBooking(booker.getId(), state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByItemOwnerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class)); //ALL
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderById(anyLong(),
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

        String state = "WAITING";
        int from = 0;
        int size = 10;

        List<BookingResponseDto> result = bookingService.getOwnerBooking(booker.getId(), state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByItemOwnerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class)); //ALL
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderById(anyLong(),
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

        String state = "REJECTED";
        int from = 0;
        int size = 10;

        List<BookingResponseDto> result = bookingService.getOwnerBooking(booker.getId(), state, from, size);

        assertEquals(1, result.size());
        assertEquals(savedBookingRequestDto, result.get(0));

        verify(bookingRepository, never())
                .findByItemOwnerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class)); //ALL
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderById(anyLong(),
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
        String state = "default state";
        int from = 0;
        int size = 10;

        Throwable e = assertThrows(ValidationException.class, () ->
                bookingService.getOwnerBooking(booker.getId(), state, from, size));

        assertEquals("Unknown state: UNSUPPORTED_STATUS", e.getMessage());

        verify(bookingRepository, never())
                .findByItemOwnerIdOrderByStartDateDesc(anyLong(), any(PageRequest.class)); //ALL
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderById(anyLong(),
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
        when(bookingRepository.findById(savedBooking.getId())).thenReturn(Optional.of(savedBooking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        savedBookingRequestDto.setStatus(Status.APPROVED);

        BookingResponseDto result = bookingService.approveBooking(owner.getId(), true, savedBooking.getId());

        assertEquals(savedBookingRequestDto, result);
    }

    @Test
    public void approveBooking_RejectedNormal() {
        when(bookingRepository.findById(savedBooking.getId())).thenReturn(Optional.of(savedBooking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        savedBookingRequestDto.setStatus(Status.REJECTED);

        BookingResponseDto result = bookingService.approveBooking(owner.getId(), false, savedBooking.getId());

        assertEquals(savedBookingRequestDto, result);
    }

    @Test
    public void approveBooking_NoSuchBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(savedBooking.getId())).thenReturn(Optional.empty());

        Throwable e = assertThrows(BookingNotFoundException.class, () ->
                bookingService.approveBooking(owner.getId(), true, savedBooking.getId()));

        assertEquals(String.format("Booking id %s not found.", savedBooking.getId()), e.getMessage());
    }

    @Test
    public void approveBooking_NotByOwner() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(savedBooking.getId())).thenReturn(Optional.of(savedBooking));

        Throwable e = assertThrows(BookingNotFoundException.class, () ->
                bookingService.approveBooking(999L, true, savedBooking.getId()));

        assertEquals(String.format("Booking id %s not found.", savedBooking.getId()), e.getMessage());
    }

    @Test
    public void approveBooking_AlreadyApproved() {
        when(bookingRepository.findById(savedBooking.getId())).thenReturn(Optional.of(savedBooking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        savedBooking.setStatus(Status.APPROVED);

        Throwable e = assertThrows(ItemNotAvailableException.class, ()
                -> bookingService.approveBooking(owner.getId(), true, savedBooking.getId()));

        assertEquals(String.format("Booking id %s already approved", savedBooking.getId()), e.getMessage());
    }
}
