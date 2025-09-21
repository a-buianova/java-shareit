package ru.practicum.shareit.request.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;

/**
 * JPA entity for item requests.
 * <p>
 * Columns:
 * - description  — non-null text
 * - requestor_id — FK to users(id)
 * - created      — creation timestamp (set on persist)
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "item_requests")
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false) // <-- фикс: requestor_id
    @ToString.Exclude
    private User requestor;

    @Column(nullable = false, updatable = false)
    private Instant created;

    @PrePersist
    void prePersist() {
        if (created == null) {
            created = Instant.now();
        }
    }
}