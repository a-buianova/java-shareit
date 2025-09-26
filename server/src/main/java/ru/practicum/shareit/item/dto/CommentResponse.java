package ru.practicum.shareit.item.dto;

import java.time.LocalDateTime;

/** API response for a comment (times are UTC). */
public record CommentResponse(
        Long id,
        String text,
        Long authorId,
        String authorName,
        LocalDateTime created
) {}