package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.web.UserIdArgumentResolver;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Изолированный веб-тест контроллера предметов.
 * Включаем наш аргумент-резолвер, чтобы подтянулся X-Sharer-User-Id -> Long.
 */
@WebMvcTest(controllers = ItemController.class)
@Import(UserIdArgumentResolver.class)
class ItemControllerMockMvcTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean ItemService service;

    private static final String HDR = "X-Sharer-User-Id";

    @Test
    @DisplayName("POST /items -> 201 Created, header required")
    void create_ok() throws Exception {
        Mockito.when(service.create(eq(7L), any(ItemCreateDto.class)))
                .thenReturn(new ItemResponse(100L, "Drill", "600W", true));

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HDR, "7")
                        .content(om.writeValueAsString(new ItemCreateDto("Drill", "600W", true, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    @DisplayName("POST /items -> 400 when header missing")
    void create_missingHeader_400() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new ItemCreateDto("Drill", "600W", true, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Missing X-Sharer-User-Id")));
    }

    @Test
    @DisplayName("POST /items -> 404 when owner not found (сервис кидает NoSuchElementException)")
    void create_ownerNotFound_404() throws Exception {
        Mockito.when(service.create(eq(9L), any(ItemCreateDto.class)))
                .thenThrow(new NoSuchElementException("owner not found"));

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HDR, "9")
                        .content(om.writeValueAsString(new ItemCreateDto("Drill", "600W", true, null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("owner not found"));
    }

    @Test
    @DisplayName("GET /items/{id} -> 200")
    void get_ok() throws Exception {
        Mockito.when(service.get(5L))
                .thenReturn(new ItemResponse(5L, "Saw", "1700W", true));

        mvc.perform(get("/items/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Saw"));
    }

    @Test
    @DisplayName("GET /items -> 200 list owner items")
    void listOwner_ok() throws Exception {
        Mockito.when(service.listOwnerItems(11L))
                .thenReturn(List.of(new ItemResponse(1L, "A", "d", true)));

        mvc.perform(get("/items")
                        .header(HDR, "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("PATCH /items/{id} -> 403 when not owner (сервис кидает SecurityException)")
    void patch_forbidden_403() throws Exception {
        Mockito.when(service.patch(eq(2L), eq(10L), any(ItemUpdateDto.class)))
                .thenThrow(new SecurityException("forbidden: not an owner"));

        mvc.perform(patch("/items/10")
                        .header(HDR, "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new ItemUpdateDto("Hack", null, null))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("forbidden: not an owner"));
    }

    @Test
    @DisplayName("GET /items/search?text=foo -> 200")
    void search_ok() throws Exception {
        Mockito.when(service.search("drill"))
                .thenReturn(List.of(new ItemResponse(7L, "Drill", "600W", true)));

        mvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    @DisplayName("GET /items/search without text -> 400")
    void search_missingParam_400() throws Exception {
        mvc.perform(get("/items/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Missing parameter")));
    }
}