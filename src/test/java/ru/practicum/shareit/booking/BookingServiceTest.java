package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    private UserDto bookerDto;
    private Item item;
    private ItemDto itemDto;
    private BookingDto bookingDtoToSave;
    private Booking bookingToSave;
    private Booking savedBooking;
    private BookingDto savedBookingDto;
    private LocalDateTime start = LocalDateTime.now().plusHours(1);
    private LocalDateTime end = start.plusHours(1);
    private Long ownerId = 1L;
    private Long bookerId = 2L;
    private Long itemId = 1L;
    private Long bookingId;
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
        bookerDto = UserDto.builder()
                .id(booker.getId())
                .build();

        item = Item.builder()
                .id(itemId)
                .name("name")
                .description("description")
                .ownerId(owner.getId())
                .isAvailable(true)
                .build();

        itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .build();

        bookingDtoToSave = BookingDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(end)
                .build();

        bookingToSave = Booking.builder()
                .startDate(Timestamp.valueOf(bookingDtoToSave.getStart()))
                .endDate(Timestamp.valueOf(bookingDtoToSave.getEnd()))
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();

        savedBooking = bookingToSave;
        savedBooking.setId(bookingId);

        savedBookingDto = BookingDto.builder()
                .id(savedBooking.getId())
                .start(savedBooking.getStartDate().toLocalDateTime())
                .end(savedBooking.getEndDate().toLocalDateTime())
                .item(itemDto)
                .booker(bookerDto)
                .status(savedBooking.getStatus())
                .build();
    }

    @Test
    public void addBooking_Normal() {
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        BookingDto result = bookingService.createBooking(bookingDtoToSave, booker.getId());
        assertEquals(savedBookingDto, result);
    }
}
