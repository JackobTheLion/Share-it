package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.item.dto.ItemInRequestDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemRequestResponseDto {
    private Long id;
    private String description;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime created;
    private List<ItemInRequestDto> items;
}
