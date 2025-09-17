package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating a new user.
 *
 * @param name  user display name (required, not blank)
 * @param email unique user email (required, valid format)
 */
public record UserCreateDto(
        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email
) {}
