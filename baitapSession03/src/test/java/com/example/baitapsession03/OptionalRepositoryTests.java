package com.example.baitapsession03;

import com.example.baitapsession03.exception.ResourceNotFoundException;
import com.example.baitapsession03.model.Course;
import com.example.baitapsession03.model.Enrollment;
import com.example.baitapsession03.model.Instructor;
import com.example.baitapsession03.repository.CourseRepository;
import com.example.baitapsession03.repository.EnrollmentRepository;
import com.example.baitapsession03.repository.InstructorRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OptionalRepositoryTests {

    @Test
    void instructorAbsentLookupIsEmptyAndMissingMutationsThrowWithoutChangingData() {
        InstructorRepository repository = new InstructorRepository();
        List<Instructor> before = repository.findAll();
        Instructor replacement = new Instructor(777L, "Optional teacher", "optional@example.com");

        assertTrue(repository.findById(1L).isPresent());
        assertTrue(repository.findById(999999L).isEmpty());
        assertThrows(ResourceNotFoundException.class, () -> repository.update(999999L, replacement));
        assertThrows(ResourceNotFoundException.class, () -> repository.deleteById(999999L));

        assertEquals(before, repository.findAll());
        assertEquals(777L, replacement.getId().longValue());
    }

    @Test
    void instructorUpdateAndDeleteReturnConcreteRecordsAndPreserveTargetId() {
        InstructorRepository repository = new InstructorRepository();
        int initialSize = repository.findAll().size();
        Instructor replacement = new Instructor(777L, "Optional teacher", "optional@example.com");

        Instructor updated = repository.update(1L, replacement);
        assertSame(replacement, updated);
        assertEquals(1L, updated.getId().longValue());
        assertSame(updated, repository.findById(1L).orElseThrow());
        assertEquals(initialSize, repository.findAll().size());

        assertSame(updated, repository.deleteById(1L));
        assertTrue(repository.findById(1L).isEmpty());
        assertEquals(initialSize - 1, repository.findAll().size());
        assertThrows(ResourceNotFoundException.class, () -> repository.deleteById(1L));
    }

    @Test
    void courseAbsentLookupIsEmptyAndMissingMutationsThrowWithoutChangingData() {
        CourseRepository repository = new CourseRepository();
        List<Course> before = repository.findAll();
        Course replacement = new Course(777L, "Optional course", "ACTIVE", 2L);

        assertTrue(repository.findById(1L).isPresent());
        assertTrue(repository.findById(999999L).isEmpty());
        assertThrows(ResourceNotFoundException.class, () -> repository.update(999999L, replacement));
        assertThrows(ResourceNotFoundException.class, () -> repository.deleteById(999999L));

        assertEquals(before, repository.findAll());
        assertEquals(777L, replacement.getId().longValue());
    }

    @Test
    void courseUpdateAndDeleteReturnConcreteRecordsAndPreserveTargetId() {
        CourseRepository repository = new CourseRepository();
        int initialSize = repository.findAll().size();
        Course replacement = new Course(777L, "Optional course", "ACTIVE", 2L);

        Course updated = repository.update(1L, replacement);
        assertSame(replacement, updated);
        assertEquals(1L, updated.getId().longValue());
        assertSame(updated, repository.findById(1L).orElseThrow());
        assertEquals(initialSize, repository.findAll().size());

        assertSame(updated, repository.deleteById(1L));
        assertTrue(repository.findById(1L).isEmpty());
        assertEquals(initialSize - 1, repository.findAll().size());
        assertThrows(ResourceNotFoundException.class, () -> repository.deleteById(1L));
    }

    @Test
    void enrollmentAbsentLookupIsEmptyAndMissingMutationsThrowWithoutChangingData() {
        EnrollmentRepository repository = new EnrollmentRepository();
        List<Enrollment> before = repository.findAll();
        Enrollment replacement = new Enrollment(777L, "Optional student", 2L);

        assertTrue(repository.findById(1L).isPresent());
        assertTrue(repository.findById(999999L).isEmpty());
        assertThrows(ResourceNotFoundException.class, () -> repository.update(999999L, replacement));
        assertThrows(ResourceNotFoundException.class, () -> repository.deleteById(999999L));

        assertEquals(before, repository.findAll());
        assertEquals(777L, replacement.getId().longValue());
    }

    @Test
    void enrollmentUpdateAndDeleteReturnConcreteRecordsAndPreserveTargetId() {
        EnrollmentRepository repository = new EnrollmentRepository();
        int initialSize = repository.findAll().size();
        Enrollment replacement = new Enrollment(777L, "Optional student", 2L);

        Enrollment updated = repository.update(1L, replacement);
        assertSame(replacement, updated);
        assertEquals(1L, updated.getId().longValue());
        assertSame(updated, repository.findById(1L).orElseThrow());
        assertEquals(initialSize, repository.findAll().size());

        assertSame(updated, repository.deleteById(1L));
        assertTrue(repository.findById(1L).isEmpty());
        assertEquals(initialSize - 1, repository.findAll().size());
        assertThrows(ResourceNotFoundException.class, () -> repository.deleteById(1L));
    }
}
