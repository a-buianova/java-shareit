package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerMockMvcTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean UserService service;

    @Test
    @DisplayName("POST /users -> 201 Created")
    void create_ok() throws Exception {
        Mockito.when(service.create(any(UserCreateDto.class)))
                .thenReturn(new UserResponse(1L, "Alice", "a@ex.com"));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new UserCreateDto("Alice", "a@ex.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("a@ex.com"));
    }

    @Test
    @DisplayName("POST /users -> 400 on invalid email")
    void create_badRequest_onInvalidEmail() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new UserCreateDto("Alice", "not-email"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("email")));
    }

    @Test
    @DisplayName("GET /users/{id} -> 200 OK")
    void get_ok() throws Exception {
        Mockito.when(service.get(10L))
                .thenReturn(new UserResponse(10L, "Bob", "b@ex.com"));

        mvc.perform(get("/users/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Bob"));
    }

    @Test
    @DisplayName("GET /users/{id} -> 404 when not found")
    void get_notFound() throws Exception {
        Mockito.when(service.get(404L)).thenThrow(new NoSuchElementException("user not found"));

        mvc.perform(get("/users/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("user not found"));
    }

    @Test
    @DisplayName("GET /users -> 200 and list returned")
    void list_ok() throws Exception {
        Mockito.when(service.list()).thenReturn(List.of(
                new UserResponse(1L, "A", "a@ex.com"),
                new UserResponse(2L, "B", "b@ex.com")
        ));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @DisplayName("PATCH /users/{id} -> 200 OK")
    void patch_ok() throws Exception {
        Mockito.when(service.patch(eq(1L), any(UserUpdateDto.class)))
                .thenReturn(new UserResponse(1L, "A2", "a@ex.com"));

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new UserUpdateDto("A2", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("A2"));
    }

    @Test
    @DisplayName("PATCH /users/{id} -> 409 on email conflict")
    void patch_conflict() throws Exception {
        Mockito.when(service.patch(eq(1L), any(UserUpdateDto.class)))
                .thenThrow(new IllegalStateException("email already used"));

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new UserUpdateDto(null, "dup@ex.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("email already used"));
    }

    @Test
    @DisplayName("DELETE /users/{id} -> 204 No Content")
    void delete_ok() throws Exception {
        mvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }
}