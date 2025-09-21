package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.CommentRepository;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired private CommentRepository commentRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private UserRepository userRepo;

    @Test
    void findByItem_IdOrderByCreatedAsc_ordersCorrectly() {
        User u = userRepo.save(User.builder().name("u").email("u@e.com").build());
        Item i = itemRepo.save(Item.builder().name("n").description("d").available(true).owner(u).build());

        Comment c2 = commentRepo.save(Comment.builder().text("two").item(i).author(u).created(Instant.now().plusSeconds(10)).build());
        Comment c1 = commentRepo.save(Comment.builder().text("one").item(i).author(u).created(Instant.now()).build());

        var res = commentRepo.findByItem_IdOrderByCreatedAsc(i.getId());
        assertThat(res).extracting(Comment::getText).containsExactly("one", "two");
    }
}