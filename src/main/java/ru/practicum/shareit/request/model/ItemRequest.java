package ru.practicum.shareit.request.model;

import lombok.Builder;
import lombok.Data;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder
public class ItemRequest {
    private final String name;
    private final String description;
}
