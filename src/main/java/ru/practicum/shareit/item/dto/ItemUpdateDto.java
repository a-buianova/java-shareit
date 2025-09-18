package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for partial item update.
 * All fields are optional; non-null values overwrite existing ones.
 */
public record ItemUpdateDto(
        @Size(min = 1, max = 255, message = "name must be between 1 and 255 characters")
        String name,

        @Size(min = 1, max = 2000, message = "description must be between 1 and 2000 characters")
        String description,

        Boolean available
) {}