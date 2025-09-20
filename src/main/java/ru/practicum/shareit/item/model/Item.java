package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

/**
 * Domain model for a shareable item.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public final class Item {

    /** Surrogate primary key. */
    @EqualsAndHashCode.Include
    private Long id;

    /** Display name of the item. */
    private String name;

    /** Human-readable description. */
    private String description;

    /** Whether the item is currently available for booking. */
    private boolean available;

    /** The owner of the item. */
    private User owner;

    /** Optional link to the originating item request (nullable). */
    private ItemRequest request;
}