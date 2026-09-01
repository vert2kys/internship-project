package com.example.internship_project.service;

import com.example.internship_project.dto.InterviewRequest;
import com.example.internship_project.dto.InterviewResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InterviewResponse generateQuestions(InterviewRequest request) {
        log.info("Запрос на генерацию вопросов для вакансии: {}", request.getJobDescription());

        String prompt = """
                Ты — интервьюер для технических специалистов.
                Проанализируй следующее описание вакансии: "%s".
                
                Сгенерируй ответ СТРОГО в формате валидного JSON без разметки markdown (без ```json):
                {
                  "questions": ["вопрос 1", "вопрос 2", "вопрос 3"],
                  "recommendations": ["совет 1", "совет 2"]
                }
                """.formatted(request.getJobDescription());

        String rawResponse = geminiService.askGemini(prompt);

        try {
            if (rawResponse == null || rawResponse.isBlank()) {
                log.warn("Получен пустой ответ от Gemini API");
                throw new IllegalArgumentException("Получен пустой ответ от Gemini API");
            }

            String cleanJson = rawResponse
                    .replaceAll("(?s)^```(?:json)?\\s*", "")
                    .replaceAll("```$", "")
                    .trim();

            InterviewResponse response = objectMapper.readValue(cleanJson, InterviewResponse.class);
            log.info("Успешно сгенерировано {} вопросов", response.getQuestions().size());
            return response;

        } catch (Exception e) {
            log.error("Ошибка при обработке ответа от Gemini API: {}. Применение fallback-ответа", e.getMessage());
            return new InterviewResponse(
                    List.of("Расскажите о вашем ключевом опыте по вакансии: " + request.getJobDescription()),
                    List.of("Повторите основные теоретические концепции")
            );
        }
    }
}