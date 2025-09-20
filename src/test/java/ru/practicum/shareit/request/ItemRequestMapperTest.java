package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ItemRequestMapper}.
 */
@DisplayName("ItemRequestMapper: manual mapper tests")
class ItemRequestMapperTest {

    private final ItemRequestMapper mapper = new ItemRequestMapper();

    @Test
    @DisplayName("toEntity(): sets description, requestor and created")
    void toEntity_success() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Need a drill");
        User requestor = User.builder().id(10L).name("Bob").email("b@ex.com").build();

        ItemRequest entity = mapper.toEntity(dto, requestor);

        assertNotNull(entity, "Entity must not be null");
        assertNull(entity.getId(), "Id must be null before save");
        assertEquals("Need a drill", entity.getDescription());
        assertEquals(10L, entity.getRequestor().getId());
        assertNotNull(entity.getCreated(), "Created must be set by mapper");
    }

    @Test
    @DisplayName("toResponse(): flattens requestorId; tolerates null requestor")
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

        ItemRequestResponse dto1 = mapper.toResponse(withRequestor);
        ItemRequestResponse dto2 = mapper.toResponse(withoutRequestor);

        assertAll(
                () -> assertEquals(5L, dto1.id()),
                () -> assertEquals("Need a drill", dto1.description()),
                () -> assertEquals(7L, dto1.requestorId()),
                () -> assertEquals(6L, dto2.id()),
                () -> assertEquals("Need a hammer", dto2.description()),
                () -> assertNull(dto2.requestorId(), "requestorId must be null when requestor is null")
        );
    }
}