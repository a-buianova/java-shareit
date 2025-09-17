package ru.practicum.shareit.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.InMemoryUserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InMemoryUserRepository}.
 */
@DisplayName("InMemoryUserRepository: unit tests")
class InMemoryUserRepositoryTest {

    @Test
    @DisplayName("save() + findById() + update() + delete(): happy path")
    void save_find_update_delete_success() {
        InMemoryUserRepository repo = new InMemoryUserRepository();

        User u = new User(null, "Alice", "a@ex.com");
        repo.save(u);

        assertNotNull(u.getId(), "ID must be assigned on save");
        Optional<User> found = repo.findById(u.getId());
        assertTrue(found.isPresent(), "Saved user must be found by id");

        u.setName("Alice2");
        repo.update(u);

        assertEquals("Alice2", repo.findById(u.getId()).orElseThrow().getName());

        assertTrue(repo.delete(u.getId()), "Delete must return true when user existed");
        assertFalse(repo.findById(u.getId()).isPresent(), "User must be removed");
    }

    @Test
    @DisplayName("existsByEmail(): case-insensitive check")
    void existsByEmail_caseInsensitive() {
        InMemoryUserRepository repo = new InMemoryUserRepository();
        repo.save(new User(null, "Bob", "Bob@Ex.com"));

        assertTrue(repo.existsByEmail("bob@ex.com"), "Email lookup must be case-insensitive");
    }
}