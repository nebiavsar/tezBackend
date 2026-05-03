package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    Optional<Group> findByIdAndTeacherId(Long id, Long teacherId);

    boolean existsByTeacherIdAndNameIgnoreCase(Long teacherId, String name);
}
