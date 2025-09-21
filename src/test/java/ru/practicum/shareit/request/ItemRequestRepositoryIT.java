package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Integration tests for ItemRequestRepository derived queries (H2, PostgreSQL mode). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("ItemRequestRepositoryIT")
class ItemRequestRepositoryIT {

    @Autowired ItemRequestRepository repo;
    @Autowired UserRepository userRepo;

    Long u1;
    Long u2;

    @BeforeEach
    void init() {
        repo.deleteAll();
        userRepo.deleteAll();

        var a = userRepo.save(User.builder().name("Ann").email("a+" + System.nanoTime() + "@ex.com").build());
        var b = userRepo.save(User.builder().name("Bob").email("b+" + System.nanoTime() + "@ex.com").build());
        u1 = a.getId();
        u2 = b.getId();

        Instant t0 = Instant.parse("2030-01-01T12:00:00Z");
        Instant t1 = t0.plusSeconds(1);
        Instant t2 = t1.plusSeconds(1);
        Instant t3 = t2.plusSeconds(1);
        Instant t4 = t3.plusSeconds(1);

        repo.save(ItemRequest.builder().description("first-u1").requestor(a).created(t0).build());
        repo.save(ItemRequest.builder().description("second-u1").requestor(a).created(t1).build());
        repo.save(ItemRequest.builder().description("r1-u2").requestor(b).created(t2).build());
        repo.save(ItemRequest.builder().description("r2-u2").requestor(b).created(t3).build());
        repo.save(ItemRequest.builder().description("r3-u2").requestor(b).created(t4).build());
    }

    @Test
    @DisplayName("findByRequestor_IdOrderByCreatedDesc — own only, DESC")
    void own_desc() {
        List<ItemRequest> list = repo.findByRequestor_IdOrderByCreatedDesc(u1);
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getDescription()).isEqualTo("second-u1");
        assertThat(list.get(1).getDescription()).isEqualTo("first-u1");
    }

    @Test
    @DisplayName("findByRequestor_IdNotOrderByCreatedDesc — excludes own, paged")
    void others_paged() {
        var page0 = repo.findByRequestor_IdNotOrderByCreatedDesc(u1, PageRequest.of(0, 2));
        assertThat(page0).hasSize(2);

        var page1 = repo.findByRequestor_IdNotOrderByCreatedDesc(u1, PageRequest.of(1, 2));
        assertThat(page1).hasSize(1);
    }
}