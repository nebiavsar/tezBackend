package com.example.demo.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.GroupRepository;
import com.example.demo.service.GroupManagementService;
import com.example.demo.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupManagementServiceImpl implements GroupManagementService {

    private final GroupRepository groupRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Group createGroup(Long teacherId, String name) {
        String normalizedName = normalizeName(name);
        if (groupRepository.existsByTeacherIdAndNameIgnoreCase(teacherId, normalizedName)) {
            throw new ConflictException("You already have a group with this name.");
        }

        User teacher = userService.getById(teacherId);
        return groupRepository.save(Group.builder()
                .name(normalizedName)
                .teacher(teacher)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getTeacherGroups(Long teacherId) {
        return groupRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public Group getOwnedGroup(Long teacherId, Long groupId) {
        return groupRepository.findByIdAndTeacherId(groupId, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found for this teacher."));
    }

    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("name is required.");
        }
        return normalized;
    }
}
