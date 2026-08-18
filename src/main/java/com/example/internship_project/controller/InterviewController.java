package com.example.internship_project.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InterviewController {

    @PostMapping("/api/v1/interview/generate")
    public Map<String, Object> generate(@RequestBody Map<String, Object> request) {
        return Map.of(
                "questions", new String[]{
                        "Расскажите о своём опыте с Java",
                        "Что такое REST API?"
                },
                "recommendations", "Заглушка — реальный ответ появится после интеграции с AI"
        );
    }
}