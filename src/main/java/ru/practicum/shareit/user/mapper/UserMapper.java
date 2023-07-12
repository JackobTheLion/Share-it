package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;

public class UserMapper {
    public static User mapFromDto(UserRequestDto userRequestDto) {
        return User.builder()
                .id(userRequestDto.getId())
                .name(userRequestDto.getName())
                .email(userRequestDto.getEmail())
                .build();
    }

    public static User mapFromDto(UserRequestDto userRequestDto, Long userId) {
        return User.builder()
                .id(userId)
                .name(userRequestDto.getName())
                .email(userRequestDto.getEmail())
                .build();
    }

    public static UserResponseDto mapToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
