package ru.practicum.shareit.item.mapper;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Component
public final class ItemMapper {

    public Item toEntity(ItemCreateDto dto, User owner, @Nullable ItemRequest request) {
        Item i = new Item();
        i.setName(dto.name());
        i.setDescription(dto.description());
        i.setAvailable(Boolean.TRUE.equals(dto.available()));
        i.setOwner(owner);
        i.setRequest(request);
        return i;
    }

    public ItemResponse toResponse(@Nullable Item i) {
        if (i == null) return null;
        return new ItemResponse(
                i.getId(),
                i.getName(),
                i.getDescription(),
                i.isAvailable()
        );
    }

    public void patch(Item target, ItemUpdateDto dto) {
        if (dto.name() != null) target.setName(dto.name());
        if (dto.description() != null) target.setDescription(dto.description());
        if (dto.available() != null) target.setAvailable(dto.available());
    }

    public ItemDetailsResponse toDetails(Item item,
                                         @Nullable Booking last,
                                         @Nullable Booking next,
                                         List<Comment> comments) {
        return new ItemDetailsResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                toShort(last),
                toShort(next),
                comments.stream().map(CommentMapper::toResponse).toList()
        );
    }

    private BookingShortDto toShort(@Nullable Booking b) {
        if (b == null) return null;
        return new BookingShortDto(
                b.getId(),
                b.getBooker() != null ? b.getBooker().getId() : null,
                b.getStart(),
                b.getEnd()
        );
    }
}