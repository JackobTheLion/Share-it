package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.util.stream.Collectors;

public class RequestMapper {

    public static Request mapFromDto(ItemRequestRequestDto itemRequestRequestDto) {
        User requester = new User();
        requester.setId(itemRequestRequestDto.getRequesterId());
        return Request.builder()
                .description(itemRequestRequestDto.getDescription())
                .requester(requester)
                .build();
    }

    public static ItemRequestResponseDto mapToDto(Request request) {
        ItemRequestResponseDto itemRequestRequestDto = ItemRequestResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated().toLocalDateTime())
                .build();
        if (request.getItems() != null) {
            itemRequestRequestDto.setItems(request.getItems().stream()
                    .map(ItemMapper::mapToItemRequestDto)
                    .collect(Collectors.toList()));
        }
        return itemRequestRequestDto;
    }
}
