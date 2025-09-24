package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request payload for creating a booking.
 * Temporal validation (start < end, availability, etc.)
 * is performed in the service layer.
 * Client must send times in format: yyyy-MM-dd'T'HH:mm:ss (UTC).
 */
public record BookingCreateDto(

        @Schema(description = "ID of the item to book", example = "42")
        @NotNull(message = "itemId is required")
        Long itemId,

        @Schema(description = "Booking start in UTC (ISO-8601 without zone)", example = "2030-01-01T10:00:00")
        @NotNull(message = "start is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime start,

        @Schema(description = "Booking end in UTC (ISO-8601 without zone)", example = "2030-01-01T12:00:00")
        @NotNull(message = "end is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime end
) {}