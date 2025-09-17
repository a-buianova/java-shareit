package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repo.InMemoryItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.InMemoryUserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ItemServiceImpl} using in-memory repositories.
 */
@DisplayName("ItemServiceImpl: unit tests (in-memory)")
class ItemServiceImplTest {

    private static final String DRILL = "Drill";
    private static final String DESC_600W = "600W";
    private static final String OWNER_EMAIL = "u@mail.com";

    private InMemoryItemRepository itemRepo;
    private InMemoryUserRepository userRepo;
    private ItemMapper mapper;
    private ItemService service;
    private Long existingOwnerId;

    @BeforeEach
    void setUp() {
        itemRepo = new InMemoryItemRepository();
        userRepo = new InMemoryUserRepository();
        mapper = new ItemMapper(); // manual mapper (no MapStruct)
        service = new ItemServiceImpl(itemRepo, userRepo, mapper);

        User owner = User.builder()
                .name("Owner")
                .email(OWNER_EMAIL)
                .build();
        userRepo.save(owner);
        existingOwnerId = owner.getId();
    }

    @Test
    @DisplayName("create(): throws 404 when owner does not exist")
    void create_ownerNotFound_throwsNotFound() {
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.create(999_999L, new ItemCreateDto(DRILL, DESC_600W, true, null)),
                "Expected 404-like exception if owner is absent"
        );
        assertEquals("owner not found", ex.getMessage());
    }

    @Test
    @DisplayName("create() + get() + listOwner(): happy path")
    void create_get_listOwner_success() {
        ItemResponse created = service.create(existingOwnerId, new ItemCreateDto(DRILL, DESC_600W, true, null));
        ItemResponse fetched = service.get(created.id());
        List<ItemResponse> ownerItems = service.listOwnerItems(existingOwnerId);

        assertAll(
                () -> assertNotNull(created.id(), "ID must be assigned"),
                () -> assertEquals(DRILL, fetched.name(), "get() must return created item"),
                () -> assertEquals(1, ownerItems.size(), "Owner must have exactly one item"),
                () -> assertEquals(created.id(), ownerItems.get(0).id(), "Same item expected")
        );
    }

    @Test
    @DisplayName("patch(): returns 403 when caller is not the owner")
    void patch_notOwner_forbidden() {
        User intruder = User.builder()
                .name("Intruder")
                .email("intruder@ex.com")
                .build();
        userRepo.save(intruder);
        Long intruderId = intruder.getId();

        ItemResponse created = service.create(existingOwnerId, new ItemCreateDto(DRILL, DESC_600W, true, null));

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> service.patch(intruderId, created.id(), new ItemUpdateDto("Hack", null, null)),
                "Only owner is allowed to patch the item"
        );
        assertEquals("forbidden: not an owner", ex.getMessage());
    }

    @Test
    @DisplayName("patch(): updates only non-null fields (partial update)")
    void patch_partialUpdate_success() {
        ItemResponse created = service.create(existingOwnerId, new ItemCreateDto(DRILL, DESC_600W, true, null));

        ItemUpdateDto patch = new ItemUpdateDto(null, "Updated description", null);
        ItemResponse updated = service.patch(existingOwnerId, created.id(), patch);

        assertAll(
                () -> assertEquals(DRILL, updated.name(), "Name must stay unchanged"),
                () -> assertEquals("Updated description", updated.description(), "Description must be updated"),
                () -> assertTrue(updated.available(), "Availability must stay unchanged (true)")
        );
    }

    @Test
    @DisplayName("search(): blank or null -> empty; filters out unavailable items; case-insensitive")
    void search_blankAndCaseInsensitive_andFiltersUnavailable() {
        service.create(existingOwnerId, new ItemCreateDto("Drill", "Hammer mode", true, null));
        service.create(existingOwnerId, new ItemCreateDto("Old DRILL", "Broken", false, null));

        assertTrue(service.search("   ").isEmpty(), "Blank query must return empty list");
        assertTrue(service.search(null).isEmpty(), "Null query must return empty list");

        List<ItemResponse> res = service.search("drill");

        assertAll(
                () -> assertEquals(1, res.size(), "Only available items must be returned"),
                () -> assertEquals("Drill", res.get(0).name(), "Must match available item, case-insensitive")
        );
    }

    @Test
    @DisplayName("get(): throws 404 when item not found")
    void get_notFound_throws404() {
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.get(123456L),
                "Expected 404-like exception for absent item"
        );
        assertEquals("item not found", ex.getMessage());
    }
}