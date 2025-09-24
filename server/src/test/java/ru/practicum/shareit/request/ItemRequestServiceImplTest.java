package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    @DisplayName("create(): 404 when user not found")
    void create_user_not_found() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(1L, new ItemRequestCreateDto("x")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("create(): OK — maps, saves, returns response with empty items")
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

        when(mapper.toResponse(eq(saved), eq(List.of())))
                .thenReturn(new ItemRequestResponse(
                        10L, "Need", 1L, saved.getCreated(), List.of()
                ));

        var r = service.create(1L, dto);

        assertThat(r.id()).isEqualTo(10L);
        verify(reqRepo).save(entity);
        verify(mapper).toResponse(saved, List.of());
    }

    @Test
    @DisplayName("findOwn(): checks user existence, loads items (bulk), maps")
    void findOwn_ok() {
        when(userRepo.existsById(1L)).thenReturn(true);

        var req1 = ItemRequest.builder().id(100L).build();
        when(reqRepo.findByRequestor_IdOrderByCreatedDesc(1L))
                .thenReturn(List.of(req1));

        var item1 = Item.builder()
                .id(5L)
                .name("Drill")
                .available(true)
                .request(req1)
                .build();
        var itemsForReq1 = List.of(item1);

        when(itemRepo.findAllByRequest_IdInOrderByIdAsc(List.of(100L)))
                .thenReturn(itemsForReq1);

        var mapped1 = new ItemRequestResponse(
                100L,
                null,
                (Instant) null,
                (ItemRequestResponse.Requester) null,
                List.of(new ItemRequestResponse.ItemShortDto(5L, "Drill", null, true, null))
        );
        when(mapper.toResponse(eq(req1), eq(itemsForReq1))).thenReturn(mapped1);

        var out = service.findOwn(1L);

        assertThat(out).hasSize(1);
        assertThat(out.get(0).items()).hasSize(1);

        verify(userRepo).existsById(1L);
        verify(reqRepo).findByRequestor_IdOrderByCreatedDesc(1L);
        verify(itemRepo).findAllByRequest_IdInOrderByIdAsc(List.of(100L));
        verify(mapper).toResponse(req1, itemsForReq1);
        verifyNoMoreInteractions(userRepo, reqRepo, itemRepo, mapper);
    }

    @Test
    @DisplayName("findAllExceptUser(): excludes userId, paginates, maps (bulk items)")
    void findAllExceptUser_ok() {
        when(userRepo.existsById(1L)).thenReturn(true);

        var req2 = ItemRequest.builder().id(200L).build();
        when(reqRepo.findByRequestor_IdNotOrderByCreatedDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(req2));

        when(itemRepo.findAllByRequest_IdInOrderByIdAsc(eq(List.of(200L)))).thenReturn(List.of());

        var mapped2 = new ItemRequestResponse(
                200L,
                null,
                (Instant) null,
                (ItemRequestResponse.Requester) null,
                List.of()
        );
        when(mapper.toResponse(eq(req2), eq(List.of()))).thenReturn(mapped2);

        var out = service.findAllExceptUser(1L, 0, 10);
        assertThat(out).hasSize(1);

        verify(userRepo).existsById(1L);
        verify(reqRepo).findByRequestor_IdNotOrderByCreatedDesc(eq(1L), any(PageRequest.class));
        verify(itemRepo).findAllByRequest_IdInOrderByIdAsc(eq(List.of(200L)));
        verify(mapper).toResponse(req2, List.of());
        verifyNoMoreInteractions(userRepo, reqRepo, itemRepo, mapper);
    }

    @Test
    @DisplayName("getById(): 404 when request not found")
    void getById_not_found() {
        when(userRepo.existsById(1L)).thenReturn(true);
        when(reqRepo.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(1L, 999L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("getById(): OK — loads single request items, maps")
    void getById_ok() {
        when(userRepo.existsById(1L)).thenReturn(true);

        var req = ItemRequest.builder().id(10L).build();
        when(reqRepo.findById(10L)).thenReturn(Optional.of(req));

        var items = List.of(Item.builder().id(1L).name("A").available(true).request(req).build());
        when(itemRepo.findAllByRequest_IdOrderByIdAsc(10L)).thenReturn(items);

        var mapped = new ItemRequestResponse(
                10L, "d", (Instant) null,
                new ItemRequestResponse.Requester(1L, null),
                List.of(new ItemRequestResponse.ItemShortDto(1L, "A", null, true, null))
        );
        when(mapper.toResponse(eq(req), eq(items))).thenReturn(mapped);

        var r = service.getById(1L, 10L);
        assertThat(r.id()).isEqualTo(10L);
        assertThat(r.items()).hasSize(1);
    }
}