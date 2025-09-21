package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper om;
    @Autowired private UserRepository userRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private BookingRepository bookingRepo;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @BeforeEach
    void setup() {
        bookingRepo.deleteAll();
        itemRepo.deleteAll();
        userRepo.deleteAll();

        User owner = userRepo.save(User.builder().name("Owner").email("owner@test.com").build());
        User booker = userRepo.save(User.builder().name("Booker").email("booker@test.com").build());
        Item item = itemRepo.save(Item.builder()
                .name("Drill")
                .description("600W")
                .available(true)
                .owner(owner)
                .build());

        ownerId = owner.getId();
        bookerId = booker.getId();
        itemId = item.getId();
    }

    @Test
    @Order(1)
    @DisplayName("POST /bookings — 201 Created и корректное тело")
    void create_ok_201_and_body() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        BookingCreateDto dto = new BookingCreateDto(
                itemId,
                now.plusDays(1),
                now.plusDays(2)
        );

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
    @DisplayName("PATCH /bookings/{id}?approved=... — владелец меняет статус на APPROVED/REJECTED (на разных бронях)")
    void approve_byOwner_changesStatus() throws Exception {
        Instant now = Instant.now();

        Booking b1 = bookingRepo.save(Booking.builder()
                .item(itemRepo.findById(itemId).orElseThrow())
                .booker(userRepo.findById(bookerId).orElseThrow())
                .start(now.plus(1, ChronoUnit.DAYS))
                .end(now.plus(2, ChronoUnit.DAYS))
                .status(BookingStatus.WAITING)
                .build());

        mvc.perform(patch("/bookings/{id}", b1.getId())
                        .param("approved", "true")
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        Booking b2 = bookingRepo.save(Booking.builder()
                .item(itemRepo.findById(itemId).orElseThrow())
                .booker(userRepo.findById(bookerId).orElseThrow())
                .start(now.plus(3, ChronoUnit.DAYS))
                .end(now.plus(4, ChronoUnit.DAYS))
                .status(BookingStatus.WAITING)
                .build());

        mvc.perform(patch("/bookings/{id}", b2.getId())
                        .param("approved", "false")
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /bookings/{id} — доступ только для владельца или букера")
    void get_access_ownerOrBooker_only() throws Exception {
        Instant now = Instant.now();
        Booking b = bookingRepo.save(Booking.builder()
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
    @DisplayName("GET /bookings и /bookings/owner — фильтрация по state и пагинация")
    void list_forBooker_and_forOwner_withStateAndPaging() throws Exception {
        Instant now = Instant.now();

        bookingRepo.save(Booking.builder()
                .item(itemRepo.findById(itemId).orElseThrow())
                .booker(userRepo.findById(bookerId).orElseThrow())
                .start(now.minus(1, ChronoUnit.HOURS))
                .end(now.plus(1, ChronoUnit.HOURS))
                .status(BookingStatus.APPROVED)
                .build());

        mvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "20")
                        .header(USER_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "20")
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(5)
    @DisplayName("Ошибки: отсутствие заголовка -> 400, неизвестный state -> 400")
    void missingHeader_or_invalidState_returns400() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        BookingCreateDto dto = new BookingCreateDto(
                itemId,
                now.plusDays(1),
                now.plusDays(2)
        );

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/bookings")
                        .param("state", "UNKNOWN")
                        .header(USER_HEADER, bookerId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /bookings без X-Sharer-User-Id -> 400")
    void create_withoutHeader_returns400() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String body = """
            { "itemId": %d, "start": "%s", "end": "%s" }
            """.formatted(itemId, now.plusDays(1), now.plusDays(2));

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("X-Sharer-User-Id")));
    }

    @Test
    @DisplayName("PATCH /bookings/{id}?approved=... без X-Sharer-User-Id -> 400")
    void approve_withoutHeader_returns400() throws Exception {
        mvc.perform(patch("/bookings/{id}", 1L).param("approved", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("X-Sharer-User-Id")));
    }

    @Test
    @DisplayName("GET /bookings/{id} без X-Sharer-User-Id -> 400")
    void get_withoutHeader_returns400() throws Exception {
        mvc.perform(get("/bookings/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("X-Sharer-User-Id")));
    }

    @Nested
    @DisplayName("Листинги")
    class Listings {

        @Test
        @DisplayName("GET /bookings без X-Sharer-User-Id -> 400")
        void listUser_withoutHeader_returns400() throws Exception {
            mvc.perform(get("/bookings").param("state", "ALL"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("X-Sharer-User-Id")));
        }

        @Test
        @DisplayName("GET /bookings/owner без X-Sharer-User-Id -> 400")
        void listOwner_withoutHeader_returns400() throws Exception {
            mvc.perform(get("/bookings/owner").param("state", "ALL"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("X-Sharer-User-Id")));
        }
    }

    @Test
    @Order(6)
    @DisplayName("POST /bookings — 400, если вещь недоступна (available=false)")
    void create_itemNotAvailable_400() throws Exception {
        Item item = itemRepo.findById(itemId).orElseThrow();
        item.setAvailable(false);
        itemRepo.save(item);

        LocalDateTime now = LocalDateTime.now();
        BookingCreateDto dto = new BookingCreateDto(
                itemId,
                now.plusDays(1),
                now.plusDays(2)
        );

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto))
                        .header(USER_HEADER, bookerId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @DisplayName("POST /bookings — 404, если владелец бронирует свою вещь")
    void create_ownerBooksOwnItem_404() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        BookingCreateDto dto = new BookingCreateDto(
                itemId,
                now.plusDays(1),
                now.plusDays(2)
        );

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto))
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    @DisplayName("POST /bookings — 400, если интервал времени невалидный (start>=end)")
    void create_invalidInterval_400() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        BookingCreateDto dto = new BookingCreateDto(itemId, now, now);

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto))
                        .header(USER_HEADER, bookerId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    @DisplayName("PATCH /bookings/{id} — 400, если бронь уже не в WAITING (повторный approve/reject)")
    void approve_alreadyFinalized_400() throws Exception {
        Instant now = Instant.now();
        Booking b = bookingRepo.save(Booking.builder()
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
    @DisplayName("GET /bookings/{id} — 404 для несуществующего id")
    void get_notFound_404() throws Exception {
        mvc.perform(get("/bookings/{id}", 999_999L).header(USER_HEADER, bookerId))
                .andExpect(status().isNotFound());
    }
}