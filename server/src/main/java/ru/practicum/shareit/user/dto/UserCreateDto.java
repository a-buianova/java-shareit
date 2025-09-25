package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating a new user.
 */
public record UserCreateDto(
        @NotBlank String name,
        @NotBlank @Email String email
) {}