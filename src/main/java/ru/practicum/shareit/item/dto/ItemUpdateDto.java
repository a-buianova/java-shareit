package ru.practicum.shareit.item.dto;

/**
 * Request payload for partially updating an item.
 * <p>All fields are optional; non-null values overwrite existing ones.</p>
 *
 * @param name        new name (nullable)
 * @param description new description (nullable)
 * @param available   new availability flag (nullable)
 */
public record ItemUpdateDto(
        String name,
        String description,
        Boolean available
) {}