package com.example.baitapsession03.service;

import com.example.baitapsession03.exception.ResourceNotFoundException;
import com.example.baitapsession03.model.Instructor;
import com.example.baitapsession03.repository.InstructorRepository;
import com.example.baitapsession03.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public InstructorService(InstructorRepository instructorRepository, CourseRepository courseRepository) {
        this.instructorRepository = instructorRepository;
        this.courseRepository = courseRepository;
    }

    public List<Instructor> getAllInstructors() {
        return instructorRepository.findAll();
    }

    public Instructor getInstructorById(Long id) {
        validateId(id);
        return instructorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên có ID " + id + "."));
    }

    public Instructor createInstructor(Instructor instructor) {
        validate(instructor);
        return instructorRepository.create(instructor);
    }

    public Instructor updateInstructor(Long id, Instructor instructor) {
        getInstructorById(id); // Service quyet dinh nem loi neu ban ghi khong ton tai.
        validate(instructor);
        if (instructor.getId() != null && !instructor.getId().equals(id)) {
            throw new IllegalArgumentException("ID trong body phải trùng với ID trên URL.");
        }
        return instructorRepository.update(id, instructor);
    }

    public Instructor deleteInstructorById(Long id) {
        getInstructorById(id); // Service quyet dinh nem loi neu ban ghi khong ton tai.
        boolean hasCourses = courseRepository.findAll().stream()
                .anyMatch(course -> course.getInstructorId().equals(id));
        if (hasCourses) {
            throw new IllegalArgumentException("Giảng viên còn khóa học; hãy chuyển hoặc xóa khóa học trước.");
        }
        return instructorRepository.deleteById(id);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID phải lớn hơn 0.");
        }
    }

    private void validate(Instructor instructor) {
        if (instructor == null) {
            throw new IllegalArgumentException("Body không được để trống.");
        }
        if (instructor.getName() == null || instructor.getName().isBlank()) {
            throw new IllegalArgumentException("Tên giảng viên không được để trống.");
        }
        if (instructor.getEmail() == null
                || !instructor.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
    }
}
