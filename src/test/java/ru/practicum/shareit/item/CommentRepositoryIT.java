package ru.practicum.shareit.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.CommentRepository;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("CommentRepositoryIT: ordering by created ASC")
class CommentRepositoryIT {

    @Autowired private CommentRepository commentRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private UserRepository userRepo;

    @Test
    @DisplayName("findByItem_IdOrderByCreatedAsc — returns [older, newer]")
    void findByItem_IdOrderByCreatedAsc_ordersCorrectly() {
        User u = userRepo.save(User.builder().name("u").email("u@e.com").build());
        Item i = itemRepo.save(Item.builder().name("n").description("d").available(true).owner(u).build());

        Instant t0 = Instant.parse("2030-01-01T00:00:00Z");
        Comment cOld = commentRepo.save(Comment.builder().text("one").item(i).author(u).created(t0).build());
        Comment cNew = commentRepo.save(Comment.builder().text("two").item(i).author(u).created(t0.plusSeconds(10)).build());

        var res = commentRepo.findByItem_IdOrderByCreatedAsc(i.getId());
        assertThat(res).extracting(Comment::getText).containsExactly("one", "two");
    }
}