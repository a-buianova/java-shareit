package ru.practicum.shareit.request.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * Domain model for an item request.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public final class ItemRequest {

    /** Surrogate primary key. */
    @EqualsAndHashCode.Include
    private Long id;

    /** Free-text description of the requested item. */
    private String description;

    /** User who created the request. */
    private User requestor;

    /** Timestamp when the request was created. */
    private LocalDateTime created;
}