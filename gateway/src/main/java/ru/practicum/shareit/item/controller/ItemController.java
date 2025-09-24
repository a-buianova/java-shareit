package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient client;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @RequestBody @Valid ItemCreateDto dto
    ) {
        log.info("Create item ownerId={}, requestId={}", ownerId, dto.requestId());
        return client.create(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @PathVariable long itemId,
            @RequestBody @Valid ItemUpdateDto dto
    ) {
        log.info("Patch item id={}, ownerId={}", itemId, ownerId);
        return client.update(ownerId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long itemId
    ) {
        log.info("Get item id={}, userId={}", itemId, userId);
        return client.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> listOwnerItems(
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("List owner items ownerId={}, from={}, size={}", ownerId, from, size);
        return client.listOwnerItems(ownerId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam String text,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        if (text == null || text.trim().isEmpty()) {
            log.info("Search with blank text -> returning [] without calling server");
            return ResponseEntity.ok().body(java.util.List.of());
        }
        log.info("Search items userId={}, text='{}', from={}, size={}", userId, text, from, size);
        return client.search(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long itemId,
            @RequestBody @Valid CommentCreateDto dto
    ) {
        log.info("Add comment to item id={}, userId={}", itemId, userId);
        return client.addComment(userId, itemId, dto);
    }
}
