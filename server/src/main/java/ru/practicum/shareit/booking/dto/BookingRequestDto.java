package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Data
@Builder
public class BookingRequestDto {
    private Long id;

    @JsonProperty(access = WRITE_ONLY)
    private Long itemId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime start;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime end;

    private ItemResponseDto item;

    private UserResponseDto booker;

    private Status status;
}
