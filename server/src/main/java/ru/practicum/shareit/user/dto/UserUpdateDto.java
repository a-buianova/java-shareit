package ru.practicum.shareit.user.dto;

/**
 * Partial update payload for users.
 */
public record UserUpdateDto(
        String name,
        String email
) {}