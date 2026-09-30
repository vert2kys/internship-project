package com.example.internship_project.service;

import com.example.internship_project.dto.InterviewResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterviewFallbackResponseFactory {

    public InterviewResponse create(String jobDescription) {
        return new InterviewResponse(
                List.of("Расскажите о вашем ключевом опыте по вакансии: " + jobDescription),
                List.of("Повторите основные теоретические концепции")
        );
    }
}
