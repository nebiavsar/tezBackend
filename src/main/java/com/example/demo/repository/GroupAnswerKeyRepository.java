package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.GroupAnswerKey;

public interface GroupAnswerKeyRepository extends JpaRepository<GroupAnswerKey, Long> {

    Optional<GroupAnswerKey> findByGroupIdAndActiveTrue(Long groupId);

    Optional<GroupAnswerKey> findTopByGroupIdOrderByVersionNumberDesc(Long groupId);
}
