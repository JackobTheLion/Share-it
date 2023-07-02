package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.model.Item;

import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingMapper.mapToDtoItem;

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
        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .build();

        if (item.getComments() != null) {
            itemDto.setComments(item.getComments().stream().map(CommentMapper::mapToDto).collect(Collectors.toList()));
        }

        if (item.getLastBooking() != null) {
            itemDto.setLastBooking(mapToDtoItem(item.getLastBooking()));
        }
        if (item.getNextBooking() != null) {
            itemDto.setNextBooking(mapToDtoItem(item.getNextBooking()));
        }
        return itemDto;
    }

    public static ItemRequestDto mapToItemRequestDto(Item item) {
        return ItemRequestDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwnerId())
                .build();
    }
}