package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.CommentRepository;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Comments: integration tests")
class CommentControllerIT {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper om;
    @Autowired private UserRepository userRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private CommentRepository commentRepo;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @BeforeEach
    void setup() {
        commentRepo.deleteAll();
        bookingRepo.deleteAll();
        itemRepo.deleteAll();
        userRepo.deleteAll();

        User owner  = userRepo.save(User.builder().name("Owner").email("o@ex.com").build());
        User booker = userRepo.save(User.builder().name("Booker").email("b@ex.com").build());
        Item item   = itemRepo.save(Item.builder()
                .name("Drill").description("600W").available(true).owner(owner).build());

        ownerId = owner.getId();
        bookerId = booker.getId();
        itemId = item.getId();
    }

    @Test
    @Order(1)
    @DisplayName("POST /items/{id}/comment — 201 при прошедшем APPROVED бронировании")
    void create_comment_ok_201() throws Exception {
        // прошедшая бронь: end < now, APPROVED
        Instant now = Instant.now();
        bookingRepo.save(Booking.builder()
                .item(itemRepo.findById(itemId).orElseThrow())
                .booker(userRepo.findById(bookerId).orElseThrow())
                .start(now.minus(2, ChronoUnit.DAYS))
                .end(now.minus(1, ChronoUnit.DAYS))
                .status(BookingStatus.APPROVED)
                .build());

        CommentCreateDto dto = new CommentCreateDto("Great tool!");

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text").value("Great tool!"))
                .andExpect(jsonPath("$.authorId").value(bookerId.intValue()))
                .andExpect(jsonPath("$.authorName").value("Booker"))
                .andExpect(jsonPath("$.created", notNullValue()));
    }

    @Test
    @Order(2)
    @DisplayName("POST /items/{id}/comment — 400 без права (нет прошедших APPROVED)")
    void create_comment_forbidden_400() throws Exception {
        Instant now = Instant.now();
        bookingRepo.save(Booking.builder()
                .item(itemRepo.findById(itemId).orElseThrow())
                .booker(userRepo.findById(bookerId).orElseThrow())
                .start(now.minus(2, ChronoUnit.DAYS))
                .end(now.minus(1, ChronoUnit.DAYS))
                .status(BookingStatus.WAITING)
                .build());

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new CommentCreateDto("Hi"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("POST /items/{id}/comment — 400 при будущем бронировании")
    void create_comment_futureBooking_400() throws Exception {
        Instant now = Instant.now();
        bookingRepo.save(Booking.builder()
                .item(itemRepo.findById(itemId).orElseThrow())
                .booker(userRepo.findById(bookerId).orElseThrow())
                .start(now.plus(1, ChronoUnit.DAYS))
                .end(now.plus(2, ChronoUnit.DAYS))
                .status(BookingStatus.APPROVED)
                .build());

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new CommentCreateDto("Future not allowed"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("POST /items/{id}/comment — 400 при пустом тексте")
    void create_comment_validation_400() throws Exception {
        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new CommentCreateDto("  "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("GET /items/{id} — комментарии возвращаются и отсортированы по created ASC")
    void get_item_contains_comments_sorted() throws Exception {
        Instant now = Instant.now();

        bookingRepo.save(Booking.builder()
                .item(itemRepo.findById(itemId).orElseThrow())
                .booker(userRepo.findById(bookerId).orElseThrow())
                .start(now.minus(3, ChronoUnit.DAYS))
                .end(now.minus(2, ChronoUnit.DAYS))
                .status(BookingStatus.APPROVED)
                .build());

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new CommentCreateDto("first"))))
                .andExpect(status().isCreated());

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new CommentCreateDto("second"))))
                .andExpect(status().isCreated());

        mvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(2)))
                .andExpect(jsonPath("$.comments[0].text").value("first"))
                .andExpect(jsonPath("$.comments[1].text").value("second"));
    }
}