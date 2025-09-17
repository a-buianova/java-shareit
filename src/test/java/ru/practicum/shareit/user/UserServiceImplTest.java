package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.common.exception.ConflictException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repo.InMemoryUserRepository;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserServiceImpl} using in-memory repository.
 */
@DisplayName("UserServiceImpl: unit tests (in-memory)")
class UserServiceImplTest {

    private InMemoryUserRepository repo;
    private UserMapper mapper;
    private UserService service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryUserRepository();
        mapper = new UserMapper(); // manual mapper
        service = new UserServiceImpl(repo, mapper);
    }

    @Test
    @DisplayName("create(): happy path")
    void create_success() {
        UserResponse u = service.create(new UserCreateDto("Alice", "a@ex.com"));

        assertNotNull(u.id());
        assertEquals("Alice", u.name());
        assertEquals("a@ex.com", u.email());
    }

    @Test
    @DisplayName("create(): 409 when email already exists")
    void create_conflictOnDuplicateEmail() {
        service.create(new UserCreateDto("Alice", "a@ex.com"));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> service.create(new UserCreateDto("Bob", "a@ex.com")),
                "Duplicate email must produce conflict"
        );
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    @DisplayName("get(): 404 when not found")
    void get_notFound() {
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.get(42L)
        );
        assertEquals("user not found", ex.getMessage());
    }

    @Test
    @DisplayName("list(): returns all")
    void list_success() {
        service.create(new UserCreateDto("A", "a@ex.com"));
        service.create(new UserCreateDto("B", "b@ex.com"));

        List<UserResponse> all = service.list();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("patch(): partial update (name only)")
    void patch_success() {
        Long id = service.create(new UserCreateDto("A", "a@ex.com")).id();

        UserResponse u = service.patch(id, new UserUpdateDto("AA", null));

        assertEquals("AA", u.name());
        assertEquals("a@ex.com", u.email());
    }

    @Test
    @DisplayName("delete(): removes user")
    void delete_success() {
        Long id = service.create(new UserCreateDto("A", "a@ex.com")).id();

        service.delete(id);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.get(id),
                "After deletion user must not exist"
        );
        assertEquals("user not found", ex.getMessage());
    }
}