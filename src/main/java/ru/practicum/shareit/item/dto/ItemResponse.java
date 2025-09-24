package ru.practicum.shareit.item.dto;

/**
 * Item API response.
 */
public record ItemResponse(
        Long id,
        String name,
        String description,
        Boolean available
) {}