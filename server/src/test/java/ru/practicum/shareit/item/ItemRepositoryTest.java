package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private User savedUser1;
    private User savedUser2;
    private Item item1;
    private Item item2;
    private int from = 0;
    private int size = 10;
    private PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);

    @BeforeEach
    public void addItem() {
        User user1 = User.builder()
                .name("name")
                .email("email@email.com")
                .build();
        User user2 = User.builder()
                .name("other name")
                .email("otheremail@email.com")
                .build();
        savedUser1 = userRepository.save(user1);
        savedUser2 = userRepository.save(user2);

        item1 = Item.builder()
                .name("name")
                .description("description")
                .isAvailable(true)
                .ownerId(savedUser1.getId())
                .build();
        item2 = Item.builder()
                .name("other name")
                .description("other description")
                .isAvailable(false)
                .ownerId(savedUser2.getId())
                .build();
        itemRepository.save(item1);
        itemRepository.save(item2);
    }

    @AfterEach
    public void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    public void findAllByOwnerId_Normal() {
        Item expectedItem = Item.builder()
                .name(item1.getName())
                .description(item1.getDescription())
                .isAvailable(item1.getIsAvailable())
                .ownerId(item1.getOwnerId())
                .build();

        List<Item> savedItems = itemRepository.findAllByOwnerIdOrderById(savedUser1.getId(), page).getContent();

        assertEquals(1, savedItems.size());
        assertEquals(expectedItem.getName(), savedItems.get(0).getName());
        assertEquals(expectedItem.getDescription(), savedItems.get(0).getDescription());
        assertEquals(expectedItem.getIsAvailable(), savedItems.get(0).getIsAvailable());
        assertEquals(expectedItem.getOwnerId(), savedItems.get(0).getOwnerId());
    }


    @Test
    public void findAllByOwnerId_Empty() {
        List<Item> savedItems = itemRepository.findAllByOwnerIdOrderById(999L, page).getContent();

        assertTrue(savedItems.isEmpty());
    }

    @Test
    public void findItem_Normal() {
        Item expectedItem = Item.builder()
                .name(item2.getName())
                .description(item2.getDescription())
                .isAvailable(item2.getIsAvailable())
                .ownerId(item2.getOwnerId())
                .build();

        String text = "oth"; // "other"

        List<Item> savedItems = itemRepository
                .findItemByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndIsAvailableIsTrue(text, text, page)
                .getContent();

        assertEquals(1, savedItems.size());
        assertEquals(1, savedItems.size());
        assertEquals(expectedItem.getName(), savedItems.get(0).getName());
        assertEquals(expectedItem.getDescription(), savedItems.get(0).getDescription());
        assertEquals(expectedItem.getIsAvailable(), savedItems.get(0).getIsAvailable());
        assertEquals(expectedItem.getOwnerId(), savedItems.get(0).getOwnerId());
    }
}
