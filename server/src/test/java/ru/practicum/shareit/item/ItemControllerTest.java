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
import ru.practicum.shareit.common.web.WebConfig;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** WebMvc slice for ItemController. */
@WebMvcTest(controllers = ItemController.class)
@Import({WebConfig.class, UserIdArgumentResolver.class})
@DisplayName("ItemControllerTest")
class ItemControllerTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean ItemService itemService;

    @Test
    @DisplayName("POST /items — 201 Created")
    void create_201() throws Exception {
        var in  = new ItemCreateDto("Saw", "Hand saw", true, null);
        var out = new ItemResponse(1L, "Saw", "Hand saw", true);

        Mockito.when(itemService.create(eq(10L), any(ItemCreateDto.class))).thenReturn(out);

        mvc.perform(post("/items")
                        .header(HDR, 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Saw"))
                .andExpect(jsonPath("$.description").value("Hand saw"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("PATCH /items/{id} — 200 OK")
    void patch_200() throws Exception {
        var in  = new ItemUpdateDto("Drill 650W", null, null);
        var out = new ItemResponse(5L, "Drill 650W", "600W", true);

        Mockito.when(itemService.patch(eq(1L), eq(5L), any(ItemUpdateDto.class))).thenReturn(out);

        mvc.perform(patch("/items/{id}", 5)
                        .header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Drill 650W"))
                .andExpect(jsonPath("$.description").value("600W"))
                .andExpect(jsonPath("$.available").value(true));
    }


    @Test
    @DisplayName("GET /items/{id} — 200 OK (details, bookings, comments)")
    void get_item_details_200() throws Exception {
        var last = new BookingShortDto(
                1L, 2L,
                Instant.now().minusSeconds(48 * 3600),
                Instant.now().minusSeconds(24 * 3600)
        );
        var next = new BookingShortDto(
                2L, 3L,
                Instant.now().plusSeconds(24 * 3600),
                Instant.now().plusSeconds(48 * 3600)
        );
        var details = new ItemDetailsResponse(
                5L, "Drill", "600W", true,
                last, next,
                List.of(new CommentResponse(10L, "Great!", 7L, "Booker", java.time.LocalDateTime.now()))
        );

        Mockito.when(itemService.get(eq(1L), eq(5L))).thenReturn(details);

        mvc.perform(get("/items/{id}", 5).header(HDR, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.lastBooking.id").value(1))
                .andExpect(jsonPath("$.lastBooking.bookerId").value(2))
                .andExpect(jsonPath("$.nextBooking.id").value(2))
                .andExpect(jsonPath("$.nextBooking.bookerId").value(3))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].text").value("Great!"));
    }

    @Test
    @DisplayName("GET /items/{id} — 200 OK без заголовка (requesterId=null)")
    void get_item_details_200_no_header() throws Exception {
        var details = new ItemDetailsResponse(
                5L, "Drill", "600W", true,
                null, null, List.of()
        );

        Mockito.when(itemService.get(isNull(), eq(5L))).thenReturn(details);

        mvc.perform(get("/items/{id}", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.comments", hasSize(0)));
    }

    @Test
    @DisplayName("GET /items — 200 OK (owner's list with details)")
    void list_owner_items_200() throws Exception {
        var details = new ItemDetailsResponse(
                5L, "Saw", "Hand saw", true, null, null, List.of()
        );

        Mockito.when(itemService.listOwnerItems(1L)).thenReturn(List.of(details));

        mvc.perform(get("/items").header(HDR, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].name").value("Saw"));
    }

    @Test
    @DisplayName("GET /items/search — 200 OK")
    void search_200() throws Exception {
        var r = new ItemResponse(7L, "Super Drill", "x", true);
        Mockito.when(itemService.search("drill")).thenReturn(List.of(r));

        mvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("Super Drill"))
                .andExpect(jsonPath("$[0].available", is(true)));
    }

    @Test
    @DisplayName("POST /items/{id}/comment — 201 Created")
    void add_comment_201() throws Exception {
        var in  = new CommentCreateDto("Great!");
        var out = new CommentResponse(10L, "Great!", 7L, "Booker", java.time.LocalDateTime.now());

        Mockito.when(itemService.addComment(eq(7L), eq(55L), any(CommentCreateDto.class)))
                .thenReturn(out);

        mvc.perform(post("/items/{itemId}/comment", 55)
                        .header(HDR, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.text").value("Great!"))
                .andExpect(jsonPath("$.authorId").value(7))
                .andExpect(jsonPath("$.authorName").value("Booker"))
                .andExpect(jsonPath("$.created").exists());
    }
}