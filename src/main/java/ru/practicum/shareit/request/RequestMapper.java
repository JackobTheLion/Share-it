package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

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
        return RequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated().toLocalDateTime())
                .build();
    }
}
