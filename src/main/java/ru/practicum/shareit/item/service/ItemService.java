package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

/**
 * Use-case API for items.
 * Defines operations for creating, retrieving, updating and searching items.
 */
public interface ItemService {

    /**
     * Create a new item owned by a specific user.
     *
     * @param ownerId the id of the owner
     * @param dto     the item creation payload
     * @return created item response
     */
    ItemResponse create(Long ownerId, ItemCreateDto dto);

    /**
     * Get an item by its id.
     *
     * @param id item id
     * @return item response
     */
    ItemResponse get(Long id);

    /**
     * List all items owned by the given user.
     *
     * @param ownerId owner id
     * @return list of item responses
     */
    List<ItemResponse> listOwnerItems(Long ownerId);

    /**
     * Partially update an item (only owner is allowed).
     *
     * @param ownerId owner id
     * @param itemId  item id
     * @param dto     partial update payload
     * @return updated item response
     */
    ItemResponse patch(Long ownerId, Long itemId, ItemUpdateDto dto);

    /**
     * Search available items by text in name/description.
     * <p>
     * Service must handle {@code null} or blank queries
     * and return an empty list in that case.
     *
     * @param text free-text query
     * @return list of matching items, never {@code null}
     */
    List<ItemResponse> search(String text);
}