package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;

/**
 * Partial update payload for users.
 */
public record UserUpdateDto(
        String name,
        @Email String email
) {}