package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Mapper for Comment <-> DTO. */
public final class CommentMapper {

    private CommentMapper() {}

    public static Comment toEntity(CommentCreateDto dto, Item item, User author) {
        return Comment.builder()
                .text(dto.text() == null ? null : dto.text().trim())
                .item(item)
                .author(author)
                // created оставляем null — выставит БД/энтити @PrePersist
                .build();
    }

    public static CommentResponse toResponse(Comment c) {
        LocalDateTime created = c.getCreated() == null
                ? null
                : LocalDateTime.ofInstant(c.getCreated(), ZoneOffset.UTC);

        return new CommentResponse(
                c.getId(),
                c.getText(),
                c.getAuthor() != null ? c.getAuthor().getId() : null,
                c.getAuthor() != null ? c.getAuthor().getName() : null,
                created
        );
    }
}