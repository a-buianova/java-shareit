package ru.practicum.shareit.user.model;

import lombok.*;

/**
 * Domain model for a user.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public final class User {

    /** Surrogate primary key. */
    @EqualsAndHashCode.Include
    private Long id;

    /** Display name of the user. */
    private String name;

    /** Email (must be unique, case-insensitive). */
    private String email;
}