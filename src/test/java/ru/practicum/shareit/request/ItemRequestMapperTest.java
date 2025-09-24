package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ItemRequestMapper: unit tests")
class ItemRequestMapperTest {

    private final ItemRequestMapper mapper = new ItemRequestMapper();

    @Test
    @DisplayName("toEntity(): sets description & requestor; 'created' stays null (JPA will set)")
    void toEntity_success() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Need a drill");
        User requestor = User.builder().id(10L).name("Bob").email("b@ex.com").build();

        ItemRequest entity = mapper.toEntity(dto, requestor);

        assertNotNull(entity);
        assertNull(entity.getId(), "id must be null before save");
        assertEquals("Need a drill", entity.getDescription());
        assertEquals(10L, entity.getRequestor().getId());
        assertNull(entity.getCreated(), "created must be null before persist");
    }

    @Test
    @DisplayName("toResponse(): flattens requestorId, maps items, tolerates null requestor")
    void toResponse_success_andNullTolerant() {
        User requestor = User.builder().id(7L).name("Ann").email("a@ex.com").build();

        ItemRequest withRequestor = ItemRequest.builder()
                .id(5L)
                .description("Need a drill")
                .requestor(requestor)
                .build();

        ItemRequest withoutRequestor = ItemRequest.builder()
                .id(6L)
                .description("Need a hammer")
                .requestor(null)
                .build();

        Item item = Item.builder()
                .id(100L)
                .name("Drill")
                .description("600W")
                .available(true)
                .build();

        ItemRequestResponse dto1 = mapper.toResponse(withRequestor, List.of(item));
        ItemRequestResponse dto2 = mapper.toResponse(withoutRequestor, List.of());

        assertAll(
                () -> assertEquals(5L, dto1.id()),
                () -> assertEquals("Need a drill", dto1.description()),
                () -> assertEquals(7L, dto1.requestorId()),
                () -> assertNotNull(dto1.items()),
                () -> assertEquals(1, dto1.items().size()),
                () -> assertEquals(100L, dto1.items().get(0).id()),
                () -> assertEquals("Drill", dto1.items().get(0).name()),
                () -> assertEquals("600W", dto1.items().get(0).description()),
                () -> assertTrue(dto1.items().get(0).available()),
                () -> assertNull(dto1.items().get(0).requestId()),
                () -> assertEquals(6L, dto2.id()),
                () -> assertEquals("Need a hammer", dto2.description()),
                () -> assertNull(dto2.requestorId()),
                () -> assertNotNull(dto2.items()),
                () -> assertTrue(dto2.items().isEmpty())
        );
    }
}