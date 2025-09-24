package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pagination/params validation for GET /requests/all
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ItemRequest: pagination/state validation")
class ItemRequestPaginationValidationMvcTest {

    @Autowired MockMvc mvc;

    private static final String HDR = "X-Sharer-User-Id";

    @Test
    @DisplayName("GET /requests/all: from < 0 -> 400")
    void all_fromNegative_400() throws Exception {
        mvc.perform(get("/requests/all")
                        .param("from", "-1")
                        .param("size", "10")
                        .header(HDR, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests/all: size <= 0 -> 400")
    void all_sizeNonPositive_400() throws Exception {
        mvc.perform(get("/requests/all")
                        .param("from", "0")
                        .param("size", "0")
                        .header(HDR, 1L))
                .andExpect(status().isBadRequest());
    }
}