package ru.practicum.shareit.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.ItemDetailsResponse;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.item.repo.CommentRepository;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemServiceImpl: business rules")
class ItemServiceImplTest {

    @Mock ItemRepository itemRepo;
    @Mock UserRepository userRepo;
    @Mock ItemMapper mapper;
    @Mock BookingRepository bookingRepo;
    @Mock CommentRepository commentRepo;

    @InjectMocks ItemServiceImpl service;

    @Test
    @DisplayName("create(): OK — owner exists, entity mapped & saved")
    void create_ok() {
        long ownerId = 10L;
        var dto = new ItemCreateDto("Drill", "600W", true, null);
        var owner = User.builder().id(ownerId).build();
        var entity = Item.builder().name("Drill").description("600W").available(true).owner(owner).build();
        var saved = Item.builder().id(100L).name("Drill").description("600W").available(true).owner(owner).build();

        when(userRepo.findById(ownerId)).thenReturn(Optional.of(owner));
        when(mapper.toEntity(eq(dto), eq(owner), isNull(ItemRequest.class))).thenReturn(entity);
        when(itemRepo.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(new ItemResponse(100L, "Drill", "600W", true));

        ItemResponse r = service.create(ownerId, dto);

        assertThat(r.id()).isEqualTo(100L);
        verify(itemRepo).save(entity);
    }

    @Test
    @DisplayName("create(): owner not found -> 404")
    void create_owner_not_found() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(1L, new ItemCreateDto("n","d", true, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("get(): 404 when item missing")
    void get_not_found() {
        when(itemRepo.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(1L, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("listOwnerItems(): returns mapped details DTOs")
    void listOwnerItems() {
        var owner = User.builder().id(1L).build();
        var items = List.of(
                Item.builder().id(1L).name("A").owner(owner).available(true).build(),
                Item.builder().id(2L).name("B").owner(owner).available(true).build()
        );
        when(userRepo.existsById(1L)).thenReturn(true);
        when(itemRepo.findAllByOwner_IdOrderByIdAsc(1L)).thenReturn(items);

        when(mapper.toDetails(eq(items.get(0)), isNull(), isNull(), anyList()))
                .thenReturn(new ItemDetailsResponse(1L, "A", null, true, null, null, List.of()));
        when(mapper.toDetails(eq(items.get(1)), isNull(), isNull(), anyList()))
                .thenReturn(new ItemDetailsResponse(2L, "B", null, true, null, null, List.of()));

        var resp = service.listOwnerItems(1L);
        assertThat(resp).extracting(ItemDetailsResponse::id).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("patch(): forbidden for non-owner")
    void patch_forbidden_for_non_owner() {
        var owner = User.builder().id(1L).build();
        var stranger = 99L;
        var item = Item.builder().id(10L).name("A").description("B").available(true).owner(owner).build();

        when(itemRepo.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.patch(stranger, 10L, new ItemUpdateDto("x", null, null)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("patch(): applies only non-null fields and saves")
    void patch_ok_partial_update() {
        var owner = User.builder().id(1L).build();
        var item = Item.builder().id(10L).name("A").description("B").available(true).owner(owner).build();
        var dto = new ItemUpdateDto(null, "New desc", false);

        when(itemRepo.findById(10L)).thenReturn(Optional.of(item));
        doAnswer(inv -> {
            Item target = inv.getArgument(0);
            ItemUpdateDto d = inv.getArgument(1);
            if (d.name() != null) target.setName(d.name());
            if (d.description() != null) target.setDescription(d.description());
            if (d.available() != null) target.setAvailable(d.available());
            return null;
        }).when(mapper).patch(any(Item.class), eq(dto));

        when(itemRepo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Item.class))).thenAnswer(inv -> {
            Item it = inv.getArgument(0);
            return new ItemResponse(it.getId(), it.getName(), it.getDescription(), it.isAvailable());
        });

        ItemResponse r = service.patch(1L, 10L, dto);

        assertThat(r.description()).isEqualTo("New desc");
        assertThat(r.available()).isFalse();

        ArgumentCaptor<Item> cap = ArgumentCaptor.forClass(Item.class);
        verify(itemRepo).save(cap.capture());
        assertThat(cap.getValue().getDescription()).isEqualTo("New desc");
        assertThat(cap.getValue().isAvailable()).isFalse();
    }

    @Test
    @DisplayName("search(): null or blank -> empty list")
    void search_null_or_blank_returns_empty() {
        assertThat(service.search(null)).isEmpty();
        assertThat(service.search("   ")).isEmpty();
        verifyNoInteractions(itemRepo, mapper);
    }

    @Test
    @DisplayName("search(): non-blank -> delegates to repository and maps")
    void search_non_blank_delegates() {
        when(itemRepo.searchAvailable("drill")).thenReturn(List.of(
                Item.builder().id(1L).name("Drill").available(true).build()
        ));
        when(mapper.toResponse(any(Item.class))).thenReturn(new ItemResponse(1L, "Drill", null, true));

        var out = service.search("drill");
        assertThat(out).hasSize(1);
        assertThat(out.get(0).name()).isEqualTo("Drill");
    }
}