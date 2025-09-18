package ru.practicum.shareit.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.InMemoryItemRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InMemoryItemRepository}.
 */
@DisplayName("InMemoryItemRepository: unit tests")
class InMemoryItemRepositoryTest {

    @Test
    @DisplayName("save() + findById() + update(): happy path; save() does not mutate input")
    void save_find_update_success() {
        InMemoryItemRepository repo = new InMemoryItemRepository();
        User owner = new User(1L, "Alice", "a@ex.com");
        Item item = Item.builder()
                .name("Drill")
                .description("Strong 600W")
                .available(true)
                .owner(owner)
                .build();

        Item persisted = repo.save(item);

        assertNull(item.getId(), "Original item must not be mutated by save()");
        assertNotNull(persisted.getId(), "ID must be assigned on save (returned instance)");

        Optional<Item> fetchedOpt = repo.findById(persisted.getId());
        assertTrue(fetchedOpt.isPresent(), "Saved item must be found by id");

        persisted.setDescription("Updated");
        repo.update(persisted);

        Item afterUpdate = repo.findById(persisted.getId()).orElseThrow();
        assertEquals("Updated", afterUpdate.getDescription(), "Description must be updated");
    }

    @Test
    @DisplayName("findByOwner(): returns only owner items")
    void findByOwner_filtersByOwner() {
        InMemoryItemRepository repo = new InMemoryItemRepository();
        User alice = new User(1L, "Alice", "a@ex.com");
        User bob = new User(2L, "Bob", "b@ex.com");

        repo.save(Item.builder().name("A1").description("x").available(true).owner(alice).build());
        repo.save(Item.builder().name("A2").description("y").available(true).owner(alice).build());
        repo.save(Item.builder().name("B1").description("z").available(true).owner(bob).build());

        List<Item> aliceItems = repo.findByOwner(alice.getId());
        List<Item> bobItems = repo.findByOwner(bob.getId());

        assertEquals(2, aliceItems.size(), "Alice must have 2 items");
        assertEquals(1, bobItems.size(), "Bob must have 1 item");
    }

    @Test
    @DisplayName("searchAvailable(): case-insensitive and filters unavailable (null/blank handled at service)")
    void searchAvailable_caseInsensitive_filtersUnavailable() {
        InMemoryItemRepository repo = new InMemoryItemRepository();
        User owner = new User(1L, "Alice", "a@ex.com");

        repo.save(Item.builder().name("Drill").description("Hammer mode").available(true).owner(owner).build());
        repo.save(Item.builder().name("Old DRILL").description("Broken").available(false).owner(owner).build());

        List<Item> found = repo.searchAvailable("drill");

        assertEquals(1, found.size(), "Only available item should be returned");
        assertEquals("Drill", found.get(0).getName(), "Must match available item ignoring case");
    }
}