package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemRequestDto {
    private Long id;
    private String name;
    private Long ownerId;
    private String description;
    private Boolean available;
    private Long requestId;
}
