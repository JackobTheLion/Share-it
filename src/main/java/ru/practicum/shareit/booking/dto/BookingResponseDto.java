package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponseDto {
    private Long id;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime start;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime end;
    private ItemResponseDto item;
    private UserResponseDto booker;
    private Status status;
}
