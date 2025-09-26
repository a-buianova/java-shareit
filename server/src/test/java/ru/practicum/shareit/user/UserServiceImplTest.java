package ru.practicum.shareit.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.practicum.shareit.common.exception.ConflictException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl: business rules")
class UserServiceImplTest {

    @Mock private UserRepository repo;
    @Mock private UserMapper mapper;

    @InjectMocks private UserServiceImpl service;

    @Test
    @DisplayName("create(): OK и проверка конфликта по email")
    void create_ok_and_conflict() {
        UserCreateDto dto = new UserCreateDto("Ann", "a@ex.com");
        User entity = User.builder().name("Ann").email("a@ex.com").build();
        User saved = User.builder().id(10L).name("Ann").email("a@ex.com").build();
        UserResponse resp = new UserResponse(10L, "Ann", "a@ex.com");

        when(repo.existsByEmailIgnoreCase("a@ex.com")).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repo.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(resp);

        UserResponse out = service.create(dto);
        assertThat(out.id()).isEqualTo(10L);

        when(repo.existsByEmailIgnoreCase("a@ex.com")).thenReturn(true);
        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("get(): 404 если не найден")
    void get_404() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("list(): маппит всех пользователей")
    void list_ok() {
        User u1 = User.builder().id(1L).name("A").email("a@ex.com").build();
        User u2 = User.builder().id(2L).name("B").email("b@ex.com").build();

        when(repo.findAll()).thenReturn(List.of(u1, u2));
        when(mapper.toResponse(u1)).thenReturn(new UserResponse(1L, "A", "a@ex.com"));
        when(mapper.toResponse(u2)).thenReturn(new UserResponse(2L, "B", "b@ex.com"));

        var out = service.list();
        assertThat(out).extracting(UserResponse::id).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("patch(): 404 если не найден; конфликт при смене email на занятый")
    void patch_404_and_conflict() {
        when(repo.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.patch(10L, new UserUpdateDto(null, "x@ex.com")))
                .isInstanceOf(NotFoundException.class);

        User existing = User.builder().id(10L).name("Ann").email("a@ex.com").build();
        when(repo.findById(10L)).thenReturn(Optional.of(existing));

        UserUpdateDto dto = new UserUpdateDto(null, "dup@ex.com");
        when(repo.existsByEmailIgnoreCase("dup@ex.com")).thenReturn(true);

        assertThatThrownBy(() -> service.patch(10L, dto)).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("patch(): OK — обновляет только непустые поля и сохраняет")
    void patch_ok() {
        User existing = User.builder().id(10L).name("Ann").email("a@ex.com").build();
        when(repo.findById(10L)).thenReturn(Optional.of(existing));

        UserUpdateDto dto = new UserUpdateDto("Anna", null);

        when(repo.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(new UserResponse(10L, "Anna", "a@ex.com"));

        UserResponse out = service.patch(10L, dto);
        assertThat(out.name()).isEqualTo("Anna");
        assertThat(out.email()).isEqualTo("a@ex.com");
    }

    @Test
    @DisplayName("delete(): 404 если отсутствует")
    void delete_404() {
        when(repo.existsById(77L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(77L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("delete(): OK когда существует")
    void delete_ok() {
        when(repo.existsById(10L)).thenReturn(true);
        service.delete(10L);
        verify(repo).deleteById(10L);
    }

    @Test
    @DisplayName("patch(): тот же email (без изменения) — не конфликтует и не проверяет уникальность")
    void patch_same_email_ok() {
        User existing = User.builder().id(10L).name("Ann").email("a@ex.com").build();

        when(repo.findById(10L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(User.class)))
                .thenReturn(new UserResponse(10L, existing.getName(), existing.getEmail()));

        UserResponse out = service.patch(10L, new UserUpdateDto(null, "a@ex.com"));

        assertThat(out.email()).isEqualTo("a@ex.com");
    }
}