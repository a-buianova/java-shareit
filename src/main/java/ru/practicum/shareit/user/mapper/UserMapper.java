package ru.practicum.shareit.user.mapper;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

/**
 * @apiNote Manual mapper for converting between {@link User} entities and DTOs.
 * @implNote Chosen instead of MapStruct to keep build simple and explicit in sprint 14.
 */
@Component
public final class UserMapper {

    /** Convert create DTO into a new {@link User} entity. */
    public User toEntity(UserCreateDto dto) {
        return User.builder()
                .name(dto.name())
                .email(dto.email())
                .build();
    }

    /** Convert entity into response DTO. */
    public UserResponse toResponse(@Nullable User u) {
        if (u == null) return null;
        return new UserResponse(u.getId(), u.getName(), u.getEmail());
    }

    /** Apply patch update: only non-null fields overwrite entity values. */
    public void patch(User target, UserUpdateDto dto) {
        if (dto.name() != null) target.setName(dto.name());
        if (dto.email() != null) target.setEmail(dto.email());
    }
}