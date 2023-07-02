package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;
    @NotBlank
    private String text;
    private String authorName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime created;
}
