package ru.practicum.shareit.common.error;

import java.time.Instant;

/**
 * @apiNote Standardized error response returned by the API.
 * @implNote Fields follow common HTTP error structure:
 * - timestamp – when the error was generated
 * - status – numeric HTTP status code
 * - error – short HTTP status reason phrase
 * - message – human-readable description of the problem
 * - path – request URI that caused the error
 */

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {}