package com.example.baitapsession03.service;

import com.example.baitapsession03.exception.ResourceNotFoundException;
import com.example.baitapsession03.model.Course;
import com.example.baitapsession03.repository.CourseRepository;
import com.example.baitapsession03.repository.InstructorRepository;
import com.example.baitapsession03.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository, InstructorRepository instructorRepository, EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        validateId(id);
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học có ID " + id + "."));
    }

    public Course createCourse(Course course) {
        validate(course);
        return courseRepository.create(course);
    }

    public Course updateCourse(Long id, Course course) {
        getCourseById(id); // Service quyet dinh nem loi neu ban ghi khong ton tai.
        validate(course);
        if (course.getId() != null && !course.getId().equals(id)) {
            throw new IllegalArgumentException("ID trong body phải trùng với ID trên URL.");
        }
        return courseRepository.update(id, course);
    }

    public Course deleteCourseById(Long id) {
        getCourseById(id); // Service quyet dinh nem loi neu ban ghi khong ton tai.
        boolean hasEnrollments = enrollmentRepository.findAll().stream()
                .anyMatch(enrollment -> enrollment.getCourseId().equals(id));
        if (hasEnrollments) {
            throw new IllegalArgumentException("Khóa học còn lượt đăng ký; hãy chuyển hoặc xóa lượt đăng ký trước.");
        }
        return courseRepository.deleteById(id);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID phải lớn hơn 0.");
        }
    }

    private void validate(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Body không được để trống.");
        }
        if (course.getTitle() == null || course.getTitle().isBlank()) {
            throw new IllegalArgumentException("Tiêu đề khóa học không được để trống.");
        }
        if (course.getStatus() == null || course.getStatus().isBlank()) {
            throw new IllegalArgumentException("Trạng thái khóa học không được để trống.");
        }
        if (course.getInstructorId() == null) {
            throw new IllegalArgumentException("instructorId không được để trống.");
        }
        instructorRepository.findById(course.getInstructorId())
                .orElseThrow(() -> new IllegalArgumentException("Giảng viên được tham chiếu không tồn tại."));
    }
}
