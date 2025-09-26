package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** WebMvc slice tests for ItemController (validation). */
@WebMvcTest(controllers = ItemController.class)
@ActiveProfiles("test")
@DisplayName("ItemGatewayController — validation")
class ItemGatewayControllerValidationTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean ItemClient itemClient;

    @Test
    @DisplayName("Missing X-Sharer-User-Id -> 400")
    void missingHeader_400() throws Exception {
        mvc.perform(get("/items/1"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(itemClient);
    }

    @Test
    @DisplayName("POST /items with blank name -> 400")
    void create_blankName_400() throws Exception {
        var dto = new ItemCreateDto("   ", "desc", true, null);

        mvc.perform(post("/items")
                        .header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    @DisplayName("POST /items with null available -> 400")
    void create_nullAvailable_400() throws Exception {
        var dto = new ItemCreateDto("Drill", "desc", null, null);

        mvc.perform(post("/items")
                        .header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    @DisplayName("PATCH /items/{id} no body -> 400 (invalid JSON)")
    void update_noBody_400() throws Exception {
        mvc.perform(patch("/items/{id}", 10)
                        .header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(itemClient);
    }

    @Test
    @DisplayName("POST /items/{id}/comment with blank text -> 400")
    void addComment_blankText_400() throws Exception {
        var dto = new CommentCreateDto("   ");
        mvc.perform(post("/items/{id}/comment", 11)
                        .header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(itemClient);
    }
}