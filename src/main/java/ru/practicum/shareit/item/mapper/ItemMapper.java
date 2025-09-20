package ru.practicum.shareit.item.mapper;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

/**
 * @apiNote Mapper for converting between {@link Item} entities and DTOs.
 */
@Component
public final class ItemMapper {

    /**
     * Convert create-DTO to entity.
     */
    public Item toEntity(ItemCreateDto dto, User owner, @Nullable ItemRequest request) {
        Item i = new Item();
        i.setName(dto.name());
        i.setDescription(dto.description());
        i.setAvailable(Boolean.TRUE.equals(dto.available()));
        i.setOwner(owner);
        i.setRequest(request);
        return i;
    }

    /**
     * Convert entity to response DTO.
     */
    public ItemResponse toResponse(@Nullable Item i) {
        if (i == null) return null;
        return new ItemResponse(
                i.getId(),
                i.getName(),
                i.getDescription(),
                i.isAvailable()
        );
    }

    /**
     * Patch entity fields with non-null values from update DTO.
     */
    public void patch(Item target, ItemUpdateDto dto) {
        if (dto.name() != null) target.setName(dto.name());
        if (dto.description() != null) target.setDescription(dto.description());
        if (dto.available() != null) target.setAvailable(dto.available());
    }
}