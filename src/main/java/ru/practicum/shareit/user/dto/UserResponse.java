package ru.practicum.shareit.user.dto;

/**
 * API response for a user.
 *
 * @param id    user ID
 * @param name  user name
 * @param email user email
 */
public record UserResponse(
        Long id,
        String name,
        String email
) {}