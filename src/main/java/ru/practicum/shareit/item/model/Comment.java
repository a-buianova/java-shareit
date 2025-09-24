package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;

/**
 * JPA entity representing a user comment for an {@link Item}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(name = "idx_comments_item", columnList = "item_id"),
                @Index(name = "idx_comments_author", columnList = "author_id"),
                @Index(name = "idx_comments_created", columnList = "created")
        }
)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    @ToString.Exclude
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    private User author;

    @Column(nullable = false, updatable = false)
    private Instant created;

    @PrePersist
    private void onCreate() {
        if (created == null) {
            created = Instant.now();
        }
    }
}