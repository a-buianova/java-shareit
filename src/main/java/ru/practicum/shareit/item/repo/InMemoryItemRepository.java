package ru.practicum.shareit.item.repo;

import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @apiNote Thread-safe in-memory item repository with a secondary index by owner id.
 * @implNote
 * - Items are stored in a concurrent map; owner index kept in sync on save/update.
 * - Returns defensive copies (snapshots) to avoid accidental external mutation.
 */
public final class InMemoryItemRepository {

    private final ConcurrentHashMap<Long, Item> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<Item>> byOwner = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    /** @return persisted entity with generated id and owner index updated. */
    public Item save(Item i) {
        long id = seq.incrementAndGet();
        i.setId(id);
        store.put(id, i);

        Long ownerId = i.getOwner() != null ? i.getOwner().getId() : null;
        if (ownerId != null) {
            byOwner.computeIfAbsent(ownerId, k -> new CopyOnWriteArrayList<>()).add(i);
        }
        return i;
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    /** @return snapshot list of owner's items (never null). */
    public List<Item> findByOwner(Long ownerId) {
        return new ArrayList<>(byOwner.getOrDefault(ownerId, new CopyOnWriteArrayList<>()));
    }

    /**
     * @apiNote Full replace by id; maintains owner index if the owner changed.
     * @return updated entity or empty if not found.
     */
    public Optional<Item> update(Item i) {
        if (i == null || i.getId() == null) {
            return Optional.empty();
        }
        Item old = store.get(i.getId());
        if (old == null) {
            return Optional.empty();
        }

        store.put(i.getId(), i);

        Long oldOwnerId = old.getOwner() != null ? old.getOwner().getId() : null;
        Long newOwnerId = i.getOwner() != null ? i.getOwner().getId() : null;

        if (!Objects.equals(oldOwnerId, newOwnerId)) {
            if (oldOwnerId != null) {
                byOwner.getOrDefault(oldOwnerId, new CopyOnWriteArrayList<>())
                        .removeIf(it -> it.getId().equals(i.getId()));
            }
            if (newOwnerId != null) {
                byOwner.computeIfAbsent(newOwnerId, k -> new CopyOnWriteArrayList<>()).add(i);
            }
        }
        return Optional.of(i);
    }

    /**
     * @apiNote Case-insensitive search by name/description; returns only available items.
     * Blank or null query -> empty list.
     */
    public List<Item> searchAvailable(String text) {
        if (text == null) {
            return List.of();
        }
        String q = text.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            return List.of();
        }
        return store.values().stream()
                .filter(Item::isAvailable)
                .filter(i -> containsIgnoreCase(i.getName(), q) || containsIgnoreCase(i.getDescription(), q))
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String src, String needleLower) {
        if (src == null) return false;
        return src.toLowerCase(Locale.ROOT).contains(needleLower);
    }
}