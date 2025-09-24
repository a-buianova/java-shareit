package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
@DisplayName("BookingController: integration tests")
class BookingControllerIT {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepo;
    @Autowired ItemRepository itemRepo;
    @Autowired BookingRepository bookingRepo;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        bookingRepo.deleteAll();
        itemRepo.deleteAll();
        userRepo.deleteAll();

        var owner  = userRepo.save(User.builder().name("Owner").email("owner@test.com").build());
        var booker = userRepo.save(User.builder().name("Booker").email("booker@test.com").build());
        var item   = itemRepo.save(Item.builder()
                .name("Drill").description("600W").available(true).owner(owner).build());

        ownerId  = owner.getId();
        bookerId = booker.getId();
        itemId   = item.getId();
    }

    @Test
    @DisplayName("POST /bookings — 201 Created")
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
    @DisplayName("PATCH /bookings/{id}?approved=true — owner -> APPROVED")
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
    @DisplayName("GET /bookings/{id} — owner: 200 OK")
    void get_byOwner_200() throws Exception {
        var b = prepareApprovedFutureBooking();
        mvc.perform(get("/bookings/{id}", b.getId()).header(USER_HEADER, ownerId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /bookings/{id} — booker: 200 OK")
    void get_byBooker_200() throws Exception {
        var b = prepareApprovedFutureBooking();
        mvc.perform(get("/bookings/{id}", b.getId()).header(USER_HEADER, bookerId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /bookings/{id} — stranger: 404 Not Found")
    void get_byStranger_404() throws Exception {
        var b = prepareApprovedFutureBooking();
        mvc.perform(get("/bookings/{id}", b.getId()).header(USER_HEADER, 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /bookings?state=ALL — for booker: 200 OK")
    void list_user_all_200() throws Exception {
        prepareCurrentApprovedBooking();
        mvc.perform(get("/bookings").param("state", "ALL").header(USER_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /bookings/owner?state=ALL — for owner: 200 OK")
    void list_owner_all_200() throws Exception {
        prepareCurrentApprovedBooking();
        mvc.perform(get("/bookings/owner").param("state", "ALL").header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /bookings — 400 when item available=false")
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
    @DisplayName("POST /bookings — 404 when owner books own item")
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
    @DisplayName("POST /bookings — 400 when start>=end")
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
    @DisplayName("PATCH /bookings/{id} — 400 when already APPROVED")
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
    @DisplayName("GET /bookings/{id} — 404 when not found")
    void get_notFound_404() throws Exception {
        mvc.perform(get("/bookings/{id}", 999_999L).header(USER_HEADER, bookerId))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("POST /bookings — 400 when header missing")
    void create_missingHeader_400() throws Exception {
        var now = LocalDateTime.now().withNano(0);
        var dto = new BookingCreateDto(itemId, now.plusDays(1), now.plusDays(2));

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /bookings — 400 when state is UNKNOWN")
    void list_user_badState_400() throws Exception {
        mvc.perform(get("/bookings").param("state", "UNKNOWN").header(USER_HEADER, bookerId))
                .andExpect(status().isBadRequest());
    }

    private Booking prepareApprovedFutureBooking() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return bookingRepo.save(Booking.builder()
                .item(itemRepo.findById(itemId).orElseThrow())
                .booker(userRepo.findById(bookerId).orElseThrow())
                .start(now.plus(1, ChronoUnit.DAYS))
                .end(now.plus(2, ChronoUnit.DAYS))
                .status(BookingStatus.APPROVED)
                .build());
    }

    private void prepareCurrentApprovedBooking() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        bookingRepo.save(Booking.builder()
                .item(itemRepo.findById(itemId).orElseThrow())
                .booker(userRepo.findById(bookerId).orElseThrow())
                .start(now.minus(1, ChronoUnit.HOURS))
                .end(now.plus(1, ChronoUnit.HOURS))
                .status(BookingStatus.APPROVED)
                .build());
    }
}