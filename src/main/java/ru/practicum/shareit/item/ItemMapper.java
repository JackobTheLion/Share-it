package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {

    public static Item mapFromDto(ItemDto itemDto, Long itemId, Long userId) {
        return Item.builder()
                .name(itemDto.getName())
                .id(itemId)
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getAvailable())
                .ownerId(userId)
                .build();
    }

    public static Item mapFromDto(ItemDto itemDto, Long userId) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getAvailable())
                .ownerId(userId)
                .build();
    }

    public static ItemDto mapToDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .build();
    }
}
