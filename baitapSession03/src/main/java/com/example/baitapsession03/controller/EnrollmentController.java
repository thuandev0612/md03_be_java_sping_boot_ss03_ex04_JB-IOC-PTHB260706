package com.example.baitapsession03.controller;

import com.example.baitapsession03.dto.ApiResponse;
import com.example.baitapsession03.exception.ResourceNotFoundException;
import com.example.baitapsession03.model.Enrollment;
import com.example.baitapsession03.service.EnrollmentService;
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
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Enrollment>>> getAllEnrollments() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách lượt đăng ký thành công.", enrollmentService.getAllEnrollments()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Enrollment>> getEnrollmentById(@PathVariable("id") Long id) {
        try {
            Enrollment enrollment = enrollmentService.getEnrollmentById(id);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin lượt đăng ký thành công.", enrollment));
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(exception.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Enrollment>> createEnrollment(@RequestBody Enrollment enrollment) {
        Enrollment created = enrollmentService.createEnrollment(enrollment);
        return ResponseEntity.created(URI.create("/enrollments/" + created.getId()))
                .body(ApiResponse.success("Tạo lượt đăng ký thành công.", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Enrollment>> updateEnrollment(
            @PathVariable("id") Long id, @RequestBody Enrollment enrollment) {
        try {
            Enrollment updated = enrollmentService.updateEnrollment(id, enrollment);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật lượt đăng ký thành công.", updated));
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(exception.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Enrollment>> deleteEnrollmentById(@PathVariable("id") Long id) {
        try {
            Enrollment deleted = enrollmentService.deleteEnrollmentById(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa lượt đăng ký thành công.", deleted));
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(exception.getMessage()));
        }
    }
}
