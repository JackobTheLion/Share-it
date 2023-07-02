package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Data
@Builder
public class RequestDto {
    private Long id;

    @NotEmpty(message = "Description cannot be empty.")
    private String description;

    @JsonProperty(access = WRITE_ONLY)
    private Long requesterId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime created;

    private List<ItemRequestDto> items;
}
