package com.example.demo.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Group;
import com.example.demo.entity.Student;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.GroupManagementService;
import com.example.demo.service.StudentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final GroupManagementService groupManagementService;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public Student createStudent(Long teacherId, Long groupId, String studentNumber, String firstName, String lastName) {
        Group group = groupManagementService.getOwnedGroup(teacherId, groupId);
        String normalizedStudentNumber = normalize(studentNumber, "studentNumber");
        if (studentRepository.existsByGroupIdAndStudentNumber(groupId, normalizedStudentNumber)) {
            throw new ConflictException("This student number already exists in the group.");
        }

        return studentRepository.save(Student.builder()
                .studentNumber(normalizedStudentNumber)
                .firstName(normalize(firstName, "firstName"))
                .lastName(normalize(lastName, "lastName"))
                .group(group)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Student getOwnedStudent(Long teacherId, Long groupId, Long studentId) {
        groupManagementService.getOwnedGroup(teacherId, groupId);
        return studentRepository.findByIdAndGroupId(studentId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found in this group."));
    }

    private String normalize(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return normalized;
    }
}
