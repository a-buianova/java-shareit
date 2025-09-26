package ru.practicum.shareit.item.dto;

/**
 * Partial update (nullable fields overwrite existing ones).
 */
public record ItemUpdateDto(
        String name,
        String description,
        Boolean available
) {}