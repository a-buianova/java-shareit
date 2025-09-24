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

/** Integration tests for ItemRequestController with real DB. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ItemRequestControllerIT")
class ItemRequestControllerIT {

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
        var a = userRepo.save(User.builder().name("Ann").email("a@ex.com").build());
        var b = userRepo.save(User.builder().name("Bob").email("b@ex.com").build());
        u1 = a.getId();
        u2 = b.getId();
    }

    @Test
    @DisplayName("POST /requests — 201 Created, created is set")
    void create_201() throws Exception {
        var dto = new ItemRequestCreateDto("Need a drill");
        Instant before = Instant.now();

        mvc.perform(post("/requests")
                        .header(HDR, u1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.requestorId").value(u1.intValue()))
                .andExpect(jsonPath("$.created", notNullValue()))
                .andExpect(jsonPath("$.items", hasSize(0)));

        Instant after = Instant.now();
        ItemRequest saved = reqRepo.findAll().get(0);
        Instant created = saved.getCreated();
        Assertions.assertTrue(!created.isBefore(before) && !created.isAfter(after.plusSeconds(1)));
    }

    @Test
    @DisplayName("POST /requests — 400 when description is blank")
    void create_400_validation() throws Exception {
        mvc.perform(post("/requests")
                        .header(HDR, u1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests — returns own requests sorted by created DESC")
    void list_own_desc() throws Exception {
        var a = userRepo.findById(u1).orElseThrow();

        reqRepo.save(ItemRequest.builder().description("late").requestor(a)
                .created(Instant.parse("2030-01-01T00:00:00Z")).build());
        reqRepo.save(ItemRequest.builder().description("latest").requestor(a)
                .created(Instant.parse("2030-01-01T00:00:01Z")).build());
        reqRepo.save(ItemRequest.builder().description("foreign")
                .requestor(userRepo.findById(u2).orElseThrow())
                .created(Instant.parse("2030-01-01T00:00:02Z")).build());

        mvc.perform(get("/requests").header(HDR, u1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description").value("latest"))
                .andExpect(jsonPath("$[1].description").value("late"));
    }

    @Test
    @DisplayName("GET /requests/all — paginates and excludes own requests")
    void list_all_paged_excluding_own() throws Exception {
        var b = userRepo.findById(u2).orElseThrow();
        reqRepo.save(ItemRequest.builder().description("r1").requestor(b).build());
        reqRepo.save(ItemRequest.builder().description("r2").requestor(b).build());
        reqRepo.save(ItemRequest.builder().description("r3").requestor(b).build());
        reqRepo.save(ItemRequest.builder().description("mine").requestor(userRepo.findById(u1).orElseThrow()).build());

        mvc.perform(get("/requests/all").header(HDR, u1).param("from", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(get("/requests/all").header(HDR, u1).param("from", "2").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /requests/{id} — 200 OK for existing")
    void get_by_id_ok() throws Exception {
        var r = reqRepo.save(ItemRequest.builder().description("one")
                .requestor(userRepo.findById(u2).orElseThrow()).build());

        mvc.perform(get("/requests/{id}", r.getId()).header(HDR, u1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("one"))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    @DisplayName("GET /requests/{id} — 404 Not Found for missing id")
    void get_by_id_404() throws Exception {
        mvc.perform(get("/requests/{id}", 9999).header(HDR, u1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /requests — 400 when header is missing")
    void missing_header_create_400() throws Exception {
        mvc.perform(post("/requests").contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests — 400 when header is missing")
    void missing_header_findOwn_400() throws Exception {
        mvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests/all — 400 when header is missing")
    void missing_header_findAll_400() throws Exception {
        mvc.perform(get("/requests/all").param("from", "0").param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests/{id} — 400 when header is missing")
    void missing_header_getById_400() throws Exception {
        mvc.perform(get("/requests/{id}", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /requests — 404 when userId does not exist")
    void create_404_userNotFound() throws Exception {
        var dto = new ItemRequestCreateDto("Need a drill");
        mvc.perform(post("/requests")
                        .header(HDR, 999_999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /requests/all — 404 when userId does not exist")
    void listAll_404_userNotFound() throws Exception {
        mvc.perform(get("/requests/all")
                        .header(HDR, 999_999L)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isNotFound());
    }
}