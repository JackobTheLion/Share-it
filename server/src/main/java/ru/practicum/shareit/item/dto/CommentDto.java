package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private String authorName;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime created;
}
