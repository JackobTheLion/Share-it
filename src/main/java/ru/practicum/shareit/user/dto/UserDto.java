package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.ValidationGroups.Create;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class UserDto {

    private Long id;

    @Email(message = "Email incorrect")
    @NotEmpty(groups = Create.class)
    private String email;

    @NotEmpty(groups = Create.class)
    private String name;
}
