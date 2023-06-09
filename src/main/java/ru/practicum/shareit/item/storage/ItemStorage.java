package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item add(Item item);

    Item update(Item item);

    Item get(Long id);

    List<Item> getAll(Long userId);

    List<Item> search(String keyWord);

    void delete(Long id, Long userId);
}
