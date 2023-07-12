package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.item.dto.ItemInRequestDto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Data
@Builder
public class RequestDto {
    private Long id;

    @NotBlank(message = "Description cannot be empty.")
    private String description;

    @JsonProperty(access = WRITE_ONLY)
    private Long requesterId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime created;

    private List<ItemInRequestDto> items;
}
