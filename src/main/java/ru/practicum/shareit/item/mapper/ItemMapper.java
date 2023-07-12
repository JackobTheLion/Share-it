package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;

import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.mapper.BookingMapper.mapToDtoItem;

public class ItemMapper {

    public static Item mapFromDto(ItemDto itemDto, Long itemId, Long userId) {
        Item item = Item.builder()
                .name(itemDto.getName())
                .id(itemId)
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getAvailable())
                .ownerId(userId)
                .build();
        if (itemDto.getRequestId() != null) {
            Request request = new Request();
            request.setId(itemDto.getRequestId());
            item.setRequest(request);
        }
        return item;
    }

    public static Item mapFromDto(ItemDto itemDto, Long userId) {
        Item item = Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getAvailable())
                .ownerId(userId)
                .build();
        if (itemDto.getRequestId() != null) {
            Request request = new Request();
            request.setId(itemDto.getRequestId());
            item.setRequest(request);
        }
        return item;
    }

    public static ItemResponseDto mapToDto(Item item) {
        ItemResponseDto itemDto = ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .build();

        if (item.getComments() != null) {
            itemDto.setComments(item.getComments().stream()
                    .map(CommentMapper::mapToDto)
                    .collect(Collectors.toList()));
        }

        if (item.getRequest() != null) {
            itemDto.setRequestId(item.getRequest().getId());
        }

        if (item.getLastBooking() != null) {
            itemDto.setLastBooking(mapToDtoItem(item.getLastBooking()));
        }
        if (item.getNextBooking() != null) {
            itemDto.setNextBooking(mapToDtoItem(item.getNextBooking()));
        }
        return itemDto;
    }

    public static ItemInRequestDto mapToItemRequestDto(Item item) {
        return ItemInRequestDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwnerId())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .requestId(item.getRequest().getId())
                .build();
    }
}