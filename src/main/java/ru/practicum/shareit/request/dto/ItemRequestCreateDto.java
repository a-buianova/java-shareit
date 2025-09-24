package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;

/** Request payload for creating an item request. */
public record ItemRequestCreateDto(
        @NotBlank(message = "description is required")
        String description
) {}