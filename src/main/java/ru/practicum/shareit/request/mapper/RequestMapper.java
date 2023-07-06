package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.util.stream.Collectors;

public class RequestMapper {

    public static Request mapFromDto(RequestDto requestDto) {
        User requester = new User();
        requester.setId(requestDto.getRequesterId());
        return Request.builder()
                .description(requestDto.getDescription())
                .requester(requester)
                .build();
    }

    public static RequestDto mapToDto(Request request) {
        RequestDto requestDto = RequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated().toLocalDateTime())
                .build();
        if (request.getItems() != null) {
            requestDto.setItems(request.getItems().stream()
                    .map(ItemMapper::mapToItemRequestDto)
                    .collect(Collectors.toList()));
        }
        return requestDto;
    }
}
