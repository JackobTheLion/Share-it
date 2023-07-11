package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@DataJpaTest
public class BookingRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private User savedUser;
    private Item savedItem;
    private Booking booking1;
    private Booking booking2;
    private Booking savedBooking1;
    private Booking savedBooking2;
    private int from = 0;
    private int size = 10;
    private PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);

    @BeforeEach
    public void beforeEach() {
        User user = User.builder()
                .name("name")
                .email("email@email.com")
                .build();
        savedUser = userRepository.save(user);

        Item item = Item.builder()
                .ownerId(savedUser.getId())
                .name("item name")
                .description("description")
                .isAvailable(true)
                .build();
        savedItem = itemRepository.save(item);


    }

    @AfterEach
    public void afterEach() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}
