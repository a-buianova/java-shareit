package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.web.UserIdArgumentResolver;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.service.ItemService;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer validation tests for {@link ItemController}.
 */
@WebMvcTest(controllers = ItemController.class)
@Import(UserIdArgumentResolver.class)
class ItemControllerValidationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockBean
    private ItemService service;

    private static final String HDR = "X-Sharer-User-Id";

    @Test
    @DisplayName("POST /items -> 400 when name is blank")
    void create_blankName_returns400() throws Exception {
        var dto = new ItemCreateDto("   ", "desc", true, null);

        mvc.perform(post("/items")
                        .header(HDR, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("name")));
    }

    @Test
    @DisplayName("POST /items -> 400 when description is blank")
    void create_blankDescription_returns400() throws Exception {
        var dto = new ItemCreateDto("Drill", "   ", true, null);

        mvc.perform(post("/items")
                        .header(HDR, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("description")));
    }

    @Test
    @DisplayName("POST /items -> 400 when available is null")
    void create_nullAvailable_returns400() throws Exception {
        var dto = new ItemCreateDto("Drill", "d", null, null);

        mvc.perform(post("/items")
                        .header(HDR, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("available")));
    }

    @Test
    @DisplayName("POST /items -> 400 when X-Sharer-User-Id header is missing")
    void create_missingHeader_returns400() throws Exception {
        var dto = new ItemCreateDto("Drill", "d", true, null);

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Missing X-Sharer-User-Id")));
    }
}