package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Size;

public record ItemUpdateDto(
        @Size(max = 255)
        String name,
        String description,
        Boolean available
) {}