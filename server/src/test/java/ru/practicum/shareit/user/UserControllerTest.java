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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@DisplayName("UserControllerTest")
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean UserService userService;

    @Test
    @DisplayName("POST /users — 201 Created")
    void create_201() throws Exception {
        var in  = new UserCreateDto("Ann", "a@ex.com");
        var out = new UserResponse(1L, "Ann", "a@ex.com");

        Mockito.when(userService.create(any(UserCreateDto.class))).thenReturn(out);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ann"))
                .andExpect(jsonPath("$.email").value("a@ex.com"));
    }

    @Test
    @DisplayName("GET /users/{id} — 200 OK")
    void get_200() throws Exception {
        Mockito.when(userService.get(1L)).thenReturn(new UserResponse(1L, "Ann", "a@ex.com"));

        mvc.perform(get("/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("a@ex.com"));
    }

    @Test
    @DisplayName("GET /users — 200 OK list")
    void list_200() throws Exception {
        Mockito.when(userService.list()).thenReturn(List.of(
                new UserResponse(1L, "Ann", "a@ex.com")
        ));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Ann"));
    }

    @Test
    @DisplayName("PATCH /users/{id} — 200 OK")
    void patch_200() throws Exception {
        Mockito.when(userService.patch(eq(1L), any(UserUpdateDto.class)))
                .thenReturn(new UserResponse(1L, "New Name", "a@ex.com"));

        mvc.perform(patch("/users/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }


    @Test
    @DisplayName("DELETE /users/{id} — 204 No Content")
    void delete_204() throws Exception {
        mvc.perform(delete("/users/{id}", 1))
                .andExpect(status().isNoContent());
        Mockito.verify(userService).delete(1L);
    }
}