package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ItemControllerIT")
class ItemControllerIT {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepo;
    @Autowired ItemRepository itemRepo;

    Long ownerId;
    Long strangerId;
    Long itemId;

    @BeforeEach
    void setUp() {
        itemRepo.deleteAll();
        userRepo.deleteAll();

        var owner = userRepo.save(User.builder().name("Owner").email("o@ex.com").build());
        var stranger = userRepo.save(User.builder().name("Stranger").email("s@ex.com").build());
        var item = itemRepo.save(Item.builder()
                .name("Drill").description("600W").available(true).owner(owner).build());

        ownerId = owner.getId();
        strangerId = stranger.getId();
        itemId = item.getId();
    }

    @Test
    @Order(1)
    @DisplayName("POST /items — 201 Created")
    void create_201() throws Exception {
        var dto = new ItemCreateDto("Saw", "Hand saw", true, null);

        mvc.perform(post("/items")
                        .header(HDR, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Saw"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @Order(2)
    @DisplayName("POST /items — 400 Bad Request (DTO validation)")
    void create_400_validation() throws Exception {
        var invalid = new ItemCreateDto("  ", "", null, null);

        mvc.perform(post("/items")
                        .header(HDR, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("GET /items/{id} — 200 OK with comments; 404 when missing")
    void get_200_and_404() throws Exception {
        mvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId.intValue()))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.comments").exists());

        mvc.perform(get("/items/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    @DisplayName("GET /items — owner list contains comments")
    void list_owner_items() throws Exception {
        mvc.perform(get("/items").header(HDR, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(itemId.intValue())))
                .andExpect(jsonPath("$[0].comments").exists());
    }

    @Test
    @Order(5)
    @DisplayName("PATCH /items/{id} — only owner can update")
    void patch_owner_only() throws Exception {
        var dto = new ItemUpdateDto("Drill 650W", null, null);

        mvc.perform(patch("/items/{id}", itemId)
                        .header(HDR, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill 650W"));

        mvc.perform(patch("/items/{id}", itemId)
                        .header(HDR, strangerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new ItemUpdateDto(null, "x", null))))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    @DisplayName("GET /items/search — blank → []; results only available=true")
    void search_rules() throws Exception {
        mvc.perform(get("/items/search").param("text", "  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        itemRepo.saveAll(List.of(
                Item.builder()
                        .name("Super Drill")
                        .description("not available")
                        .available(false)
                        .owner(userRepo.findById(ownerId).orElseThrow())
                        .build()
        ));

        mvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].available", everyItem(is(true))))
                .andExpect(jsonPath("$[*].name", everyItem(containsStringIgnoringCase("drill"))));
    }

    @Test
    @Order(7)
    @DisplayName("Missing X-Sharer-User-Id — 400 Bad Request (where required)")
    void missing_header_400() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new ItemCreateDto("A", "B", true, null))))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/items"))
                .andExpect(status().isBadRequest());

        mvc.perform(patch("/items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new ItemUpdateDto("X", null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    @DisplayName("GET /items/{id} — 404 Not Found for missing id")
    void get_404() throws Exception {
        mvc.perform(get("/items/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(9)
    @DisplayName("PATCH /items/{id} — 404 Not Found for missing id")
    void patch_404() throws Exception {
        mvc.perform(patch("/items/{id}", 12345)
                        .header(HDR, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new ItemUpdateDto("X", null, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    @DisplayName("POST /items — 404 Not Found when owner missing")
    void create_ownerNotFound_404() throws Exception {
        var dto = new ItemCreateDto("Drill", "600W", true, null);

        mvc.perform(post("/items")
                        .header(HDR, 777_777L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    @DisplayName("GET /items/search — 400 Bad Request when text param missing")
    void search_missingParam_400() throws Exception {
        mvc.perform(get("/items/search"))
                .andExpect(status().isBadRequest());
    }
}