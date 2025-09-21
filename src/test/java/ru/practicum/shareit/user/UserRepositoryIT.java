package ru.practicum.shareit.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("UserRepositoryIT")
class UserRepositoryIT {

    @Autowired UserRepository repo;

    @Test
    @DisplayName("save/find/delete — базовый CRUD")
    void crud_ok() {
        var u = repo.saveAndFlush(User.builder().name("A").email("a@ex.com").build());
        assertThat(u.getId()).isNotNull();
        assertThat(repo.findById(u.getId())).isPresent();
        repo.deleteById(u.getId());
        assertThat(repo.findById(u.getId())).isEmpty();
    }

    @Test
    @DisplayName("Уникальность email — второй insert с тем же email падает (DB constraint)")
    void uniqueEmail_enforcedByDb() {
        repo.saveAndFlush(User.builder().name("A").email("dup@ex.com").build());
        assertThatThrownBy(() ->
                repo.saveAndFlush(User.builder().name("B").email("dup@ex.com").build())
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("existsByEmailIgnoreCase — true/false в зависимости от регистрации")
    void existsByEmailIgnoreCase_ok() {
        assertThat(repo.existsByEmailIgnoreCase("x@ex.com")).isFalse();
        repo.saveAndFlush(User.builder().name("X").email("X@ex.com").build());
        assertThat(repo.existsByEmailIgnoreCase("x@ex.com")).isTrue();
    }
}