package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.repo.UserRepository;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("UserController: integration tests")
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository repo;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("POST /users — 201 Created + body; затем GET/LIST/PATCH/DELETE")
    void create_get_list_patch_delete_flow() throws Exception {
        UserCreateDto dto = new UserCreateDto("Ann", "a@ex.com");

        String created = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Ann"))
                .andExpect(jsonPath("$.email").value("a@ex.com"))
                .andReturn().getResponse().getContentAsString();

        long id = om.readTree(created).get("id").asLong();

        mvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@ex.com"));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(patch("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));

        mvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(2)
    @DisplayName("POST /users — 409 при дубликате email")
    void duplicateEmail_returns409() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"email\":\"dup@ex.com\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Y\",\"email\":\"dup@ex.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /users — 400 при пустом имени или некорректном email")
    void create_blankName_or_invalidEmail_400() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"a@ex.com\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"A\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /users/{id} — 400 при некорректном формате email")
    void patch_invalidEmail_400() throws Exception {
        mvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"wrong\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(0)
    @DisplayName("GET /users — при пустой БД возвращает []")
    void list_empty_ok() throws Exception {
        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}