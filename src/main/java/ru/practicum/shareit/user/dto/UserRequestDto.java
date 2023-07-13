package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.ValidationGroups.Create;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class UserRequestDto {

    private Long id;

    @Email(message = "Email incorrect")
    @NotBlank(groups = Create.class)
    private String email;

    @NotBlank(groups = Create.class)
    private String name;
}
