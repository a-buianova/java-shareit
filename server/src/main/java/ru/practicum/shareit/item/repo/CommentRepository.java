package ru.practicum.shareit.item.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Comment;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** All comments for a given item (sorted by created asc). */
    List<Comment> findByItem_IdOrderByCreatedAsc(Long itemId);

    /** Comments for many items at once to avoid N+1 when listing owner items. */
    List<Comment> findByItem_IdInOrderByCreatedAsc(Collection<Long> itemIds);

}