package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.Instant;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ItemRequestControllerTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepo;
    @Autowired ItemRequestRepository reqRepo;

    Long u1;
    Long u2;

    @BeforeEach
    void setUp() {
        reqRepo.deleteAll();
        userRepo.deleteAll();
        User a = userRepo.save(User.builder().name("Ann").email("a@ex.com").build());
        User b = userRepo.save(User.builder().name("Bob").email("b@ex.com").build());
        u1 = a.getId();
        u2 = b.getId();
    }

    @Test
    @Order(1)
    @DisplayName("POST /requests — 201 и корректное тело; created выставляется на текущее время")
    void create_ok_201() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Need a drill");
        Instant before = Instant.now();

        mvc.perform(post("/requests")
                        .header(HDR, u1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.requestorId").value(u1.intValue()))
                .andExpect(jsonPath("$.created", notNullValue()));

        Instant after = Instant.now();

        ItemRequest saved = reqRepo.findAll().get(0);
        Instant created = saved.getCreated();

        Assertions.assertTrue(
                !created.isBefore(before) && !created.isAfter(after.plusSeconds(1)),
                () -> String.format("created=%s must be between %s and %s", created, before, after.plusSeconds(1))
        );
    }

    @Test
    @Order(2)
    @DisplayName("POST /requests — 400 при пустом description")
    void create_validation_400() throws Exception {
        mvc.perform(post("/requests")
                        .header(HDR, u1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("GET /requests — возвращает запросы текущего пользователя, отсортированные по created DESC")
    void list_own_desc() throws Exception {
        reqRepo.save(ItemRequest.builder()
                .description("late")
                .requestor(userRepo.findById(u1).orElseThrow())
                .build());
        Thread.sleep(10);
        reqRepo.save(ItemRequest.builder()
                .description("latest")
                .requestor(userRepo.findById(u1).orElseThrow())
                .build());
        reqRepo.save(ItemRequest.builder()
                .description("foreign")
                .requestor(userRepo.findById(u2).orElseThrow())
                .build());

        mvc.perform(get("/requests").header(HDR, u1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description").value("latest"))
                .andExpect(jsonPath("$[1].description").value("late"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /requests/all — пагинация и исключение собственных запросов")
    void list_all_paged_excluding_own() throws Exception {
        reqRepo.save(ItemRequest.builder().description("r1").requestor(userRepo.findById(u2).orElseThrow()).build());
        reqRepo.save(ItemRequest.builder().description("r2").requestor(userRepo.findById(u2).orElseThrow()).build());
        reqRepo.save(ItemRequest.builder().description("r3").requestor(userRepo.findById(u2).orElseThrow()).build());
        reqRepo.save(ItemRequest.builder().description("mine").requestor(userRepo.findById(u1).orElseThrow()).build());

        mvc.perform(get("/requests/all")
                        .header(HDR, u1)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(get("/requests/all")
                        .header(HDR, u1)
                        .param("from", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @Order(5)
    @DisplayName("GET /requests/{id} — 200 для существующего; 404 — если не найден")
    void get_by_id_ok_and_404() throws Exception {
        ItemRequest r = reqRepo.save(ItemRequest.builder()
                .description("one")
                .requestor(userRepo.findById(u2).orElseThrow())
                .build());

        mvc.perform(get("/requests/{id}", r.getId()).header(HDR, u1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("one"));

        mvc.perform(get("/requests/{id}", 9999).header(HDR, u1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Отсутствие X-Sharer-User-Id -> 400 везде, где требуется")
    void missing_header_400() throws Exception {
        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"x\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/requests/all").param("from", "0").param("size", "10"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/requests/{id}", 1L))
                .andExpect(status().isBadRequest());
    }
}