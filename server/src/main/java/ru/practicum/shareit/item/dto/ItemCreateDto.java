package ru.practicum.shareit.item.dto;

/**
 * Create-item request.
 */
public record ItemCreateDto(
        String name,
        String description,
        Boolean available,
        Long requestId
) {}