package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body for creating a comment. */
public record CommentCreateDto(
        @NotBlank String text
) {}