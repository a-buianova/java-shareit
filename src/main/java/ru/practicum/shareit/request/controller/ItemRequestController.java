package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.web.CurrentUserId;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

/**
 * REST controller for item requests.
 */
@Validated
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestResponse create(@CurrentUserId Long userId,
                                      @RequestBody @Valid ItemRequestCreateDto dto) {
        return service.create(userId, dto);
    }

    @GetMapping
    public List<ItemRequestResponse> findOwn(@CurrentUserId Long userId) {
        return service.findOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponse> findAllExceptUser(@CurrentUserId Long userId,
                                                       @RequestParam(defaultValue = "0")
                                                       @PositiveOrZero int from,
                                                       @RequestParam(defaultValue = "10")
                                                       @Positive int size) {
        return service.findAllExceptUser(userId, from, size);
    }

    @GetMapping("/{id}")
    public ItemRequestResponse getById(@CurrentUserId Long userId,
                                       @PathVariable("id") Long requestId) {
        return service.getById(userId, requestId);
    }
}