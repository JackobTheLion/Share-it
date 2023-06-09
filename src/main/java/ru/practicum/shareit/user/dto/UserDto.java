package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import static ru.practicum.shareit.validation.ValidationGroups.Create;

@Data
@Builder
public class UserDto {
    private long id;
    @Email(message = "Email incorrect")
    @NotEmpty(groups = Create.class)
    private String email;
    @NotEmpty(groups = Create.class)
    private String name;
}
