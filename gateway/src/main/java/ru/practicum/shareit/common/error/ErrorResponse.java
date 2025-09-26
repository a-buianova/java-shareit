package ru.practicum.shareit.common.error;

import java.time.Instant;

/** Unified error payload for Gateway (mirrors the server response). */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {}
