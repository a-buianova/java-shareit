package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(UserController.class)
@ActiveProfiles("test")
@DisplayName("UserGatewayControllerHappyPathTest")
class UserGatewayControllerHappyPathTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean UserClient userClient;

    @Test
    @DisplayName("POST /users — 201 Created")
    void create_201() throws Exception {
        var dto = new UserCreateDto("Bob", "bob@example.com");

        when(userClient.create(any(UserCreateDto.class)))
                .thenReturn(ResponseEntity.status(201).body("{\"id\":1,\"name\":\"Bob\"}"));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));

        verify(userClient).create(ArgumentMatchers.any(UserCreateDto.class));
    }

    @Test
    @DisplayName("GET /users/{id} — 200 OK")
    void get_200() throws Exception {
        when(userClient.get(5L))
                .thenReturn(ResponseEntity.ok("{\"id\":5,\"name\":\"Ann\"}"));

        mvc.perform(get("/users/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)));

        verify(userClient).get(5L);
    }

    @Test
    @DisplayName("GET /users — 200 OK")
    void list_200() throws Exception {
        when(userClient.list()).thenReturn(ResponseEntity.ok("[]"));

        mvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userClient).list();
    }

    @Test
    @DisplayName("PATCH /users/{id} — 200 OK")
    void patch_200() throws Exception {
        var dto = new UserUpdateDto("NewName", "new@example.com");
        when(userClient.patch(7L, dto))
                .thenReturn(ResponseEntity.ok("{\"id\":7,\"name\":\"NewName\"}"));

        mvc.perform(patch("/users/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(7)));

        verify(userClient).patch(7L, dto);
    }

    @Test
    @DisplayName("DELETE /users/{id} — 200 OK")
    void delete_200() throws Exception {
        when(userClient.delete(9L)).thenReturn(ResponseEntity.ok().build());

        mvc.perform(delete("/users/{id}", 9L))
                .andExpect(status().isOk());

        verify(userClient).delete(9L);
    }
}