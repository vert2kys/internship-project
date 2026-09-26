package com.example.internship_project.service;

import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    public String buildInterviewPrompt(String jobDescription) {
        return """
                Ты — интервьюер для технических специалистов.
                Проанализируй следующее описание вакансии: "%s".
                
                Сгенерируй ответ СТРОГО в формате валидного JSON без разметки markdown (без ```json):
                {
                  "questions": ["вопрос 1", "вопрос 2", "вопрос 3"],
                  "recommendations": ["совет 1", "совет 2"]
                }
                """.formatted(jobDescription);
    }
}