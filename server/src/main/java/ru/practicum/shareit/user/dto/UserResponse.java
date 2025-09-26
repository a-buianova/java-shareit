package ru.practicum.shareit.user.dto;

/**
 * API response for a user.
 */
public record UserResponse(
        Long id,
        String name,
        String email
) {}