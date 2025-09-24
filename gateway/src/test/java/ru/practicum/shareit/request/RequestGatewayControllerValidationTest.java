package ru.practicum.shareit.request;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.controller.RequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Validation tests for RequestController (gateway). */
@WebMvcTest(controllers = RequestController.class)
@ActiveProfiles("test")
@DisplayName("RequestGatewayController — validation")
class RequestGatewayControllerValidationTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean RequestClient requestClient;

    @Test
    @DisplayName("POST /requests — 400, когда description пустой")
    void create_blankDescription_400() throws Exception {
        var bad = new ItemRequestCreateDto("   ");

        mvc.perform(post("/requests")
                        .header(HDR, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest());

        Mockito.verify(requestClient, never()).create(Mockito.anyLong(), Mockito.any());
    }

    @Test
    @DisplayName("Отсутствует X-Sharer-User-Id — 400 (все эндпоинты)")
    void missingHeader_400() throws Exception {
        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new ItemRequestCreateDto("ok"))))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/requests/all").param("from", "0").param("size", "10"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/requests/{id}", 1L))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(requestClient);
    }
}