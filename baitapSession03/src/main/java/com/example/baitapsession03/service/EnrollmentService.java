package com.example.baitapsession03.service;

import com.example.baitapsession03.exception.ResourceNotFoundException;
import com.example.baitapsession03.model.Enrollment;
import com.example.baitapsession03.repository.EnrollmentRepository;
import com.example.baitapsession03.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository, CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    public Enrollment getEnrollmentById(Long id) {
        validateId(id);
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lượt đăng ký có ID " + id + "."));
    }

    public Enrollment createEnrollment(Enrollment enrollment) {
        validate(enrollment);
        return enrollmentRepository.create(enrollment);
    }

    public Enrollment updateEnrollment(Long id, Enrollment enrollment) {
        getEnrollmentById(id); // Service quyet dinh nem loi neu ban ghi khong ton tai.
        validate(enrollment);
        if (enrollment.getId() != null && !enrollment.getId().equals(id)) {
            throw new IllegalArgumentException("ID trong body phải trùng với ID trên URL.");
        }
        return enrollmentRepository.update(id, enrollment);
    }

    public Enrollment deleteEnrollmentById(Long id) {
        getEnrollmentById(id); // Service quyet dinh nem loi neu ban ghi khong ton tai.
        return enrollmentRepository.deleteById(id);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID phải lớn hơn 0.");
        }
    }

    private void validate(Enrollment enrollment) {
        if (enrollment == null) {
            throw new IllegalArgumentException("Body không được để trống.");
        }
        if (enrollment.getStudentName() == null || enrollment.getStudentName().isBlank()) {
            throw new IllegalArgumentException("Tên học viên không được để trống.");
        }
        if (enrollment.getCourseId() == null) {
            throw new IllegalArgumentException("courseId không được để trống.");
        }
        courseRepository.findById(enrollment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Khóa học được tham chiếu không tồn tại."));
    }
}
