package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient client;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody @Valid UserCreateDto dto) {
        log.info("Create user: name='{}', email='{}'", dto.getName(), dto.getEmail());
        return client.create(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable @Positive long id) {
        log.info("Get user {}", id);
        return client.get(id);
    }

    @GetMapping
    public ResponseEntity<Object> list() {
        log.info("List users");
        return client.list();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> patch(@PathVariable @Positive long id,
                                        @RequestBody @Valid UserUpdateDto dto) {
        log.info("Patch user {}: name='{}', email='{}'", id, dto.getName(), dto.getEmail());
        return client.patch(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable @Positive long id) {
        log.info("Delete user {}", id);
        return client.delete(id);
    }
}