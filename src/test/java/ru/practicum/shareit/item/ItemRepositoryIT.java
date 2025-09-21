package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DataJpaTest for ItemRepository: verifies @Query searchAvailable behavior.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("ItemRepository: searchAvailable()")
class ItemRepositoryIT {

    @Autowired ItemRepository itemRepo;
    @Autowired UserRepository userRepo;

    Long ownerId;

    @BeforeEach
    void init() {
        itemRepo.deleteAll();
        userRepo.deleteAll();

        String uniqueEmail = "owner+" + System.nanoTime() + "@ex.com";

        var owner = userRepo.save(User.builder()
                .name("Owner")
                .email(uniqueEmail)
                .build());

        ownerId = owner.getId();

        itemRepo.saveAll(List.of(
                Item.builder().name("Drill").description("powerful 600W").available(true).owner(owner).build(),
                Item.builder().name("driver").description("screwdriver").available(true).owner(owner).build(),
                Item.builder().name("Old Drill").description("broken").available(false).owner(owner).build()
        ));
    }

    @Test
    @DisplayName("Ищет по name/description (case-insensitive) и возвращает только available=true")
    void searchAvailable_ok() {
        var r1 = itemRepo.searchAvailable("DRILL");
        assertThat(r1).extracting(Item::getName).containsExactlyInAnyOrder("Drill");

        var r2 = itemRepo.searchAvailable("driver");
        assertThat(r2).extracting(Item::getName).containsExactlyInAnyOrder("driver");
    }
}