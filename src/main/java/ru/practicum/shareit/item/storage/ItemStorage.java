package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item addItem(Item item);

    Item updateItem(Item item);

    Item getItem(Long id);

    List<Item> getAllItems(Long userId);

    List<Item> searchItem(String keyWord);

    void deleteItem(Long id, Long userId);
}
