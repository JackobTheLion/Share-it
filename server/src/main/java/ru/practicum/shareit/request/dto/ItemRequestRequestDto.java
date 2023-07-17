package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemInRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestRequestDto {
    private Long id;
    private String description;
    @JsonProperty(access = WRITE_ONLY)
    private Long requesterId;
    private LocalDateTime created = LocalDateTime.now();
    private List<ItemInRequestDto> items;
}
