package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByGroupIdOrderByLastNameAscFirstNameAsc(Long groupId);

    Optional<Student> findByIdAndGroupId(Long id, Long groupId);

    Optional<Student> findByGroupIdAndStudentNumber(Long groupId, String studentNumber);

    boolean existsByGroupIdAndStudentNumber(Long groupId, String studentNumber);
}
