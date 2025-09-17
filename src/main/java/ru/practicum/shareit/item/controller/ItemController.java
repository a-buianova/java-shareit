package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.web.CurrentUserId;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * @apiNote REST controller for Item CRUD and search.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;

    /**
     * @apiNote Create an item owned by the current user.
     * @return 201 Created with {@link ItemResponse}.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse create(@CurrentUserId Long userId, @RequestBody @Valid ItemCreateDto dto) {
        return service.create(userId, dto);
    }

    /**
     * @apiNote Get item by id.
     */
    @GetMapping("/{itemId}")
    public ItemResponse get(@PathVariable Long itemId) {
        return service.get(itemId);
    }

    /**
     * @apiNote List items owned by the current user.
     */
    @GetMapping
    public List<ItemResponse> listOwner(@CurrentUserId Long userId) {
        return service.listOwnerItems(userId);
    }

    /**
     * @apiNote Partially update an item; only owner is allowed.
     */
    @PatchMapping("/{itemId}")
    public ItemResponse patch(@CurrentUserId Long userId,
                              @PathVariable Long itemId,
                              @RequestBody @Valid ItemUpdateDto dto) {
        return service.patch(userId, itemId, dto);
    }

    /**
     * @apiNote Search available items by text (case-insensitive). Blank → empty.
     */
    @GetMapping("/search")
    public List<ItemResponse> search(@RequestParam String text) {
        return service.search(text);
    }
}