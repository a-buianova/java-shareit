package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Happy-path tests for ItemController (gateway). */
@WebMvcTest(controllers = ItemController.class)
@ActiveProfiles("test")
@DisplayName("ItemGatewayController — happy path")
class ItemGatewayControllerHappyPathTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean ItemClient itemClient;

    @Test
    @DisplayName("POST /items — 201 Created (проксируется тело ответа клиента)")
    void create_item_201() throws Exception {
        var dto = new ItemCreateDto("Drill", "600W", true, null);

        when(itemClient.create(eq(10L), any(ItemCreateDto.class)))
                .thenReturn(ResponseEntity.status(201).body(
                        """
                        {"id":1,"name":"Drill","description":"600W","available":true}
                        """
                ));

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HDR, 10)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Drill")));

        verify(itemClient).create(eq(10L), ArgumentMatchers.any(ItemCreateDto.class));
    }

    @Test
    @DisplayName("PATCH /items/{id} — 200 OK")
    void patch_item_200() throws Exception {
        var dto = new ItemUpdateDto("New name", null, null);

        when(itemClient.update(eq(10L), eq(5L), any(ItemUpdateDto.class)))
                .thenReturn(ResponseEntity.ok("""
                        {"id":5,"name":"New name","description":"600W","available":true}
                        """));

        mvc.perform(patch("/items/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HDR, 10)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("New name"));

        verify(itemClient).update(eq(10L), eq(5L), any(ItemUpdateDto.class));
    }

    @Test
    @DisplayName("GET /items/{id} — 200 OK")
    void get_item_200() throws Exception {
        when(itemClient.getById(eq(9L), eq(5L)))
                .thenReturn(ResponseEntity.ok("""
                        {"id":5,"name":"Drill","description":"600W","available":true}
                        """));

        mvc.perform(get("/items/{id}", 5L).header(HDR, 9))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(itemClient).getById(9L, 5L);
    }

    @Test
    @DisplayName("GET /items — owner list 200 OK")
    void list_owner_items_200() throws Exception {
        when(itemClient.listOwnerItems(eq(9L), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok("[]"));

        mvc.perform(get("/items").param("from", "0").param("size", "10").header(HDR, 9))
                .andExpect(status().isOk());

        verify(itemClient).listOwnerItems(9L, 0, 10);
    }

    @Test
    @DisplayName("GET /items/search — 200 OK")
    void search_items_200() throws Exception {
        when(itemClient.search(eq(7L), eq("drill"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok("[]"));

        mvc.perform(get("/items/search")
                        .param("text", "drill")
                        .param("from", "0")
                        .param("size", "10")
                        .header(HDR, 7))
                .andExpect(status().isOk());

        verify(itemClient).search(7L, "drill", 0, 10);
    }

    @Test
    @DisplayName("POST /items/{id}/comment — 201 Created")
    void add_comment_201() throws Exception {
        var dto = new CommentCreateDto("Nice tool!");

        when(itemClient.addComment(eq(12L), eq(5L), any(CommentCreateDto.class)))
                .thenReturn(ResponseEntity.status(201).body("""
                        {"id":1,"text":"Nice tool!","authorName":"User"}
                        """));

        mvc.perform(post("/items/{id}/comment", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HDR, 12)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Nice tool!"));

        verify(itemClient).addComment(eq(12L), eq(5L), any(CommentCreateDto.class));
    }
}