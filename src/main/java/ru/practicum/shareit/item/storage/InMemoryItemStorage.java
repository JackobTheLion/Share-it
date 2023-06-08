package ru.practicum.shareit.item.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.exceptions.NoRightsException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Qualifier("inMem")
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();
    private Long id = 0L;

    @Override
    public Item addItem(Item item) {
        item.setId(getId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        Item savedItem = items.get(item.getId());
        if (savedItem == null) {
            throw new ItemNotFoundException(String.format("Item id %s not found", item.getId()));
        }
        if (savedItem.getOwnerId() != item.getOwnerId()) {
            throw new NoRightsException(String.format("User id %s cannot update item id %s", item.getOwnerId(), item.getId()));
        }
        if (item.getName() != null) {
            savedItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            savedItem.setDescription(item.getDescription());
        }
        if (item.getIsAvailable() != null && !item.getIsAvailable().equals(savedItem.getIsAvailable())) {
            savedItem.setIsAvailable(item.getIsAvailable());
        }
        return savedItem;
    }

    @Override
    public Item getItem(Long id) {
        Item item = items.get(id);
        if (item == null) {
            throw new ItemNotFoundException(String.format("Item id %s not found", id));
        }
        return item;
    }

    @Override
    public List<Item> getAllItems(Long userId) {
        List<Item> result;
        if (userId == null) {
            result = items.values().stream()
                    .collect(Collectors.toList());
        } else {
            result = items.values().stream()
                    .filter(i -> i.getOwnerId().equals(userId))
                    .collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<Item> searchItem(String keyWord) {
        List<Item> result = new ArrayList<>();
        if (keyWord != null && keyWord.trim().isEmpty()) {
            return result;
        }
        result = items.values().stream()
                .filter(item -> (item.getName().toLowerCase().contains(keyWord.toLowerCase())
                        || item.getDescription().toLowerCase().contains(keyWord.toLowerCase()))
                        && item.getIsAvailable())
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public void deleteItem(Long id, Long userId) {
        Item savedItem = items.get(id);
        if (savedItem == null) {
            throw new ItemNotFoundException(String.format("Item id %s not found", id));
        }
        if (savedItem.getOwnerId() != userId) {
            throw new NoRightsException(String.format("User id %s cannot update item id %s", userId, id));
        }
        items.remove(id);
    }

    private Long getId() {
        id++;
        return id;
    }
}
