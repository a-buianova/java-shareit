package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemRequestServiceImpl: business rules")
class ItemRequestServiceImplTest {

    @Mock ItemRequestRepository reqRepo;
    @Mock ItemRepository itemRepo;
    @Mock UserRepository userRepo;
    @Mock ItemRequestMapper mapper;

    @InjectMocks ItemRequestServiceImpl service;

    @Test
    @DisplayName("create(): 404 если пользователь не найден")
    void create_user_not_found() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(1L, new ItemRequestCreateDto("x")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("create(): OK — маппинг + сохранение + toResponse(..., emptyItems)")
    void create_ok() {
        var user   = User.builder().id(1L).build();
        var dto    = new ItemRequestCreateDto("Need");

        var entity = ItemRequest.builder()
                .description("Need")
                .requestor(user)
                .created(Instant.now())
                .build();

        var saved  = ItemRequest.builder()
                .id(10L)
                .description("Need")
                .requestor(user)
                .created(Instant.now())
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toEntity(dto, user)).thenReturn(entity);
        when(reqRepo.save(entity)).thenReturn(saved);

        Instant created = saved.getCreated(); // уже Instant
        when(mapper.toResponse(eq(saved), eq(List.of())))
                .thenReturn(new ItemRequestResponse(10L, "Need", 1L, created, List.of()));
        var r = service.create(1L, dto);

        assertThat(r.id()).isEqualTo(10L);
        verify(reqRepo).save(entity);
        verify(mapper).toResponse(saved, List.of());
    }

    @Test
    @DisplayName("findOwn(): проверяет existence пользователя, грузит свои запросы и прикладывает items")
    void findOwn_ok() {
        when(userRepo.existsById(1L)).thenReturn(true);

        var req1 = ItemRequest.builder().id(100L).build();
        when(reqRepo.findByRequestor_IdOrderByCreatedDesc(1L)).thenReturn(List.of(req1));

        when(itemRepo.findAllByRequest_IdOrderByIdAsc(100L)).thenReturn(List.of(
                Item.builder().id(5L).name("Drill").available(true).build()
        ));

        when(mapper.toResponse(eq(req1), anyList()))
                .thenAnswer(inv -> new ItemRequestResponse(
                        100L, null, null, null, inv.getArgument(1))
                );

        var out = service.findOwn(1L);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).items()).hasSize(1);
    }

    @Test
    @DisplayName("findAllExceptUser(): исключает userId, пагинация, и прикладывает items")
    void findAllExceptUser_ok() {
        when(userRepo.existsById(1L)).thenReturn(true);

        var req2 = ItemRequest.builder().id(200L).build();
        when(reqRepo.findByRequestor_IdNotOrderByCreatedDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(req2));

        when(itemRepo.findAllByRequest_IdOrderByIdAsc(200L)).thenReturn(List.of());

        when(mapper.toResponse(eq(req2), anyList()))
                .thenAnswer(inv -> new ItemRequestResponse(
                        200L, null, null, null, inv.getArgument(1))
                );

        var out = service.findAllExceptUser(1L, 0, 10);
        assertThat(out).hasSize(1);
    }

    @Test
    @DisplayName("getById(): 404 если запрос не найден")
    void getById_not_found() {
        when(userRepo.existsById(1L)).thenReturn(true);
        when(reqRepo.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(1L, 999L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("getById(): OK — найдёт запрос, подложит items и смаппит")
    void getById_ok() {
        when(userRepo.existsById(1L)).thenReturn(true);

        var req = ItemRequest.builder().id(10L).build();
        when(reqRepo.findById(10L)).thenReturn(Optional.of(req));
        when(itemRepo.findAllByRequest_IdOrderByIdAsc(10L)).thenReturn(List.of(
                Item.builder().id(1L).name("A").available(true).build()
        ));
        when(mapper.toResponse(eq(req), anyList()))
                .thenAnswer(inv -> new ItemRequestResponse(
                        10L, "d", 1L, null, inv.getArgument(1))
                );

        var r = service.getById(1L, 10L);
        assertThat(r.id()).isEqualTo(10L);
        assertThat(r.items()).hasSize(1);
    }
}