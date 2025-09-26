package ru.practicum.shareit.user.dto;

/**
 * Request payload for creating a new user.
 */
public record UserCreateDto(
        String name,
        String email
) {}