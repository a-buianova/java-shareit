package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

/** Use-case API for items. */
public interface ItemService {

    ItemResponse create(Long ownerId, ItemCreateDto dto);

    ItemResponse get(Long id);

    List<ItemResponse> listOwnerItems(Long ownerId);

    ItemResponse patch(Long ownerId, Long itemId, ItemUpdateDto dto);

    List<ItemResponse> search(String text);
}