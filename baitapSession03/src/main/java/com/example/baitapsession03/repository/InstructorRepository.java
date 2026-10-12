package com.example.baitapsession03.repository;

import com.example.baitapsession03.exception.ResourceNotFoundException;
import com.example.baitapsession03.model.Instructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InstructorRepository {

    private final List<Instructor> instructors = new ArrayList<>(List.of(
            new Instructor(1L, "Nguyen Van An", "an@example.com"),
            new Instructor(2L, "Tran Thi Binh", "binh@example.com")
    ));

    // Tra ban sao danh sach de GET khong bi anh huong khi request khac them/xoa.
    public synchronized List<Instructor> findAll() {
        return new ArrayList<>(instructors);
    }

    public synchronized Optional<Instructor> findById(long id) {
        return instructors.stream()
                .filter(instructor -> instructor.getId().longValue() == id)
                .findFirst();
    }

    public synchronized Instructor create(Instructor instructor) {
        if (instructor.getId() == null) {
            long maxId = instructors.stream().mapToLong(Instructor::getId).max().orElse(0L);
            if (maxId == Long.MAX_VALUE) {
                throw new IllegalArgumentException("Không thể cấp thêm ID tự động.");
            }
            instructor.setId(maxId + 1);
        } else if (instructor.getId() <= 0 || findById(instructor.getId()).isPresent()) {
            throw new IllegalArgumentException("ID phải lớn hơn 0 và chưa tồn tại.");
        }

        instructors.add(instructor);
        return instructor;
    }

    public synchronized Instructor update(long id, Instructor instructor) {
        Instructor existing = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên có ID " + id + "."));

        // Chi cap nhat khi tim thay, giu ID tren URL.
        instructor.setId(id);
        instructors.set(instructors.indexOf(existing), instructor);
        return instructor;
    }

    public synchronized Instructor deleteById(long id) {
        Instructor existing = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên có ID " + id + "."));
        instructors.remove(existing);
        return existing;
    }
}
