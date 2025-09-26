package ru.practicum.shareit.item.dto;

/** Request body for creating a comment. */
public record CommentCreateDto(
        String text
) {}