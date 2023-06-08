package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
public class Item {
    private Long id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;
    private Boolean isAvailable;
    private final Long ownerId;
}
