package com.example.internship_project.repository;

import com.example.internship_project.entity.InterviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewLogRepository extends JpaRepository<InterviewLog, Long> {
}

