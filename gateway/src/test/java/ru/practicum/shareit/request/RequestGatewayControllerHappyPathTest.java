package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.controller.RequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Happy-path tests for RequestController (gateway). */
@WebMvcTest(controllers = RequestController.class)
@ActiveProfiles("test")
@DisplayName("RequestGatewayController — happy path")
class RequestGatewayControllerHappyPathTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean RequestClient requestClient;

    @Test
    @DisplayName("POST /requests — 201 Created; тело ответа проксируется")
    void create_201() throws Exception {
        var dto = new ItemRequestCreateDto("Need a drill");

        when(requestClient.create(eq(7L), any(ItemRequestCreateDto.class)))
                .thenReturn(ResponseEntity.status(201).body("""
                        {"id":1,"description":"Need a drill","requestorId":7,"created":"2030-01-01T00:00:00Z","items":[]}
                        """));

        mvc.perform(post("/requests")
                        .header(HDR, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.requestorId").value(7));

        verify(requestClient).create(eq(7L), ArgumentMatchers.any(ItemRequestCreateDto.class));
    }

    @Test
    @DisplayName("GET /requests — 200 OK (own)")
    void findOwn_200() throws Exception {
        when(requestClient.findOwn(7L)).thenReturn(ResponseEntity.ok("""
                [{"id":1,"description":"A"},{"id":2,"description":"B"}]
                """));

        mvc.perform(get("/requests").header(HDR, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(requestClient).findOwn(7L);
    }

    @Test
    @DisplayName("GET /requests/all — 200 OK (paged)")
    void findAllExceptUser_200() throws Exception {
        when(requestClient.findAllExceptUser(7L, 0, 2))
                .thenReturn(ResponseEntity.ok("""
                        [{"id":10},{"id":11}]
                        """));

        mvc.perform(get("/requests/all")
                        .header(HDR, 7)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(requestClient).findAllExceptUser(7L, 0, 2);
    }

    @Test
    @DisplayName("GET /requests/{id} — 200 OK")
    void getById_200() throws Exception {
        when(requestClient.getById(7L, 55L))
                .thenReturn(ResponseEntity.ok("""
                        {"id":55,"description":"one","items":[]}
                        """));

        mvc.perform(get("/requests/{id}", 55L).header(HDR, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(55));

        verify(requestClient).getById(7L, 55L);
    }
}