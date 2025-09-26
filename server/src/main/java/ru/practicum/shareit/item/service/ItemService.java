package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

/** Use-case API for managing items. */
public interface ItemService {

    ItemResponse create(Long ownerId, ItemCreateDto dto);

    ItemDetailsResponse get(Long requesterId, Long itemId);

    List<ItemDetailsResponse> listOwnerItems(Long ownerId);

    ItemResponse patch(Long ownerId, Long itemId, ItemUpdateDto dto);

    List<ItemResponse> search(String text);

    CommentResponse addComment(Long userId, Long itemId, CommentCreateDto dto);
}