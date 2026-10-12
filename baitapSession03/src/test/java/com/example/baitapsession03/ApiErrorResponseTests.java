package com.example.baitapsession03;

import com.example.baitapsession03.service.InstructorService;
import com.example.baitapsession03.model.Instructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiErrorResponseTests {

    @LocalServerPort
    private int port;

    @Autowired
    private JsonMapper jsonMapper;

    // Chi thay bean trong lop test nay de kiem tra response khi co loi bat ngo.
    @MockitoBean
    private InstructorService instructorService;

    @Test
    void unknownRouteReturnsWrapped404() throws Exception {
        assertFailure(send("GET", "/route-does-not-exist", null, null), 404);
    }

    @Test
    void unsupportedMethodReturnsWrapped405AndPreservesAllowHeader() throws Exception {
        HttpResponse<String> response = send("PATCH", "/courses", "{}", "application/json");
        assertFailure(response, 405);
        assertTrue(response.headers().firstValue("Allow").orElse("").contains("GET"));
    }

    @Test
    void unsupportedContentTypeReturnsWrapped415() throws Exception {
        for (String route : new String[]{"/instructors", "/courses", "/enrollments"}) {
            assertFailure(send("POST", route, "plain text", "text/plain"), 415);
        }
    }

    @Test
    void unexpectedFailureReturnsWrapped500WithoutInternalDetails() throws Exception {
        when(instructorService.getAllInstructors())
                .thenThrow(new IllegalStateException("private-database-information"));
        HttpResponse<String> response = send("GET", "/instructors", null, null);
        assertFailure(response, 500);
        assertFalse(response.body().contains("private-database-information"));
        assertFalse(response.body().contains("IllegalStateException"));
    }

    @Test
    void unexpectedDetailUpdateAndDeleteFailuresAreNotMistakenFor404() throws Exception {
        when(instructorService.getInstructorById(1L))
                .thenThrow(new IllegalStateException("unexpected-lookup-error"));
        when(instructorService.updateInstructor(eq(1L), any(Instructor.class)))
                .thenThrow(new IllegalStateException("unexpected-update-error"));
        when(instructorService.deleteInstructorById(1L))
                .thenThrow(new IllegalStateException("unexpected-delete-error"));

        assertFailure(send("GET", "/instructors/1", null, null), 500);
        assertFailure(send("PUT", "/instructors/1",
                "{\"name\":\"Test\",\"email\":\"test@example.com\"}", "application/json"), 500);
        assertFailure(send("DELETE", "/instructors/1", null, null), 500);
    }

    private void assertFailure(HttpResponse<String> response, int status) {
        assertEquals(status, response.statusCode(), response.body());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        JsonNode wrapper = jsonMapper.readTree(response.body());
        assertEquals(3, wrapper.size());
        assertFalse(wrapper.get("success").booleanValue());
        assertFalse(wrapper.get("message").stringValue().isBlank());
        assertNotNull(wrapper.get("data"));
        assertTrue(wrapper.get("data").isNull());
    }

    private HttpResponse<String> send(String method, String path, String body, String contentType)
            throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest.Builder request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + path))
                    .timeout(Duration.ofSeconds(10));
            if (contentType != null) {
                request.header("Content-Type", contentType);
            }
            request.method(method, body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body));
            return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
