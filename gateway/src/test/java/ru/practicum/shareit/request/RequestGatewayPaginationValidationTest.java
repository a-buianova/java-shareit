package ru.practicum.shareit.request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.controller.RequestController;

import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pagination/params validation tests for GET /requests/all (gateway). */
@WebMvcTest(controllers = RequestController.class)
@ActiveProfiles("test")
@DisplayName("RequestGateway — pagination validation")
class RequestGatewayPaginationValidationTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;

    @MockBean RequestClient requestClient;

    @Test
    @DisplayName("GET /requests/all: from < 0 -> 400")
    void all_fromNegative_400() throws Exception {
        mvc.perform(get("/requests/all")
                        .param("from", "-1")
                        .param("size", "10")
                        .header(HDR, 1L))
                .andExpect(status().isBadRequest());

        Mockito.verify(requestClient, never())
                .findAllExceptUser(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    @DisplayName("GET /requests/all: size <= 0 -> 400")
    void all_sizeNonPositive_400() throws Exception {
        mvc.perform(get("/requests/all")
                        .param("from", "0")
                        .param("size", "0")
                        .header(HDR, 1L))
                .andExpect(status().isBadRequest());

        Mockito.verify(requestClient, never())
                .findAllExceptUser(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
    }
}