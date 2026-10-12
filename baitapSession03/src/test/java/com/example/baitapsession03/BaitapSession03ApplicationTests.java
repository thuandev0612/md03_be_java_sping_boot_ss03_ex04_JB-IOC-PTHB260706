package com.example.baitapsession03;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Khoi dong Spring va goi HTTP that de kiem tra bean cua ca ba nhanh.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BaitapSession03ApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void instructorsEndpointReturnsCompleteSeedData() throws Exception {
        assertSeedData(getJson("/instructors"), "id", "name", "email");
    }

    @Test
    void coursesEndpointReturnsCompleteSeedData() throws Exception {
        assertSeedData(getJson("/courses"), "id", "title", "status", "instructorId");
    }

    @Test
    void enrollmentsEndpointReturnsCompleteSeedData() throws Exception {
        assertSeedData(getJson("/enrollments"), "id", "studentName", "courseId");
    }

    @Test
    void seededReferencesExistAndDemonstrateOneToManyRelationships() throws Exception {
        Set<Long> instructorIds = new HashSet<>();
        for (JsonNode instructor : getJson("/instructors")) {
            instructorIds.add(instructor.get("id").longValue());
        }

        Set<Long> courseIds = new HashSet<>();
        Map<Long, Integer> coursesPerInstructor = new HashMap<>();
        for (JsonNode course : getJson("/courses")) {
            long instructorId = course.get("instructorId").longValue();
            assertTrue(instructorIds.contains(instructorId), "Course must refer to an existing instructor");
            courseIds.add(course.get("id").longValue());
            coursesPerInstructor.merge(instructorId, 1, Integer::sum);
        }

        Map<Long, Integer> enrollmentsPerCourse = new HashMap<>();
        for (JsonNode enrollment : getJson("/enrollments")) {
            long courseId = enrollment.get("courseId").longValue();
            assertTrue(courseIds.contains(courseId), "Enrollment must refer to an existing course");
            enrollmentsPerCourse.merge(courseId, 1, Integer::sum);
        }

        assertTrue(coursesPerInstructor.values().stream().anyMatch(count -> count >= 2));
        assertTrue(enrollmentsPerCourse.values().stream().anyMatch(count -> count >= 2));
    }

    private void assertSeedData(JsonNode records, String... fields) {
        assertTrue(records.isArray());
        assertTrue(records.size() >= 2, "Each model must have at least two records");
        Set<Long> ids = new HashSet<>();
        for (JsonNode record : records) {
            for (String field : fields) {
                JsonNode value = record.get(field);
                assertNotNull(value, "Missing field: " + field);
                assertFalse(value.isNull(), "Null field: " + field);
                if (value.isString()) {
                    assertFalse(value.stringValue().isBlank(), "Blank field: " + field);
                }
            }
            assertTrue(record.get("id").isIntegralNumber());
            assertTrue(record.get("id").longValue() > 0);
            assertTrue(ids.add(record.get("id").longValue()), "IDs must be unique within each model");
        }
    }

    private JsonNode getJson(String path) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + path))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode(), response.body());
            assertTrue(response.headers().firstValue("Content-Type").orElse("")
                    .startsWith("application/json"));
            JsonNode wrapper = jsonMapper.readTree(response.body());
            assertEquals(3, wrapper.size());
            assertTrue(wrapper.get("success").booleanValue());
            assertFalse(wrapper.get("message").stringValue().isBlank());
            assertNotNull(wrapper.get("data"));
            return wrapper.get("data");
        }
    }
}
