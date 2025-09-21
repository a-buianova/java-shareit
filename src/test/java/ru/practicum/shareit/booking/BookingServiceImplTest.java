package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.BookingStateParam;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit-tests for BookingServiceImpl: pure business rules, repositories are mocked.
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl: business rules (DTO uses LocalDateTime)")
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepo;
    @Mock private ItemRepository itemRepo;
    @Mock private UserRepository userRepo;

    @InjectMocks private BookingServiceImpl service;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2030, 1, 1, 12, 0);

    @Test
    @DisplayName("create(): OK -> WAITING")
    void create_ok() {
        long userId = 10L;
        var dto = new BookingCreateDto(5L, FIXED_NOW.plusDays(1), FIXED_NOW.plusDays(2));
        var user = User.builder().id(userId).build();
        var owner = User.builder().id(1L).build();
        var item = Item.builder().id(5L).available(true).owner(owner).build();

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepo.findById(5L)).thenReturn(Optional.of(item));
        when(bookingRepo.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });

        BookingResponse r = service.create(userId, dto);

        assertThat(r.status()).isEqualTo("WAITING");
        assertThat(r.id()).isEqualTo(100L);

        ArgumentCaptor<Booking> cap = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepo).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    @DisplayName("create(): owner cannot book own item -> 404")
    void create_ownerBooksOwnItem_404() {
        long userId = 1L;
        var dto = new BookingCreateDto(5L, FIXED_NOW.plusDays(1), FIXED_NOW.plusDays(2));
        var user = User.builder().id(userId).build();
        var item = Item.builder().id(5L).available(true).owner(user).build();

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepo.findById(5L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.create(userId, dto)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("create(): start >= end -> 400")
    void create_invalidInterval_400() {
        long uid = 10L;
        var dto = new BookingCreateDto(5L, FIXED_NOW, FIXED_NOW);

        when(userRepo.findById(uid)).thenReturn(Optional.of(User.builder().id(uid).build()));
        when(itemRepo.findById(5L)).thenReturn(Optional.of(
                Item.builder().id(5L).available(true).owner(User.builder().id(1L).build()).build()
        ));

        assertThatThrownBy(() -> service.create(uid, dto)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("create(): item not found -> 404")
    void create_item_not_found_404() {
        long uid = 10L;
        var dto = new BookingCreateDto(777L, FIXED_NOW.plusDays(1), FIXED_NOW.plusDays(2));

        when(userRepo.findById(uid)).thenReturn(Optional.of(User.builder().id(uid).build()));
        when(itemRepo.findById(777L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(uid, dto)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("create(): user not found -> 404")
    void create_user_not_found_404() {
        long uid = 10L;
        var dto = new BookingCreateDto(5L, FIXED_NOW.plusDays(1), FIXED_NOW.plusDays(2));

        when(userRepo.findById(uid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(uid, dto)).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(itemRepo, bookingRepo);
    }

    @Test
    @DisplayName("create(): item unavailable -> 400")
    void create_item_unavailable_400() {
        long uid = 10L;
        var dto = new BookingCreateDto(5L, FIXED_NOW.plusDays(1), FIXED_NOW.plusDays(2));

        var user = User.builder().id(uid).build();
        var owner = User.builder().id(1L).build();
        var item = Item.builder().id(5L).available(false).owner(owner).build();

        when(userRepo.findById(uid)).thenReturn(Optional.of(user));
        when(itemRepo.findById(5L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.create(uid, dto)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("approve(): only owner; WAITING -> APPROVED/REJECTED")
    void approve_ok() {
        long ownerId = 1L;
        var owner = User.builder().id(ownerId).build();
        var item = Item.builder().id(5L).owner(owner).build();
        var booking = Booking.builder()
                .id(100L).item(item)
                .booker(User.builder().id(10L).build())
                .status(BookingStatus.WAITING)
                .build();

        when(bookingRepo.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var r1 = service.approve(ownerId, 100L, true);
        assertThat(r1.status()).isEqualTo("APPROVED");

        booking.setStatus(BookingStatus.WAITING);
        var r2 = service.approve(ownerId, 100L, false);
        assertThat(r2.status()).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("approve(): not owner -> 403")
    void approve_notOwner_403() {
        var item = Item.builder().id(5L).owner(User.builder().id(1L).build()).build();
        var booking = Booking.builder()
                .id(100L).item(item)
                .booker(User.builder().id(10L).build())
                .status(BookingStatus.WAITING)
                .build();

        when(bookingRepo.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.approve(99L, 100L, true))
                .isInstanceOf(ForbiddenException.class);
        verify(bookingRepo, never()).save(any());
    }

    @Test
    @DisplayName("approve(): booking not found -> 404")
    void approve_notFound_404() {
        when(bookingRepo.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approve(1L, 100L, true))
                .isInstanceOf(NotFoundException.class);
        verify(bookingRepo, never()).save(any());
    }

    @Test
    @DisplayName("approve(): status not WAITING -> 400")
    void approve_alreadyFinalized_400() {
        var item = Item.builder().id(5L).owner(User.builder().id(1L).build()).build();
        var booking = Booking.builder()
                .id(100L).item(item)
                .booker(User.builder().id(10L).build())
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingRepo.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.approve(1L, 100L, true))
                .isInstanceOf(BadRequestException.class);
        verify(bookingRepo, never()).save(any());
    }

    @Test
    @DisplayName("get(): only owner/booker, otherwise 404")
    void get_access() {
        var owner = User.builder().id(1L).build();
        var booker = User.builder().id(2L).build();
        var item = Item.builder().id(5L).owner(owner).build();
        var booking = Booking.builder()
                .id(100L).item(item).booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingRepo.findById(100L)).thenReturn(Optional.of(booking));

        assertThat(service.get(1L, 100L)).isNotNull();
        assertThat(service.get(2L, 100L)).isNotNull();
        assertThatThrownBy(() -> service.get(3L, 100L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("get(): booking not found -> 404")
    void get_notFound_404() {
        when(bookingRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(1L, 999L)).isInstanceOf(NotFoundException.class);
    }

    @Nested
    @DisplayName("listUser / listOwner: all states")
    class Lists {

        @ParameterizedTest(name = "listUser: state={0}")
        @EnumSource(BookingStateParam.class)
        void listUser_allStates(BookingStateParam state) {
            when(userRepo.existsById(10L)).thenReturn(true);

            switch (state) {
                case ALL -> when(bookingRepo.findByBooker_IdOrderByStartDesc(eq(10L), any(PageRequest.class)))
                        .thenReturn(List.of());
                case CURRENT -> when(bookingRepo.findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(
                        eq(10L), any(Instant.class), any(Instant.class), any(PageRequest.class)))
                        .thenReturn(List.of());
                case PAST -> when(bookingRepo.findByBooker_IdAndEndBeforeOrderByStartDesc(
                        eq(10L), any(Instant.class), any(PageRequest.class)))
                        .thenReturn(List.of());
                case FUTURE -> when(bookingRepo.findByBooker_IdAndStartAfterOrderByStartDesc(
                        eq(10L), any(Instant.class), any(PageRequest.class)))
                        .thenReturn(List.of());
                case WAITING -> when(bookingRepo.findByBooker_IdAndStatusOrderByStartDesc(
                        eq(10L), eq(BookingStatus.WAITING), any(PageRequest.class)))
                        .thenReturn(List.of());
                case REJECTED -> when(bookingRepo.findByBooker_IdAndStatusOrderByStartDesc(
                        eq(10L), eq(BookingStatus.REJECTED), any(PageRequest.class)))
                        .thenReturn(List.of());
            }

            assertThat(service.listUser(10L, state, 0, 10)).isNotNull();
        }

        @ParameterizedTest(name = "listOwner: state={0}")
        @EnumSource(BookingStateParam.class)
        void listOwner_allStates(BookingStateParam state) {
            when(userRepo.existsById(1L)).thenReturn(true);

            switch (state) {
                case ALL -> when(bookingRepo.findByItem_Owner_IdOrderByStartDesc(eq(1L), any(PageRequest.class)))
                        .thenReturn(List.of());
                case CURRENT -> when(bookingRepo.findByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(
                        eq(1L), any(Instant.class), any(Instant.class), any(PageRequest.class)))
                        .thenReturn(List.of());
                case PAST -> when(bookingRepo.findByItem_Owner_IdAndEndBeforeOrderByStartDesc(
                        eq(1L), any(Instant.class), any(PageRequest.class)))
                        .thenReturn(List.of());
                case FUTURE -> when(bookingRepo.findByItem_Owner_IdAndStartAfterOrderByStartDesc(
                        eq(1L), any(Instant.class), any(PageRequest.class)))
                        .thenReturn(List.of());
                case WAITING -> when(bookingRepo.findByItem_Owner_IdAndStatusOrderByStartDesc(
                        eq(1L), eq(BookingStatus.WAITING), any(PageRequest.class)))
                        .thenReturn(List.of());
                case REJECTED -> when(bookingRepo.findByItem_Owner_IdAndStatusOrderByStartDesc(
                        eq(1L), eq(BookingStatus.REJECTED), any(PageRequest.class)))
                        .thenReturn(List.of());
            }

            assertThat(service.listOwner(1L, state, 0, 10)).isNotNull();
        }

        @Test
        @DisplayName("listUser(): 404 if user not found")
        void listUser_userNotFound_404() {
            when(userRepo.existsById(10L)).thenReturn(false);
            assertThatThrownBy(() -> service.listUser(10L, BookingStateParam.ALL, 0, 10))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("listOwner(): 404 if owner not found")
        void listOwner_ownerNotFound_404() {
            when(userRepo.existsById(1L)).thenReturn(false);
            assertThatThrownBy(() -> service.listOwner(1L, BookingStateParam.ALL, 0, 10))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}