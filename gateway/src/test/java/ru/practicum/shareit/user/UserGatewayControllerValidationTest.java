package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(UserController.class)
@ActiveProfiles("test")
@DisplayName("UserGatewayControllerValidationTest")
class UserGatewayControllerValidationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean UserClient userClient;

    @Test
    @DisplayName("POST /users — blank name -> 400")
    void create_blankName_400() throws Exception {
        var dto = new UserCreateDto("   ", "ok@example.com");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(userClient, never()).create(Mockito.any());
    }

    @Test
    @DisplayName("POST /users — invalid email -> 400")
    void create_invalidEmail_400() throws Exception {
        var dto = new UserCreateDto("Ann", "not-an-email");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(userClient, never()).create(Mockito.any());
    }

    @Test
    @DisplayName("PATCH /users/{id} — invalid email -> 400")
    void patch_invalidEmail_400() throws Exception {
        var dto = new UserUpdateDto("Name", "bad-email");

        mvc.perform(patch("/users/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(userClient, never()).patch(Mockito.anyLong(), Mockito.any());
    }
}