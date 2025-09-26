package ru.practicum.shareit.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.controller.ItemController;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** WebMvc tests for ItemController pagination params. */
@WebMvcTest(controllers = ItemController.class)
@ActiveProfiles("test")
@DisplayName("ItemGatewayController — pagination validation")
class ItemGatewayPaginationValidationTest {

    private static final String HDR = "X-Sharer-User-Id";
    @Autowired MockMvc mvc;
    @MockBean ItemClient itemClient;

    @Test
    @DisplayName("GET /items: from < 0 -> 400")
    void list_fromNegative_400() throws Exception {
        mvc.perform(get("/items").param("from", "-1").param("size", "10").header(HDR, 1))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(itemClient);
    }

    @Test
    @DisplayName("GET /items: size <= 0 -> 400")
    void list_sizeNonPositive_400() throws Exception {
        mvc.perform(get("/items").param("from", "0").param("size", "0").header(HDR, 1))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(itemClient);
    }

    @Test
    @DisplayName("GET /items/search: from < 0 -> 400")
    void search_fromNegative_400() throws Exception {
        mvc.perform(get("/items/search").param("text", "x").param("from", "-1").param("size", "10").header(HDR, 1))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(itemClient);
    }

    @Test
    @DisplayName("GET /items/search: size <= 0 -> 400")
    void search_sizeNonPositive_400() throws Exception {
        mvc.perform(get("/items/search").param("text", "x").param("from", "0").param("size", "0").header(HDR, 1))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(itemClient);
    }
}