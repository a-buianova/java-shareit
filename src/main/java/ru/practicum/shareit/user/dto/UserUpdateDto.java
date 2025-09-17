package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;

/**
 * Request payload for updating user fields.
 * <p>All fields are optional; non-null values overwrite existing ones.</p>
 *
 * @param name  new name (nullable)
 * @param email new email (nullable, must be valid format)
 */
public record UserUpdateDto(
        String name,

        @Email(message = "email must be valid")
        String email
) {}