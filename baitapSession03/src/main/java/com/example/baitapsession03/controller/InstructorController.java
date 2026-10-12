package com.example.baitapsession03.controller;

import com.example.baitapsession03.dto.ApiResponse;
import com.example.baitapsession03.exception.ResourceNotFoundException;
import com.example.baitapsession03.model.Instructor;
import com.example.baitapsession03.service.InstructorService;
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
@RequestMapping("/instructors")
public class InstructorController {

    private final InstructorService instructorService;

    @Autowired
    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Instructor>>> getAllInstructors() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách giảng viên thành công.", instructorService.getAllInstructors()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Instructor>> getInstructorById(@PathVariable("id") Long id) {
        try {
            Instructor instructor = instructorService.getInstructorById(id);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin giảng viên thành công.", instructor));
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(exception.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Instructor>> createInstructor(@RequestBody Instructor instructor) {
        Instructor created = instructorService.createInstructor(instructor);
        return ResponseEntity.created(URI.create("/instructors/" + created.getId()))
                .body(ApiResponse.success("Tạo giảng viên thành công.", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Instructor>> updateInstructor(
            @PathVariable("id") Long id, @RequestBody Instructor instructor) {
        try {
            Instructor updated = instructorService.updateInstructor(id, instructor);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật giảng viên thành công.", updated));
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(exception.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Instructor>> deleteInstructorById(@PathVariable("id") Long id) {
        try {
            Instructor deleted = instructorService.deleteInstructorById(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa giảng viên thành công.", deleted));
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(exception.getMessage()));
        }
    }
}
