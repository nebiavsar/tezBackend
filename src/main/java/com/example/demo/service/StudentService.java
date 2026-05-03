package com.example.demo.service;

import com.example.demo.entity.Student;

public interface StudentService {

    Student createStudent(Long teacherId, Long groupId, String studentNumber, String firstName, String lastName);

    Student getOwnedStudent(Long teacherId, Long groupId, Long studentId);
}
