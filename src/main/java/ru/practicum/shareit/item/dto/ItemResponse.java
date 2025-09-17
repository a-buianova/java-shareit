package ru.practicum.shareit.item.dto;

/**
 * Response payload for item endpoints.
 *
 * @param id          item ID
 * @param name        item name
 * @param description item description
 * @param available   availability flag
 */
public record ItemResponse(
        Long id,
        String name,
        String description,
        Boolean available
) {}