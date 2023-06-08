package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static ru.practicum.shareit.validation.ValidationGroups.Create;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
public class ItemDto {
    private Long id;
    @NotEmpty(groups = Create.class)
    private final String name;
    @NotEmpty(groups = Create.class)
    private final String description;
    @NotNull(groups = Create.class)
    private Boolean available;
    private final Long shareAmount;
}
