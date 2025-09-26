package ru.practicum.shareit.request;

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
import ru.practicum.shareit.common.web.WebConfig;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.dto.ItemRequestResponse.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse.Requester;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** WebMvc slice tests for {@link ItemRequestController}. */
@WebMvcTest(controllers = ItemRequestController.class)
@Import({WebConfig.class, UserIdArgumentResolver.class})
@DisplayName("ItemRequestControllerTest (WebMvc slice)")
class ItemRequestControllerTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean ItemRequestService service;

    @Test
    @DisplayName("POST /requests — 201 Created")
    void create_201() throws Exception {
        var dto = new ItemRequestCreateDto("Need a drill");

        var resp = new ItemRequestResponse(
                1L,
                "Need a drill",
                Instant.now(),
                new Requester(7L, "Bob"),
                List.of(new ItemShortDto(100L, "Drill", "600W", true, 1L))
        );

        Mockito.when(service.create(eq(7L), any())).thenReturn(resp);

        mvc.perform(post("/requests")
                        .header(HDR, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.created", notNullValue()))
                .andExpect(jsonPath("$.requester.id").value(7))
                .andExpect(jsonPath("$.requester.name").value("Bob"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].name").value("Drill"));
    }

    @Test
    @DisplayName("GET /requests — returns user's requests (DESC by created)")
    void findOwn_ok() throws Exception {
        var r1 = new ItemRequestResponse(1L, "A", Instant.now(),
                new Requester(7L, "U"), List.of());
        var r2 = new ItemRequestResponse(2L, "B", Instant.now().plusSeconds(1),
                new Requester(7L, "U"), List.of());

        Mockito.when(service.findOwn(7L)).thenReturn(List.of(r2, r1));

        mvc.perform(get("/requests").header(HDR, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[0].requester.id").value(7));
    }

    @Test
    @DisplayName("GET /requests/all — returns paged list")
    void findAllExceptUser_ok() throws Exception {
        Mockito.when(service.findAllExceptUser(eq(7L), eq(0), eq(2)))
                .thenReturn(List.of(
                        new ItemRequestResponse(10L, "x", Instant.now(),
                                new Requester(8L, "A"), List.of()),
                        new ItemRequestResponse(11L, "y", Instant.now(),
                                new Requester(9L, "B"), List.of())
                ));

        mvc.perform(get("/requests/all")
                        .header(HDR, 7)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].requester.id").value(8))
                .andExpect(jsonPath("$[1].requester.id").value(9));
    }

    @Test
    @DisplayName("GET /requests/{id} — 200 OK")
    void getById_ok() throws Exception {
        Mockito.when(service.getById(7L, 55L))
                .thenReturn(new ItemRequestResponse(55L, "one", Instant.now(),
                        new Requester(8L, "Alice"), List.of()));

        mvc.perform(get("/requests/{id}", 55).header(HDR, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(55))
                .andExpect(jsonPath("$.requester.id").value(8))
                .andExpect(jsonPath("$.requester.name").value("Alice"));
    }

    @Test
    @DisplayName("GET /requests/{id} — 404 Not Found")
    void getById_404() throws Exception {
        Mockito.when(service.getById(7L, 9999L))
                .thenThrow(new ru.practicum.shareit.common.exception.NotFoundException("not found"));

        mvc.perform(get("/requests/{id}", 9999).header(HDR, 7))
                .andExpect(status().isNotFound());
    }

}