package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingControllerIT {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepo;
    @Autowired ItemRepository itemRepo;
    @Autowired BookingRepository bookingRepo;

    Long ownerId, bookerId, itemId;

    @BeforeEach
    void setUp() {
        bookingRepo.deleteAll();
        itemRepo.deleteAll();
        userRepo.deleteAll();

        var owner = userRepo.save(User.builder().name("Owner").email("owner@test.com").build());
        var booker = userRepo.save(User.builder().name("Booker").email("booker@test.com").build());
        var item = itemRepo.save(Item.builder()
                .name("Drill").description("600W").available(true).owner(owner).build());

        ownerId = owner.getId();
        bookerId = booker.getId();
        itemId = item.getId();
    }

    @Test
    @Order(1)
    @DisplayName("POST /bookings — 201 и валидный ответ")
    void create_201() throws Exception {
        var now = LocalDateTime.now().withNano(0);
        var dto = new BookingCreateDto(itemId, now.plusDays(1), now.plusDays(2));

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto))
                        .header(USER_HEADER, bookerId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.item.id").value(itemId.intValue()))
                .andExpect(jsonPath("$.booker.id").value(bookerId.intValue()));
    }

    @Test
    @Order(2)
    @DisplayName("PATCH approve=true — владелец меняет статус на APPROVED")
    void approve_true_200() throws Exception {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        var b = bookingRepo.save(Booking.builder()
                .item(itemRepo.findById(itemId).orElseThrow())
                .booker(userRepo.findById(bookerId).orElseThrow())
                .start(now.plus(1, ChronoUnit.DAYS))
                .end(now.plus(2, ChronoUnit.DAYS))
                .status(BookingStatus.WAITING)
                .build());

        mvc.perform(patch("/bookings/{id}", b.getId())
                        .param("approved", "true")
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /bookings/{id} — доступ owner/booker, чужому 404")
    void get_access() throws Exception {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        var b = bookingRepo.save(Booking.builder()
                .item(itemRepo.findById(itemId).orElseThrow())
                .booker(userRepo.findById(bookerId).orElseThrow())
                .start(now.plus(1, ChronoUnit.DAYS))
                .end(now.plus(2, ChronoUnit.DAYS))
                .status(BookingStatus.APPROVED)
                .build());

        mvc.perform(get("/bookings/{id}", b.getId()).header(USER_HEADER, ownerId))
                .andExpect(status().isOk());
        mvc.perform(get("/bookings/{id}", b.getId()).header(USER_HEADER, bookerId))
                .andExpect(status().isOk());
        mvc.perform(get("/bookings/{id}", b.getId()).header(USER_HEADER, 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    @DisplayName("Листинги booker/owner — state=ALL")
    void lists() throws Exception {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        bookingRepo.save(Booking.builder()
                .item(itemRepo.findById(itemId).orElseThrow())
                .booker(userRepo.findById(bookerId).orElseThrow())
                .start(now.minus(1, ChronoUnit.HOURS))
                .end(now.plus(1, ChronoUnit.HOURS))
                .status(BookingStatus.APPROVED)
                .build());

        mvc.perform(get("/bookings").param("state", "ALL").header(USER_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        mvc.perform(get("/bookings/owner").param("state", "ALL").header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(5)
    @DisplayName("Ошибки: отсутствие заголовка и плохой state → 400")
    void errors_400() throws Exception {
        var now = LocalDateTime.now().withNano(0);
        var dto = new BookingCreateDto(itemId, now.plusDays(1), now.plusDays(2));

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/bookings").param("state", "UNKNOWN").header(USER_HEADER, bookerId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    @DisplayName("POST — 400, если вещь недоступна")
    void create_itemNotAvailable_400() throws Exception {
        var item = itemRepo.findById(itemId).orElseThrow();
        item.setAvailable(false);
        itemRepo.save(item);

        var now = LocalDateTime.now().withNano(0);
        var dto = new BookingCreateDto(itemId, now.plusDays(1), now.plusDays(2));

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto))
                        .header(USER_HEADER, bookerId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @DisplayName("POST — 404, если владелец бронирует свою вещь")
    void create_ownerBooksOwnItem_404() throws Exception {
        var now = LocalDateTime.now().withNano(0);
        var dto = new BookingCreateDto(itemId, now.plusDays(1), now.plusDays(2));

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto))
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    @DisplayName("POST — 400, если интервал невалиден (start>=end)")
    void create_invalidInterval_400() throws Exception {
        var now = LocalDateTime.now().withNano(0);
        var dto = new BookingCreateDto(itemId, now, now);

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto))
                        .header(USER_HEADER, bookerId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    @DisplayName("PATCH — 400, если бронь уже не WAITING")
    void approve_alreadyFinalized_400() throws Exception {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        var b = bookingRepo.save(Booking.builder()
                .item(itemRepo.findById(itemId).orElseThrow())
                .booker(userRepo.findById(bookerId).orElseThrow())
                .start(now.plus(1, ChronoUnit.DAYS))
                .end(now.plus(2, ChronoUnit.DAYS))
                .status(BookingStatus.APPROVED)
                .build());

        mvc.perform(patch("/bookings/{id}", b.getId())
                        .param("approved", "true")
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    @DisplayName("GET — 404 для несуществующего id")
    void get_notFound_404() throws Exception {
        mvc.perform(get("/bookings/{id}", 999_999L).header(USER_HEADER, bookerId))
                .andExpect(status().isNotFound());
    }
}