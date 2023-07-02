package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDtoItem;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

import static ru.practicum.shareit.validation.ValidationGroups.Create;

@Data
@Builder
public class ItemDto {
    private Long id;

    @NotEmpty(groups = Create.class, message = "Name cannot be empty.")
    private final String name;

    @NotEmpty(groups = Create.class, message = "Description cannot be empty.")
    private final String description;

    @NotNull(groups = Create.class, message = "Available should be true or false")
    private Boolean available;

    private List<CommentDto> comments;

    private Long requestId;

    private BookingDtoItem lastBooking;

    private BookingDtoItem nextBooking;
}