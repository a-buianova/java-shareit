package ru.practicum.shareit.user.mapper;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

/**
 * Manual mapper for User <-> DTO.
 */
@Component
public final class UserMapper {

    public User toEntity(UserCreateDto dto) {
        return User.builder()
                .name(dto.name())
                .email(dto.email())
                .build();
    }

    public UserResponse toResponse(@Nullable User u) {
        if (u == null) return null;
        return new UserResponse(u.getId(), u.getName(), u.getEmail());
    }
}