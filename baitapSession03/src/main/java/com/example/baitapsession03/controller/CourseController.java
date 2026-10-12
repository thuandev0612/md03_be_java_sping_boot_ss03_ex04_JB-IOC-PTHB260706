package com.example.baitapsession03.controller;

import com.example.baitapsession03.dto.ApiResponse;
import com.example.baitapsession03.exception.ResourceNotFoundException;
import com.example.baitapsession03.model.Course;
import com.example.baitapsession03.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Course>>> getAllCourses() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách khóa học thành công.", courseService.getAllCourses()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> getCourseById(@PathVariable("id") Long id) {
        try {
            Course course = courseService.getCourseById(id);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khóa học thành công.", course));
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(exception.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Course>> createCourse(@RequestBody Course course) {
        Course created = courseService.createCourse(course);
        return ResponseEntity.created(URI.create("/courses/" + created.getId()))
                .body(ApiResponse.success("Tạo khóa học thành công.", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(
            @PathVariable("id") Long id, @RequestBody Course course) {
        try {
            Course updated = courseService.updateCourse(id, course);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật khóa học thành công.", updated));
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(exception.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> deleteCourseById(@PathVariable("id") Long id) {
        try {
            Course deleted = courseService.deleteCourseById(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa khóa học thành công.", deleted));
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(exception.getMessage()));
        }
    }
}
