package com.example.baitapsession03;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CrudApiTests {

    @LocalServerPort
    private int port;

    @Autowired
    private JsonMapper jsonMapper;

    private static final String INSTRUCTOR = """
            {"name":"New teacher","email":"teacher@example.com"}
            """;
    private static final String COURSE = """
            {"title":"New course","status":"DRAFT","instructorId":1}
            """;
    private static final String ENROLLMENT = """
            {"studentName":"New student","courseId":1}
            """;

    @Test
    void instructorSupportsCompleteCrudLifecycle() throws Exception {
        exerciseCrud("/instructors", INSTRUCTOR,
                "{\"name\":\"Updated teacher\",\"email\":\"updated@example.com\"}", "name", "email");
    }

    @Test
    void courseSupportsCompleteCrudLifecycle() throws Exception {
        exerciseCrud("/courses", COURSE,
                "{\"title\":\"Updated course\",\"status\":\"ACTIVE\",\"instructorId\":2}",
                "title", "status", "instructorId");
    }

    @Test
    void enrollmentSupportsCompleteCrudLifecycle() throws Exception {
        exerciseCrud("/enrollments", ENROLLMENT,
                "{\"studentName\":\"Updated student\",\"courseId\":2}", "studentName", "courseId");
    }

    @Test
    void absentRecordsReturn404WithoutChangingLists() throws Exception {
        for (String route : new String[]{"/instructors", "/courses", "/enrollments"}) {
            JsonNode before = getJson(route);
            expectStatus("GET", route + "/999999", null, 404);
            expectStatus("PUT", route + "/999999", validBody(route), 404);
            expectStatus("DELETE", route + "/999999", null, 404);
            assertEquals(before, getJson(route));
        }
    }

    @Test
    void invalidBodiesReturn400AndNeverPartiallyUpdateRecords() throws Exception {
        String[][] invalidRequests = {
                {"/instructors", "{}"},
                {"/instructors", "{\"name\":\" \",\"email\":\"test@example.com\"}"},
                {"/instructors", "{\"name\":\"Test\",\"email\":\"invalid\"}"},
                {"/courses", "{}"},
                {"/courses", "{\"title\":\" \",\"status\":\"ACTIVE\",\"instructorId\":1}"},
                {"/courses", "{\"title\":\"Test\",\"status\":\" \",\"instructorId\":1}"},
                {"/courses", "{\"title\":\"Test\",\"status\":\"ACTIVE\",\"instructorId\":999999}"},
                {"/courses", "{\"title\":\"Test\",\"status\":\"ACTIVE\",\"instructorId\":null}"},
                {"/enrollments", "{}"},
                {"/enrollments", "{\"studentName\":\" \",\"courseId\":1}"},
                {"/enrollments", "{\"studentName\":\"Test\",\"courseId\":999999}"},
                {"/enrollments", "{\"studentName\":\"Test\",\"courseId\":null}"}
        };
        for (String[] invalid : invalidRequests) {
            JsonNode before = getJson(invalid[0]);
            assertValidationError("POST", invalid[0], invalid[1]);
            assertValidationError("PUT", invalid[0] + "/1", invalid[1]);
            assertEquals(before, getJson(invalid[0]));
        }
    }

    @Test
    void invalidIdsMalformedJsonAndMismatchedBodyIdReturn400() throws Exception {
        for (String route : new String[]{"/instructors", "/courses", "/enrollments"}) {
            JsonNode before = getJson(route);
            for (String id : new String[]{"0", "-1", "abc"}) {
                expectStatus("GET", route + "/" + id, null, 400);
                expectStatus("PUT", route + "/" + id, validBody(route), 400);
                expectStatus("DELETE", route + "/" + id, null, 400);
            }
            for (String body : new String[]{"", "null", "{"}) {
                expectStatus("POST", route, body, 400);
                expectStatus("PUT", route + "/1", body, 400);
            }
            assertValidationError("PUT", route + "/1", withId(validBody(route), 2));
            assertEquals(before, getJson(route));
        }
    }

    @Test
    void manualAndGeneratedIdsStayUniqueAndDuplicateCreatesReturn400() throws Exception {
        for (String route : new String[]{"/instructors", "/courses", "/enrollments"}) {
            JsonNode before = getJson(route);
            for (long invalidId : new long[]{0, -1, 1}) {
                assertValidationError("POST", route, withId(validBody(route), invalidId));
            }
            assertEquals(before, getJson(route));

            JsonNode manual = create(route, withId(validBody(route), 1000));
            assertEquals(1000, manual.get("id").longValue());
            JsonNode generated = create(route, validBody(route));
            assertTrue(generated.get("id").longValue() > 1000);

            expectStatus("DELETE", route + "/" + manual.get("id").longValue(), null, 200);
            expectStatus("DELETE", route + "/" + generated.get("id").longValue(), null, 200);
            assertEquals(before, getJson(route));
        }
    }

    @Test
    void referencedParentsCannotBeDeletedUntilChildrenAreReassigned() throws Exception {
        long instructorId = create("/instructors", INSTRUCTOR).get("id").longValue();
        long courseId = create("/courses", """
                {"title":"Linked course","status":"ACTIVE","instructorId":%d}
                """.formatted(instructorId)).get("id").longValue();
        long enrollmentId = create("/enrollments", """
                {"studentName":"Linked student","courseId":%d}
                """.formatted(courseId)).get("id").longValue();

        assertValidationError("DELETE", "/instructors/" + instructorId, null);
        assertValidationError("DELETE", "/courses/" + courseId, null);
        assertEquals(instructorId, getJson("/courses/" + courseId).get("instructorId").longValue());
        assertEquals(courseId, getJson("/enrollments/" + enrollmentId).get("courseId").longValue());

        expectStatus("PUT", "/courses/" + courseId, COURSE, 200);
        expectStatus("DELETE", "/instructors/" + instructorId, null, 200);
        expectStatus("PUT", "/enrollments/" + enrollmentId, ENROLLMENT, 200);
        expectStatus("DELETE", "/courses/" + courseId, null, 200);
        expectStatus("DELETE", "/enrollments/" + enrollmentId, null, 200);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void deletingChildrenThenParentsLeavesEmptyListsWith200() throws Exception {
        for (String route : new String[]{"/enrollments", "/courses", "/instructors"}) {
            for (JsonNode record : getJson(route)) {
                expectStatus("DELETE", route + "/" + record.get("id").longValue(), null, 200);
            }
            JsonNode empty = getJson(route);
            assertTrue(empty.isArray());
            assertEquals(0, empty.size());
        }
    }

    private void exerciseCrud(String route, String initialBody, String updatedBody, String... fields)
            throws Exception {
        JsonNode before = getJson(route);
        JsonNode created = create(route, initialBody);
        long id = created.get("id").longValue();
        String detailPath = route + "/" + id;
        assertTrue(id > 0);
        assertFields(initialBody, created, fields);
        assertEquals(before.size() + 1, getJson(route).size());
        assertEquals(created, getJson(detailPath));

        JsonNode updated = jsonMapper.readTree(expectStatus("PUT", detailPath, updatedBody, 200).body()).get("data");
        assertEquals(id, updated.get("id").longValue());
        assertFields(updatedBody, updated, fields);
        assertEquals(updated, getJson(detailPath));
        assertEquals(before.size() + 1, getJson(route).size());

        JsonNode deleted = jsonMapper.readTree(expectStatus("DELETE", detailPath, null, 200).body()).get("data");
        assertEquals(updated, deleted);
        expectStatus("GET", detailPath, null, 404);
        expectStatus("PUT", detailPath, updatedBody, 404);
        expectStatus("DELETE", detailPath, null, 404);
        assertEquals(before, getJson(route));
    }

    private JsonNode create(String route, String body) throws Exception {
        HttpResponse<String> response = expectStatus("POST", route, body, 201);
        JsonNode created = jsonMapper.readTree(response.body()).get("data");
        assertEquals(route + "/" + created.get("id").longValue(),
                response.headers().firstValue("Location").orElse(""));
        return created;
    }

    private void assertFields(String expectedBody, JsonNode actual, String... fields) {
        JsonNode expected = jsonMapper.readTree(expectedBody);
        for (String field : fields) {
            assertEquals(expected.get(field), actual.get(field), field);
        }
    }

    private void assertValidationError(String method, String path, String body) throws Exception {
        HttpResponse<String> response = expectStatus(method, path, body, 400);
        JsonNode error = jsonMapper.readTree(response.body());
        assertNotNull(error.get("message"));
        assertFalse(error.get("message").stringValue().isBlank());
    }

    private String withId(String body, long id) {
        return "{\"id\":" + id + "," + body.strip().substring(1);
    }

    private String validBody(String route) {
        return switch (route) {
            case "/instructors" -> INSTRUCTOR;
            case "/courses" -> COURSE;
            case "/enrollments" -> ENROLLMENT;
            default -> throw new IllegalArgumentException("Unknown route");
        };
    }

    private JsonNode getJson(String path) throws Exception {
        return jsonMapper.readTree(expectStatus("GET", path, null, 200).body()).get("data");
    }

    private HttpResponse<String> expectStatus(String method, String path, String body, int expected)
            throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + path))
                    .timeout(Duration.ofSeconds(10));
            if (body == null) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                builder.header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(body));
            }
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(expected, response.statusCode(), method + " " + path + ": " + response.body());
            assertTrue(response.headers().firstValue("Content-Type").orElse("")
                    .startsWith("application/json"));
            JsonNode wrapper = jsonMapper.readTree(response.body());
            assertTrue(wrapper.isObject());
            assertEquals(3, wrapper.size(), "Response must contain exactly success, message and data");
            assertNotNull(wrapper.get("success"));
            assertTrue(wrapper.get("success").isBoolean());
            assertEquals(expected < 400, wrapper.get("success").booleanValue());
            assertNotNull(wrapper.get("message"));
            assertTrue(wrapper.get("message").isString());
            assertFalse(wrapper.get("message").stringValue().isBlank());
            assertNotNull(wrapper.get("data"), "data must be present even when null");
            if (expected >= 400) {
                assertTrue(wrapper.get("data").isNull());
            } else {
                assertFalse(wrapper.get("data").isNull());
            }
            return response;
        }
    }
}
