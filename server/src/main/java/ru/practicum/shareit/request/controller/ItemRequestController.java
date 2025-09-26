package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.web.CurrentUserId;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

/**
 * REST controller for item requests.
 * Note: request-body validation lives in the gateway; here we only guard pagination.
 */
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestResponse create(@CurrentUserId Long userId,
                                      @RequestBody  ItemRequestCreateDto dto) {
        return service.create(userId, dto);
    }

    @GetMapping
    public List<ItemRequestResponse> findOwn(@CurrentUserId Long userId) {
        return service.findOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponse> findAllExceptUser(@CurrentUserId Long userId,
                                                       @RequestParam(defaultValue = "0") int from,
                                                       @RequestParam(defaultValue = "10") int size) {
        validatePage(from, size);
        return service.findAllExceptUser(userId, from, size);
    }

    @GetMapping("/{id}")
    public ItemRequestResponse getById(@CurrentUserId Long userId,
                                       @PathVariable("id") Long requestId) {
        return service.getById(userId, requestId);
    }

    // --- helpers ---
    private static void validatePage(int from, int size) {
        if (from < 0) throw new BadRequestException("from must be >= 0");
        if (size <= 0) throw new BadRequestException("size must be > 0");
    }
}