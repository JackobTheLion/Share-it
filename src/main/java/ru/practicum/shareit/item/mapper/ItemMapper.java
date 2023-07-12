package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemInRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;

import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.mapper.BookingMapper.mapToDtoItem;

public class ItemMapper {

    public static Item mapFromDto(ItemRequestDto itemRequestDto, Long itemId, Long userId) {
        Item item = Item.builder()
                .name(itemRequestDto.getName())
                .id(itemId)
                .description(itemRequestDto.getDescription())
                .isAvailable(itemRequestDto.getAvailable())
                .ownerId(userId)
                .build();
        if (itemRequestDto.getRequestId() != null) {
            Request request = new Request();
            request.setId(itemRequestDto.getRequestId());
            item.setRequest(request);
        }
        return item;
    }

    public static Item mapFromDto(ItemRequestDto itemRequestDto, Long userId) {
        Item item = Item.builder()
                .name(itemRequestDto.getName())
                .description(itemRequestDto.getDescription())
                .isAvailable(itemRequestDto.getAvailable())
                .ownerId(userId)
                .build();
        if (itemRequestDto.getRequestId() != null) {
            Request request = new Request();
            request.setId(itemRequestDto.getRequestId());
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