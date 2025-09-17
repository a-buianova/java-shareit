package ru.practicum.shareit.user.repo;

import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @apiNote Thread-safe in-memory user repository with a case-insensitive email index.
 * @implNote
 * - Uses {@link ConcurrentHashMap} and {@link AtomicLong} for lock-free operations.
 * - Email keys are normalized to lower-case (ROOT) once and reused.
 */
public final class InMemoryUserRepository {

    private final ConcurrentHashMap<Long, User> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> emailIdx = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    /** @return persisted entity with generated id. */
    public User save(User u) {
        long id = seq.incrementAndGet();
        u.setId(id);
        store.put(id, u);
        String key = normalizeEmail(u.getEmail());
        if (key != null) {
            emailIdx.put(key, id);
        }
        return u;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    /** @return snapshot list (detached copy). */
    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    /** Case-insensitive check; null email -> false. */
    public boolean existsByEmail(String email) {
        String key = normalizeEmail(email);
        return key != null && emailIdx.containsKey(key);
    }

    /**
     * @apiNote Full replace by id; returns empty if entity absent.
     * @implNote Email index is not touched here (call {@link #reindexEmail(String, String, Long)} when changing email).
     */
    public Optional<User> update(User u) {
        if (u == null || u.getId() == null || !store.containsKey(u.getId())) {
            return Optional.empty();
        }
        store.put(u.getId(), u);
        return Optional.of(u);
    }

    /** @return true if an entity was removed. */
    public boolean delete(Long id) {
        User removed = store.remove(id);
        if (removed != null) {
            String key = normalizeEmail(removed.getEmail());
            if (key != null) {
                emailIdx.remove(key);
            }
            return true;
        }
        return false;
    }

    /**
     * @apiNote Rebuild email index for a particular user id.
     * @implNote Old email key is removed even if new email is null (index cleanup).
     */
    public void reindexEmail(String oldEmail, String newEmail, Long id) {
        String oldKey = normalizeEmail(oldEmail);
        if (oldKey != null) {
            emailIdx.remove(oldKey);
        }
        String newKey = normalizeEmail(newEmail);
        if (newKey != null) {
            emailIdx.put(newKey, id);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}