package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.CommentResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** @JsonTest for CommentResponse: verifies ISO-8601 date format. */
@JsonTest
@DisplayName("CommentResponse JSON")
class CommentResponseJsonTest {

    @Autowired private JacksonTester<CommentResponse> json;
    @Autowired private ObjectMapper om;

    @Test
    @DisplayName("Serialize created as ISO-8601 without timezone")
    void serialize_created_iso8601() throws Exception {
        var dto = new CommentResponse(10L, "Great!", 7L, "Booker",
                LocalDateTime.of(2030, 1, 2, 3, 4, 5));

        var content = this.json.write(dto);

        assertThat(content).extractingJsonPathNumberValue("$.id").isEqualTo(10);
        assertThat(content).extractingJsonPathStringValue("$.text").isEqualTo("Great!");
        assertThat(content).extractingJsonPathStringValue("$.authorName").isEqualTo("Booker");
        assertThat(content).extractingJsonPathStringValue("$.created")
                .isEqualTo("2030-01-02T03:04:05");
    }
}