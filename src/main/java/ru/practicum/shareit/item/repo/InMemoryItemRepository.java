package ru.practicum.shareit.item.repo;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory item repository with a secondary index by owner id.
 * <p>
 * - Items are stored in a concurrent map; owner index kept in sync on save/update.
 * - Returns defensive copies (snapshots) to avoid accidental external mutation.
 */
@Repository("itemRepository")
public final class InMemoryItemRepository {

    private final ConcurrentHashMap<Long, Item> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<Item>> byOwner = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    /**
     * Persist a copy of the given item and assign a generated id.
     */
    public Item save(Item src) {
        long id = seq.incrementAndGet();
        Item persisted = copy(src, id);
        store.put(id, persisted);

        Long ownerId = persisted.getOwner() != null ? persisted.getOwner().getId() : null;
        if (ownerId != null) {
            byOwner.computeIfAbsent(ownerId, k -> new CopyOnWriteArrayList<>()).add(persisted);
        }
        return persisted;
    }

    /**
     * Find by id; returns a defensive copy if present.
     */
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(store.get(id)).map(s -> copy(s, null));
    }

    /**
     * List items by owner; returns an immutable snapshot (may be empty).
     */
    public List<Item> findByOwner(Long ownerId) {
        List<Item> bucket = byOwner.get(ownerId);
        if (bucket == null || bucket.isEmpty()) {
            return Collections.emptyList();
        }

        List<Item> out = new ArrayList<>(bucket.size());
        for (Item it : bucket) {
            out.add(copy(it, null));
        }
        return out;
    }

    /**
     * Replace item by id and keep owner index consistent.
     *
     * @return updated entity (never {@code null})
     * @throws NoSuchElementException if the entity does not exist
     */
    public Item update(Item i) {
        if (i == null || i.getId() == null) {
            throw new NoSuchElementException("item not found");
        }
        Item old = store.get(i.getId());
        if (old == null) {
            throw new NoSuchElementException("item not found");
        }

        store.put(i.getId(), i);

        Long oldOwnerId = old.getOwner() != null ? old.getOwner().getId() : null;
        Long newOwnerId = i.getOwner() != null ? i.getOwner().getId() : null;

        if (!Objects.equals(oldOwnerId, newOwnerId)) {
            if (oldOwnerId != null) {
                List<Item> oldBucket = byOwner.get(oldOwnerId);
                if (oldBucket != null) {
                    oldBucket.removeIf(it -> it.getId().equals(i.getId()));
                }
            }
            if (newOwnerId != null) {
                byOwner.computeIfAbsent(newOwnerId, k -> new CopyOnWriteArrayList<>()).add(i);
            }
        } else if (newOwnerId != null) {
            List<Item> bucket = byOwner.get(newOwnerId);
            if (bucket != null) {
                for (int idx = 0; idx < bucket.size(); idx++) {
                    if (Objects.equals(bucket.get(idx).getId(), i.getId())) {
                        bucket.set(idx, i);
                        break;
                    }
                }
            }
        }
        return i;
    }

    /**
     * Case-insensitive search by name/description; returns only available items.
     * Assumes {@code text} is validated upstream (non-null, non-blank).
     */
    public List<Item> searchAvailable(String text) {
        String q = text.toLowerCase(Locale.ROOT);
        return store.values().stream()
                .filter(Item::isAvailable)
                .filter(i -> containsIgnoreCase(i.getName(), q) || containsIgnoreCase(i.getDescription(), q))
                .map(s -> copy(s, null))
                .collect(Collectors.toList());
    }

    // ---- helpers ----

    private boolean containsIgnoreCase(String src, String needleLower) {
        return src != null && src.toLowerCase(Locale.ROOT).contains(needleLower);
    }

    private Item copy(Item s, Long idOverride) {
        if (s == null) return null;
        return Item.builder()
                .id(idOverride != null ? idOverride : s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .available(s.isAvailable())
                .owner(s.getOwner())
                .request(s.getRequest())
                .build();
    }
}