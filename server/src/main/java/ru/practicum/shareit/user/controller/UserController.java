package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * @apiNote REST controller for User CRUD operations.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    /**
     * @apiNote Create a user.
     * @return 201 Created with {@link UserResponse}.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody UserCreateDto dto) {
        return service.create(dto);
    }

    /**
     * @apiNote Get user by id.
     */
    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    /**
     * @apiNote List all users.
     */
    @GetMapping
    public List<UserResponse> list() {
        return service.list();
    }

    /**
     * @apiNote Patch user fields. Returns 409 if email becomes non-unique.
     */
    @PatchMapping("/{id}")
    public UserResponse patch(@PathVariable Long id,
                              @RequestBody UserUpdateDto dto) {
        return service.patch(id, dto);
    }

    /**
     * @apiNote Delete user by id.
     * @implNote Returns 204 No Content by convention.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}