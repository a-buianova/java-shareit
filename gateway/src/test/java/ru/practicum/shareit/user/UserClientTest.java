package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(UserClient.class)
@Import(UserClient.class)
@DisplayName("UserClientTest")
class UserClientTest {

    @Autowired UserClient client;
    @Autowired MockServerRestTemplateCustomizer customizer;
    MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        server = customizer.getServer();
    }

    @Test
    @DisplayName("create(): POST /users")
    void create_ok() {
        server.expect(requestTo("http://localhost:9090/users"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        client.create(new UserCreateDto("Ann", "ann@example.com"));
        server.verify();
    }

    @Test
    @DisplayName("get(): GET /users/{id}")
    void get_ok() {
        server.expect(requestTo("http://localhost:9090/users/5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":5}", MediaType.APPLICATION_JSON));

        client.get(5L);
        server.verify();
    }

    @Test
    @DisplayName("list(): GET /users")
    void list_ok() {
        server.expect(requestTo("http://localhost:9090/users"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        client.list();
        server.verify();
    }

    @Test
    @DisplayName("patch(): PATCH /users/{id}")
    void patch_ok() {
        server.expect(requestTo("http://localhost:9090/users/7"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{\"id\":7}", MediaType.APPLICATION_JSON));

        client.patch(7L, new UserUpdateDto("NewName", "new@example.com"));
        server.verify();
    }

    @Test
    @DisplayName("delete(): DELETE /users/{id}")
    void delete_ok() {
        server.expect(requestTo("http://localhost:9090/users/9"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        client.delete(9L);
        server.verify();
    }
}