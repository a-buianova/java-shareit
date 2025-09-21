package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.web.CurrentUserId;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse create(@CurrentUserId Long userId,
                               @RequestBody @Valid ItemCreateDto dto) {
        return service.create(userId, dto);
    }

    // GET /items/{id} — заголовок НЕ обязателен (нужен для владельца, чтобы показать last/next)
    @GetMapping("/{itemId}")
    public ItemDetailsResponse get(@PathVariable Long itemId,
                                   @RequestHeader(value = USER_HEADER, required = false) Long requesterId) {
        return service.get(requesterId, itemId);
    }

    @GetMapping
    public List<ItemDetailsResponse> listOwner(@CurrentUserId Long userId) {
        return service.listOwnerItems(userId);
    }

    @PatchMapping("/{itemId}")
    public ItemResponse patch(@CurrentUserId Long userId,
                              @PathVariable Long itemId,
                              @RequestBody @Valid ItemUpdateDto dto) {
        return service.patch(userId, itemId, dto);
    }

    @GetMapping("/search")
    public List<ItemResponse> search(@RequestParam String text) {
        return service.search(text);
    }

    /** POST /items/{itemId}/comment — add a comment (requires a past APPROVED booking). */
    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED) // 201 как в коллекции
    public CommentResponse addComment(@CurrentUserId Long userId,
                                      @PathVariable Long itemId,
                                      @RequestBody @Valid CommentCreateDto dto) {
        return service.addComment(userId, itemId, dto);
    }
}