package com.example.internship_project.controller;

import com.example.internship_project.dto.InterviewRequest;
import com.example.internship_project.dto.InterviewResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interview")
public class InterviewController {

    @PostMapping("/generate")
    public InterviewResponse generate(@Valid @RequestBody InterviewRequest request) {
        return new InterviewResponse(
                List.of(
                        "Расскажите о своём опыте с Java",
                        "Что такое REST API?"
                ),
                "Заглушка, нормальный ответ появится после интеграции с AI"
        );
    }
}