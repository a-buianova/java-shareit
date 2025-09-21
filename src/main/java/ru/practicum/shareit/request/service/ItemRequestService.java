package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;

import java.util.List;

/** Use-case API for item requests. */
public interface ItemRequestService {

    ItemRequestResponse create(Long userId, ItemRequestCreateDto dto);

    List<ItemRequestResponse> findOwn(Long userId);

    List<ItemRequestResponse> findAllExceptUser(Long userId, int from, int size);

    ItemRequestResponse getById(Long userId, Long requestId);
}