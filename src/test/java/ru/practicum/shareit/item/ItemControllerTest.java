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

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ItemController: integration tests")
class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper om;
    @Autowired private UserRepository userRepo;
    @Autowired private ItemRepository itemRepo;

    private Long ownerId;
    private Long strangerId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        itemRepo.deleteAll();
        userRepo.deleteAll();

        User owner = userRepo.save(User.builder().name("Owner").email("o@ex.com").build());
        User stranger = userRepo.save(User.builder().name("Stranger").email("s@ex.com").build());
        Item item = itemRepo.save(Item.builder()
                .name("Drill")
                .description("600W")
                .available(true)
                .owner(owner)
                .build());

        ownerId = owner.getId();
        strangerId = stranger.getId();
        itemId = item.getId();
    }

    @Test
    @Order(1)
    @DisplayName("POST /items — 201 Created и корректное тело")
    void create_ok_201() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("Saw", "Hand saw", true, null);

        mvc.perform(post("/items")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Saw"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @Order(2)
    @DisplayName("POST /items — 400 при нарушении валидации DTO")
    void create_validation_400() throws Exception {
        ItemCreateDto invalid = new ItemCreateDto("  ", "", null, null);

        mvc.perform(post("/items")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("GET /items/{id} — 200 и тело (с полем comments); 404 для несуществующего id")
    void get_ok_200_and_404_when_absent() throws Exception {
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
    @DisplayName("GET /items — список вещей владельца (каждый элемент содержит comments)")
    void list_owner_items() throws Exception {
        mvc.perform(get("/items").header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(itemId.intValue())))
                .andExpect(jsonPath("$[0].comments").exists());
    }

    @Test
    @Order(5)
    @DisplayName("PATCH /items/{id} — частичное изменение только владельцем")
    void patch_owner_only() throws Exception {
        ItemUpdateDto dto = new ItemUpdateDto("Drill 650W", null, null);

        mvc.perform(patch("/items/{id}", itemId)
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill 650W"));

        mvc.perform(patch("/items/{id}", itemId)
                        .header(USER_HEADER, strangerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new ItemUpdateDto(null, "x", null))))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    @DisplayName("GET /items/search — пустая строка -> []; поиск только по available=true")
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
    @DisplayName("Обязательный X-Sharer-User-Id отсутствует -> 400")
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
    @DisplayName("GET /items/{id} — 404 для несуществующего id")
    void get_notFound_404() throws Exception {
        mvc.perform(get("/items/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(9)
    @DisplayName("PATCH /items/{id} — 404 для несуществующего id")
    void patch_notFound_404() throws Exception {
        mvc.perform(patch("/items/{id}", 12345)
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new ItemUpdateDto("X", null, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    @DisplayName("POST /items — 404, если владелец не существует")
    void create_ownerNotFound_404() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("Drill", "600W", true, null);

        mvc.perform(post("/items")
                        .header(USER_HEADER, 777_777L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    @DisplayName("GET /items/search — 400, если отсутствует обязательный параметр text")
    void search_missingParam_400() throws Exception {
        mvc.perform(get("/items/search"))
                .andExpect(status().isBadRequest());
    }
}