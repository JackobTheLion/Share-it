package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.data.domain.Sort.Direction.DESC;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private Item item;

    private User booker;

    @BeforeEach
    void setUp() {
        this.item = createItem("Pencil", "Basic drawing tool", false, createUser("Mike Turtle", "mikelangelo@tmnt.com"));
        this.booker = createUser("Mr. Smith", "find.neo@matrix.com");
    }

    private Item createItem(String name, String description, boolean available, User owner) {
        var item = new Item();
        item.setIsAvailable(available);
        item.setName(name);
        item.setDescription(description);
        item.setOwnerId(owner.getId());
        return itemRepository.save(item);
    }

    private User createUser(String name, String email) {
        var owner = new User();
        owner.setName(name);
        owner.setEmail(email);
        return userRepository.save(owner);
    }

    private Booking createBooking(Status status, Item item, User booker, LocalDateTime start, LocalDateTime end) {
        var booking = new Booking();
        booking.setStatus(status);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStartDate(Timestamp.valueOf(start));
        booking.setEndDate(Timestamp.valueOf(end));
        return bookingRepository.save(booking);
    }

    @Test
    void testFindApprovedItems() {
        var booking = createBooking(Status.APPROVED, item, booker, LocalDateTime.now(), LocalDateTime.now());
        var result = bookingRepository
                .findByBookerIdAndStatusEqualsOrderByStartDateDesc(booker.getId(), Status.WAITING,
                        PageRequest.of(0, 10, Sort.by(DESC, "created")));
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(booking.getId(), result.getContent().get(0).getId());
        Assertions.assertEquals(booking.getStatus(), result.getContent().get(0).getStatus());
    }

    @Test
    void testFindPendingItems() {
        var booking = createBooking(Status.WAITING, item, booker, LocalDateTime.now(), LocalDateTime.now());
        var result = bookingRepository.findPending(booker.getId(), Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(booking.getId(), result.getContent().get(0).getId());
        Assertions.assertEquals(booking.getStatus(), result.getContent().get(0).getStatus());
    }

    @Test
    void testFindOwnerPendingItems() {
        var booking = createBooking(Status.WAITING, item, booker, LocalDateTime.now(), LocalDateTime.now());
        var result = bookingRepository.findOwnerPending(item.getOwnerId(), Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(booking.getId(), result.getContent().get(0).getId());
        Assertions.assertEquals(booking.getStatus(), result.getContent().get(0).getStatus());
    }

    @Test
    void testFindCurrentForDate() {
        var start = LocalDateTime.now().plusDays(-1);
        var end = LocalDateTime.now().plusDays(1);
        var booking = createBooking(Status.WAITING, item, booker, start, end);
        var result = bookingRepository.findCurrentForDate(booker.getId(), LocalDateTime.now(), Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(booking.getId(), result.getContent().get(0).getId());
        Assertions.assertEquals(booking.getStatus(), result.getContent().get(0).getStatus());
    }

    @Test
    void testFindOwnerForDate() {
        var start = LocalDateTime.now().plusDays(-1);
        var end = LocalDateTime.now().plusDays(1);
        var booking = createBooking(Status.WAITING, item, booker, start, end);
        var result = bookingRepository.findOwnerCurrentForDate(item.getOwner().getId(), LocalDateTime.now(), Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(booking.getId(), result.getContent().get(0).getId());
        Assertions.assertEquals(booking.getStatus(), result.getContent().get(0).getStatus());
    }

    @Test
    void testFindCanceledItems() {
        var booking = createBooking(Status.CANCELED, item, booker, LocalDateTime.now(), LocalDateTime.now());
        var result = bookingRepository.findCanceled(booker.getId(), Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(booking.getId(), result.getContent().get(0).getId());
        Assertions.assertEquals(booking.getStatus(), result.getContent().get(0).getStatus());
    }

    @Test
    void testFindRejectedItems() {
        var booking = createBooking(Status.REJECTED, item, booker, LocalDateTime.now(), LocalDateTime.now());
        var result = bookingRepository.findCanceled(booker.getId(), Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(booking.getId(), result.getContent().get(0).getId());
        Assertions.assertEquals(booking.getStatus(), result.getContent().get(0).getStatus());
    }

    @Test
    void testFindOwnerCanceledItems() {
        var booking = createBooking(Status.CANCELED, item, booker, LocalDateTime.now(), LocalDateTime.now());
        var result = bookingRepository.findOwnerCanceled(item.getOwner().getId(), Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(booking.getId(), result.getContent().get(0).getId());
        Assertions.assertEquals(booking.getStatus(), result.getContent().get(0).getStatus());
    }

    @Test
    void testFindOwnerRejectedItems() {
        var booking = createBooking(Status.REJECTED, item, booker, LocalDateTime.now(), LocalDateTime.now());
        var result = bookingRepository.findOwnerCanceled(item.getOwner().getId(), Pageable.unpaged());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(booking.getId(), result.getContent().get(0).getId());
        Assertions.assertEquals(booking.getStatus(), result.getContent().get(0).getStatus());
    }

    @Test
    void testIsAvailableForBooking() {
        var start = LocalDateTime.now().plusDays(-2);
        var end = LocalDateTime.now().plusDays(2);
        var booking = createBooking(Status.APPROVED, item, booker, start, end);
        Assertions.assertTrue(bookingRepository.isAvailableForBooking(item.getId(), LocalDateTime.now().plusDays(-1), LocalDateTime.now().plusDays(1)));
        Assertions.assertFalse(bookingRepository.isAvailableForBooking(item.getId(), LocalDateTime.now().plusDays(-3), LocalDateTime.now().plusDays(3)));
    }

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}
