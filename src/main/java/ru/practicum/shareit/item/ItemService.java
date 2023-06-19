package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemService(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    public Item addItem(Item item) {
        log.info("Adding item {}", item);
        if (item.getName() == null || item.getName().isEmpty()) {
            log.info("Item name cannot be empty");
            throw new ValidationException("Item name cannot be empty");
        }
        Optional<User> userOpt = userStorage.findById(item.getOwnerId());
        if (userOpt.isEmpty()) {
            log.info("Item id {} not found ", item.getId());
            throw new ItemNotFoundException(String.format("User id %s not found", item.getOwnerId()));
        }
        itemStorage.save(item);
        log.info("Item added {}.", item);
        return item;
    }

    public Item updateItem(Item item) {
        log.info("Updating item with: {}", item);
        try {
            Item savedItem = itemStorage.getById(item.getId());
            if (!savedItem.getOwnerId().equals(item.getOwnerId())) {
                log.info("Item {} does not belong to user {}.", item.getId(), item.getOwnerId());
                throw new ItemNotFoundException(String.format("Item id %s not found", item.getId()));
            }
            if (item.getName() != null) {
                savedItem.setName(item.getName());
            }
            if (item.getDescription() != null) {
                savedItem.setDescription(item.getDescription());
            }
            if (item.getIsAvailable() != null) {
                savedItem.setIsAvailable(item.getIsAvailable());
            }
            log.info("Item updated: {}", savedItem);
            return itemStorage.save(savedItem);
        } catch (EntityNotFoundException e) {
            log.info("Item id {} not found ", item.getId());
            throw new ItemNotFoundException(String.format("Item id %s not found", item.getId()));
        }
    }

    public List<Item> getAllItems(Long userId) {
        List<Item> items;
        if (userId == null) {
            log.info("userId is null. Getting all items");
            items = itemStorage.findAll();
        } else {
            log.info("Getting all items of user id: {}", userId);
            items = itemStorage.findAllByOwnerId(userId);
        }
        log.info("Number of items found: {}", items.size());
        return items;
    }

    public Item getItem(Long itemId) {
        try {
            log.info("Looking for item id {}", itemId);
            Item item = itemStorage.getById(itemId);
            log.info("Item found: {}", item);
            return item;
        } catch (EntityNotFoundException e) {
            log.info("Item id {} not found ", itemId);
            throw new ItemNotFoundException(String.format("Item id %s not found", itemId));
        }
    }

    public List<Item> searchItem(String text, Long userId) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        log.info("Looking for item by key word: \"{}\". User id: {}", text, userId);
        List<Item> items = itemStorage
                .findItemByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndIsAvailableIsTrue(text, text);
        log.info("Number of items found: {}", items.size());
        return items;
    }

    public void deleteItem(Long itemId, Long userId) {
        log.info("Deleting item id {} by user id {}", itemId, userId);
        Item savedItem = itemStorage.getById(itemId);
        if (!savedItem.getOwnerId().equals(itemId)) {
            log.info("Item {} does not belong to user {}.", itemId, userId);
            throw new ItemNotFoundException(String.format("Item id %s not found", itemId));
        }
        itemStorage.deleteById(itemId);
    }
}
