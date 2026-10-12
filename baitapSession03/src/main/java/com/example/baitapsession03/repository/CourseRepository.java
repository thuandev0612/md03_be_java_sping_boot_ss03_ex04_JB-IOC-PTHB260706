package com.example.baitapsession03.repository;

import com.example.baitapsession03.exception.ResourceNotFoundException;
import com.example.baitapsession03.model.Course;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CourseRepository {

    private final List<Course> courses = new ArrayList<>(List.of(
            new Course(1L, "Java Fundamentals", "ACTIVE", 1L),
            new Course(2L, "Spring Boot", "ACTIVE", 1L),
            new Course(3L, "Web Fundamentals", "DRAFT", 2L)
    ));

    // Tra ban sao danh sach de GET khong bi anh huong khi request khac them/xoa.
    public synchronized List<Course> findAll() {
        return new ArrayList<>(courses);
    }

    public synchronized Optional<Course> findById(long id) {
        return courses.stream()
                .filter(course -> course.getId().longValue() == id)
                .findFirst();
    }

    public synchronized Course create(Course course) {
        if (course.getId() == null) {
            long maxId = courses.stream().mapToLong(Course::getId).max().orElse(0L);
            if (maxId == Long.MAX_VALUE) {
                throw new IllegalArgumentException("Không thể cấp thêm ID tự động.");
            }
            course.setId(maxId + 1);
        } else if (course.getId() <= 0 || findById(course.getId()).isPresent()) {
            throw new IllegalArgumentException("ID phải lớn hơn 0 và chưa tồn tại.");
        }

        courses.add(course);
        return course;
    }

    public synchronized Course update(long id, Course course) {
        Course existing = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học có ID " + id + "."));

        // Chi cap nhat khi tim thay, giu ID tren URL.
        course.setId(id);
        courses.set(courses.indexOf(existing), course);
        return course;
    }

    public synchronized Course deleteById(long id) {
        Course existing = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học có ID " + id + "."));
        courses.remove(existing);
        return existing;
    }
}
