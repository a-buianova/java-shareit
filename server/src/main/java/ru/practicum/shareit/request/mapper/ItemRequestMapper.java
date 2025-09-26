package ru.practicum.shareit.request.mapper;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

/**
 * Maps between ItemRequest entity and transport DTOs.
 * Note: 'created' is NOT set here; JPA sets it on persist (@PrePersist) or DB default.
 */
@Component
public final class ItemRequestMapper {

    /** Build entity from create-dto without 'created' assignment. */
    public ItemRequest toEntity(ItemRequestCreateDto dto, User requestor) {
        return ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(requestor)
                .build();
    }

    /**
     * Map entity + its items to response DTO. Tolerates nulls.
     * requester -> nested object {id, name} required by Postman tests.
     */
    public ItemRequestResponse toResponse(@Nullable ItemRequest entity, @Nullable List<Item> items) {
        if (entity == null) return null;

        ItemRequestResponse.Requester requester = null;
        User u = entity.getRequestor();
        if (u != null) {
            requester = new ItemRequestResponse.Requester(u.getId(), u.getName());
        }

        List<ItemRequestResponse.ItemShortDto> itemDtos =
                (items == null ? List.<Item>of() : items).stream()
                        .map(this::toShortItem)
                        .toList();

        return new ItemRequestResponse(
                entity.getId(),
                entity.getDescription(),
                entity.getCreated(),
                requester,
                itemDtos
        );
    }

    private ItemRequestResponse.ItemShortDto toShortItem(Item i) {
        Long requestId = (i.getRequest() != null) ? i.getRequest().getId() : null;
        return new ItemRequestResponse.ItemShortDto(
                i.getId(),
                i.getName(),
                i.getDescription(),
                i.isAvailable(),
                requestId
        );
    }
}