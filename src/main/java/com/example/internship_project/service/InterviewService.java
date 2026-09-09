package com.example.internship_project.service;

import com.example.internship_project.dto.InterviewRequest;
import com.example.internship_project.dto.InterviewResponse;
import com.example.internship_project.entity.InterviewLog;
import com.example.internship_project.repository.InterviewLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final GeminiService geminiService;

    @Autowired(required = false)
    private final InterviewLogRepository interviewLogRepository;

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

            // Сохраняем лог в базу данных
            saveInterviewLog(request.getJobDescription(), objectMapper.writeValueAsString(response));

            return response;

        } catch (Exception e) {
            log.error("Ошибка при обработке ответа от Gemini API: {}. Применение fallback-ответа", e.getMessage());
            InterviewResponse fallbackResponse = new InterviewResponse(
                    List.of("Расскажите о вашем ключевом опыте по вакансии: " + request.getJobDescription()),
                    List.of("Повторите основные теоретические концепции")
            );

            // Сохраняем лог с fallback-ответом
            try {
                saveInterviewLog(request.getJobDescription(), objectMapper.writeValueAsString(fallbackResponse));
            } catch (Exception logException) {
                log.error("Ошибка при сохранении лога: {}", logException.getMessage());
            }

            return fallbackResponse;
        }
    }

    private void saveInterviewLog(String request, String response) {
        try {
            // Если repository недоступен (разработка без БД), пропускаем сохранение
            if (interviewLogRepository == null) {
                log.debug("Логирование в БД отключено (режим разработки)");
                return;
            }

            InterviewLog interviewLog = new InterviewLog();
            interviewLog.setRequest(request);
            interviewLog.setResponse(response);
            interviewLogRepository.save(interviewLog);
            log.info("Лог интервью успешно сохранен");
        } catch (Exception e) {
            log.error("Ошибка при сохранении лога интервью: {}", e.getMessage());
        }
    }
}