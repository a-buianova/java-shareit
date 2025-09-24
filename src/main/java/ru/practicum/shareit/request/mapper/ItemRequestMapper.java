package ru.practicum.shareit.request.mapper;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

/**
 * Mapper for converting between ItemRequest entities and DTOs.
 * Important: 'created' is not set here; it is assigned by JPA (@PrePersist) / DB.
 */
@Component
public final class ItemRequestMapper {

    /** Build entity without setting 'created' (JPA sets it on persist). */
    public ItemRequest toEntity(ItemRequestCreateDto dto, User requestor) {
        return ItemRequest.builder()
                .description(dto.description())
                .requestor(requestor)
                .build();
    }

    public ItemRequestResponse toResponse(@Nullable ItemRequest entity, List<Item> items) {
        if (entity == null) return null;
        Long requestorId = entity.getRequestor() != null ? entity.getRequestor().getId() : null;
        return new ItemRequestResponse(
                entity.getId(),
                entity.getDescription(),
                requestorId,
                entity.getCreated(),
                items == null ? List.of() : items.stream().map(this::toShortItem).toList()
        );
    }

    private ItemRequestResponse.ItemShortDto toShortItem(Item i) {
        return new ItemRequestResponse.ItemShortDto(
                i.getId(),
                i.getName(),
                i.getDescription(),
                i.isAvailable(),
                i.getRequest() != null ? i.getRequest().getId() : null
        );
    }
}