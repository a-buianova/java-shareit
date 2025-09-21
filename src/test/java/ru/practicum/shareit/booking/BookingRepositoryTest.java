package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("BookingRepository: JPQL/derived queries (Instant)")
class BookingRepositoryTest {

    @Autowired private BookingRepository bookingRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private UserRepository userRepo;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    private final Instant now = Instant.parse("2030-01-01T12:00:00Z");

    @BeforeEach
    void setUp() {
        var owner = userRepo.save(User.builder()
                .name("Owner")
                .email("o+" + System.nanoTime() + "@ex.com")
                .build());

        var booker = userRepo.save(User.builder()
                .name("Booker")
                .email("b+" + System.nanoTime() + "@ex.com")
                .build());

        var item = itemRepo.save(Item.builder()
                .name("Drill")
                .description("600W")
                .available(true)
                .owner(owner)
                .build());

        ownerId = owner.getId();
        bookerId = booker.getId();
        itemId = item.getId();

        bookingRepo.save(Booking.builder()
                .start(now.plusSeconds(2 * 24 * 3600))  // +2d
                .end(now.plusSeconds(3 * 24 * 3600))    // +3d
                .item(item).booker(booker).status(BookingStatus.WAITING).build());

        bookingRepo.save(Booking.builder()
                .start(now.minusSeconds(3600))          // -1h
                .end(now.plusSeconds(3600))             // +1h
                .item(item).booker(booker).status(BookingStatus.APPROVED).build());

        bookingRepo.save(Booking.builder()
                .start(now.minusSeconds(3 * 24 * 3600)) // -3d
                .end(now.minusSeconds(2 * 24 * 3600))   // -2d
                .item(item).booker(booker).status(BookingStatus.REJECTED).build());
    }

    @Test
    @DisplayName("Поиск для букера: ALL — отсортировано по start DESC")
    void byBooker_allStates_orderedDesc() {
        var page = PageRequest.of(0, 10);

        var all = bookingRepo.findByBooker_IdOrderByStartDesc(bookerId, page);

        assertThat(all).hasSize(3);
        assertThat(all).isSortedAccordingTo((a, b) -> b.getStart().compareTo(a.getStart()));
    }

    @Test
    @DisplayName("Поиск для букера: CURRENT — одно текущее бронирование")
    void byBooker_current() {
        var page = PageRequest.of(0, 10);

        var list = bookingRepo.findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(
                bookerId, now, now, page);

        assertThat(list).hasSize(1);
        assertThat(list).extracting(Booking::getStatus).containsExactly(BookingStatus.APPROVED);
    }

    @Test
    @DisplayName("Поиск для владельца: FUTURE/PAST/WAITING — по одному результату в каждой выборке")
    void byOwner_futurePastWaiting() {
        var page = PageRequest.of(0, 10);

        List<Booking> future  = bookingRepo.findFutureByOwner(ownerId, now, page);
        List<Booking> past    = bookingRepo.findPastByOwner(ownerId, now, page);
        List<Booking> waiting = bookingRepo.findByOwnerAndStatus(ownerId, BookingStatus.WAITING, page);

        assertThat(future).hasSize(1);
        assertThat(past).hasSize(1);
        assertThat(waiting).hasSize(1);
    }

    @Test
    @DisplayName("exists...EndBeforeAndStatus — подтверждает наличие прошедшего REJECTED бронирования")
    void existsByBookerAndItemAndEndBeforeAndStatus() {
        boolean ok = bookingRepo.existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
                bookerId, itemId, now, BookingStatus.REJECTED);

        assertThat(ok).isTrue();
    }
}