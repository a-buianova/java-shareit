package ru.practicum.shareit.request.mapper;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * @apiNote Manual mapper for converting between ItemRequest entities and DTOs.
 */
@Component
public final class ItemRequestMapper {

    /**
     * Convert create-DTO to entity.
     * <p>Sets {@code created} to current time and assigns the provided requestor.</p>
     */
    public ItemRequest toEntity(ItemRequestCreateDto dto, User requestor) {
        return ItemRequest.builder()
                .description(dto.description())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
    }

    /**
     * Convert entity to response DTO.
     */
    public ItemRequestResponse toResponse(@Nullable ItemRequest entity) {
        if (entity == null) return null;
        Long requestorId = entity.getRequestor() != null ? entity.getRequestor().getId() : null;
        return new ItemRequestResponse(
                entity.getId(),
                entity.getDescription(),
                requestorId,
                entity.getCreated()
        );
    }
}