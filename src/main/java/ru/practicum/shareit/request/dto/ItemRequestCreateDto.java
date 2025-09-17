package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating an item request.
 *
 * @param description description of the needed item (required, not blank)
 */
public record ItemRequestCreateDto(
        @NotBlank(message = "description is required")
        String description
) {}