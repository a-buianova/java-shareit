package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DataJpaTest for UserRepository: CRUD + email uniqueness (DB constraint).
 */
@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("UserRepository: JPA CRUD + unique email")
class UserRepositoryTest {

    @Autowired UserRepository repo;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    @Test
    @DisplayName("save/find/delete — базовые операции работают")
    void crud_ok() {
        var u = repo.saveAndFlush(User.builder().name("A").email("a@ex.com").build());
        assertThat(u.getId()).isNotNull();

        var found = repo.findById(u.getId());
        assertThat(found).isPresent();

        repo.deleteById(u.getId());
        assertThat(repo.findById(u.getId())).isEmpty();
    }

    @Test
    @DisplayName("Уникальность email на уровне БД — второй insert с тем же email падает")
    void uniqueEmail_enforcedByDb() {
        repo.saveAndFlush(User.builder().name("A").email("dup@ex.com").build());

        assertThatThrownBy(() ->
                repo.saveAndFlush(User.builder().name("B").email("dup@ex.com").build())
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}