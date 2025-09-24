package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("UserController: integration tests")
class UserControllerIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository repo;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    @Test
    @DisplayName("GET /users — empty DB returns []")
    void list_empty_ok() throws Exception {
        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("POST /users — 201 Created with body")
    void create_201() throws Exception {
        var dto = new UserCreateDto("Ann", "a@ex.com");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Ann"))
                .andExpect(jsonPath("$.email").value("a@ex.com"));
    }

    @Test
    @DisplayName("GET /users/{id} — 200 OK for existing")
    void get_200() throws Exception {
        var dto = new UserCreateDto("Ann", "a@ex.com");
        var created = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andReturn().getResponse().getContentAsString();
        long id = om.readTree(created).get("id").asLong();

        mvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@ex.com"));
    }

    @Test
    @DisplayName("GET /users — 200 OK list")
    void list_200() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new UserCreateDto("Ann", "a@ex.com"))))
                .andExpect(status().isCreated());

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("PATCH /users/{id} — 200 OK updates fields")
    void patch_200() throws Exception {
        var created = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new UserCreateDto("Ann", "a@ex.com"))))
                .andReturn().getResponse().getContentAsString();
        long id = om.readTree(created).get("id").asLong();

        mvc.perform(patch("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    @DisplayName("DELETE /users/{id} — 204 No Content; then GET -> 404")
    void delete_204_then_get_404() throws Exception {
        var created = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new UserCreateDto("Ann", "a@ex.com"))))
                .andReturn().getResponse().getContentAsString();
        long id = om.readTree(created).get("id").asLong();

        mvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /users — 409 Conflict on duplicate email")
    void duplicateEmail_returns409() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new UserCreateDto("X", "dup@ex.com"))))
                .andExpect(status().isCreated());

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new UserCreateDto("Y", "dup@ex.com"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /users — 400 on blank name or invalid email")
    void create_400_validation() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"a@ex.com\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ann\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /users/{id} — 400 on invalid email format")
    void patch_invalidEmail_400() throws Exception {
        mvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"wrong\"}"))
                .andExpect(status().isBadRequest());
    }
}