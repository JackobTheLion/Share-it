package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoItem;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

import static ru.practicum.shareit.validation.ValidationGroups.Create;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {
    private Long id;
    @NotBlank(groups = Create.class, message = "Name cannot be empty.")
    private String name;
    @NotBlank(groups = Create.class, message = "Description cannot be empty.")
    private String description;
    @NotNull(groups = Create.class, message = "Available should be true or false")
    private Boolean available;
    private List<CommentDto> comments;
    private Long requestId;
    private BookingDtoItem lastBooking;
    private BookingDtoItem nextBooking;
}