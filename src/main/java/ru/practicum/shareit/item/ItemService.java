package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Component
@Slf4j
public class ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemService(ItemStorage itemStorage,
                       UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    public Item addItem(Item item) {
        log.info("Adding item {}", item);
        if (item.getName() == null || item.getName().isEmpty()) {
            log.info("Item name cannot be empty");
            throw new ValidationException("Item name cannot be empty");
        }
        userStorage.getUser(item.getOwnerId());
        itemStorage.addItem(item);
        log.info("Item added {}.", item);
        return item;
    }

    public Item updateItem(Item item) {
        log.info("Updating item with: {}", item);
        itemStorage.getItem(item.getId());
        userStorage.getUser(item.getOwnerId());
        Item updatedItem = itemStorage.updateItem(item);
        log.info("Item updated: {}", updatedItem);
        return updatedItem;
    }

    public List<Item> getAllItems(Long userId) {
        if (userId == null) {
            log.info("userId is null. Getting all items");
        } else {
            log.info("Getting all items of user id: {}", userId);
        }
        List<Item> items = itemStorage.getAllItems(userId);
        log.info("Number of items found: {}", items.size());
        return items;
    }

    public Item getItem(Long itemId) {
        log.info("Looking for item id {}", itemId);
        Item item = itemStorage.getItem(itemId);
        log.info("Item found: {}", item);
        return item;
    }

    public List<Item> searchItem(String text, Long userId) {
        log.info("Looking for item by key word: \"{}\". User id: {}", text, userId);
        List<Item> items = itemStorage.searchItem(text);
        log.info("Number of items found: {}", items.size());
        return items;
    }

    public void deleteItem(Long itemId, Long userId) {
        log.info("Deleting item id {} by user id {}", itemId, userId);
        itemStorage.deleteItem(itemId, userId);
    }
}
