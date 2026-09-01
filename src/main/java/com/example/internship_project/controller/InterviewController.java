package com.example.internship_project.controller;

import com.example.internship_project.dto.InterviewRequest;
import com.example.internship_project.dto.InterviewResponse;
import com.example.internship_project.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/generate")
    public InterviewResponse generateQuestions(@Valid @RequestBody InterviewRequest request) {
        return interviewService.generateQuestions(request);
    }
}