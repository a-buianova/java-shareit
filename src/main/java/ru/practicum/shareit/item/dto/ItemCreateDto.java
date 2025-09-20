package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for creating an item.
 *
 * @param name        item name (required, not blank)
 * @param description item description (required, not blank)
 * @param available   whether the item is available (required)
 * @param requestId   optional link to an item request
 */
public record ItemCreateDto(
        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "description is required")
        String description,

        @NotNull(message = "available flag is required")
        Boolean available,

        Long requestId
) {}