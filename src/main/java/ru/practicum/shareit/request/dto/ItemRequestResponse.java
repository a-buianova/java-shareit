package ru.practicum.shareit.request.dto;

import java.time.LocalDateTime;

/**
 * API response for an item request.
 *
 * @param id          request ID
 * @param description request description
 * @param requestorId user ID of the requester
 * @param created     timestamp when the request was created
 */
public record ItemRequestResponse(
        Long id,
        String description,
        Long requestorId,
        LocalDateTime created
) {}