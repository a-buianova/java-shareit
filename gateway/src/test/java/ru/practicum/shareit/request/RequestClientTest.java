package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(RequestClient.class)
@Import(RequestClient.class)
@DisplayName("RequestClientTest")
class RequestClientTest {

    @Autowired RequestClient client;
    @Autowired MockServerRestTemplateCustomizer customizer;

    MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        server = customizer.getServer();
    }

    @Test
    @DisplayName("create(): POST /requests with body")
    void create_ok() {
        server.expect(requestTo("http://localhost:9090/requests"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        var dto = new ItemRequestCreateDto("Need a drill");
        client.create(1L, dto);

        server.verify();
    }

    @Test
    @DisplayName("findOwn(): GET /requests")
    void findOwn_ok() {
        server.expect(requestTo("http://localhost:9090/requests"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        client.findOwn(1L);

        server.verify();
    }

    @Test
    @DisplayName("findAllExceptUser(): GET /requests/all with pagination")
    void findAllExceptUser_ok() {
        server.expect(requestTo("http://localhost:9090/requests/all?from=0&size=2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        client.findAllExceptUser(1L, 0, 2);

        server.verify();
    }

    @Test
    @DisplayName("getById(): GET /requests/{id}")
    void getById_ok() {
        server.expect(requestTo("http://localhost:9090/requests/5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":5}", MediaType.APPLICATION_JSON));

        client.getById(1L, 5L);

        server.verify();
    }
}