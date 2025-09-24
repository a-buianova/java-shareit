package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestBody @Valid ItemRequestCreateDto dto
    ) {
        log.info("Create item request by userId={}", userId);
        return requestClient.create(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> findOwn(
            @RequestHeader("X-Sharer-User-Id") long userId
    ) {
        log.info("List own item requests userId={}", userId);
        return requestClient.findOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllExceptUser(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        log.info("List requests of others userId={}, from={}, size={}", userId, from, size);
        return requestClient.findAllExceptUser(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable("id") long id
    ) {
        log.info("Get item request id={} by userId={}", id, userId);
        return requestClient.getById(userId, id);
    }
}