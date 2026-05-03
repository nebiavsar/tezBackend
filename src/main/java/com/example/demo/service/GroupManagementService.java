package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Group;

public interface GroupManagementService {

    Group createGroup(Long teacherId, String name);

    List<Group> getTeacherGroups(Long teacherId);

    Group getOwnedGroup(Long teacherId, Long groupId);
}
