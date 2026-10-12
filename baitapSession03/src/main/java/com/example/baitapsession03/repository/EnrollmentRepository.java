package com.example.baitapsession03.repository;

import com.example.baitapsession03.exception.ResourceNotFoundException;
import com.example.baitapsession03.model.Enrollment;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EnrollmentRepository {

    private final List<Enrollment> enrollments = new ArrayList<>(List.of(
            new Enrollment(1L, "Le Minh Chau", 1L),
            new Enrollment(2L, "Pham Quang Duy", 1L),
            new Enrollment(3L, "Hoang Ngoc Ha", 2L),
            new Enrollment(4L, "Do Tuan Kiet", 3L)
    ));

    // Tra ban sao danh sach de GET khong bi anh huong khi request khac them/xoa.
    public synchronized List<Enrollment> findAll() {
        return new ArrayList<>(enrollments);
    }

    public synchronized Optional<Enrollment> findById(long id) {
        return enrollments.stream()
                .filter(enrollment -> enrollment.getId().longValue() == id)
                .findFirst();
    }

    public synchronized Enrollment create(Enrollment enrollment) {
        if (enrollment.getId() == null) {
            long maxId = enrollments.stream().mapToLong(Enrollment::getId).max().orElse(0L);
            if (maxId == Long.MAX_VALUE) {
                throw new IllegalArgumentException("Không thể cấp thêm ID tự động.");
            }
            enrollment.setId(maxId + 1);
        } else if (enrollment.getId() <= 0 || findById(enrollment.getId()).isPresent()) {
            throw new IllegalArgumentException("ID phải lớn hơn 0 và chưa tồn tại.");
        }

        enrollments.add(enrollment);
        return enrollment;
    }

    public synchronized Enrollment update(long id, Enrollment enrollment) {
        Enrollment existing = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lượt đăng ký có ID " + id + "."));

        // Chi cap nhat khi tim thay, giu ID tren URL.
        enrollment.setId(id);
        enrollments.set(enrollments.indexOf(existing), enrollment);
        return enrollment;
    }

    public synchronized Enrollment deleteById(long id) {
        Enrollment existing = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lượt đăng ký có ID " + id + "."));
        enrollments.remove(existing);
        return existing;
    }
}
