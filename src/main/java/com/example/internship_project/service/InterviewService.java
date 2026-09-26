package com.example.internship_project.service;

import com.example.internship_project.dto.InterviewRequest;
import com.example.internship_project.dto.InterviewResponse;
import com.example.internship_project.entity.InterviewLog;
import com.example.internship_project.repository.InterviewLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final GeminiService geminiService;
    private final PromptBuilderService promptBuilderService;
    private final ObjectMapper objectMapper;
    private final Optional<InterviewLogRepository> interviewLogRepository;

    public InterviewResponse generateQuestions(InterviewRequest request) {
        log.info("Запрос на генерацию вопросов для вакансии: {}", request.getJobDescription());

        String prompt = promptBuilderService.buildInterviewPrompt(request.getJobDescription());

        try {
            String rawResponse = geminiService.askGemini(prompt);

            if (rawResponse == null || rawResponse.isBlank()) {
                log.warn("Получен пустой ответ от Gemini API");
                throw new IllegalArgumentException("Получен пустой ответ от Gemini API");
            }

            String cleanJson = cleanJsonResponse(rawResponse);
            InterviewResponse response = objectMapper.readValue(cleanJson, InterviewResponse.class);
            log.info("Успешно сгенерировано {} вопросов", response.getQuestions().size());

            saveInterviewLog(request.getJobDescription(), response);
            return response;

        } catch (Exception e) {
            log.error("Ошибка при обработке ответа от Gemini API: {}. Применение fallback-ответа", e.getMessage());
            InterviewResponse fallbackResponse = createFallbackResponse(request.getJobDescription());
            saveInterviewLog(request.getJobDescription(), fallbackResponse);
            return fallbackResponse;
        }
    }

    private String cleanJsonResponse(String rawResponse) {
        return rawResponse
                .replaceAll("(?s)^```(?:json)?\\s*", "")
                .replaceAll("```$", "")
                .trim();
    }

    private InterviewResponse createFallbackResponse(String jobDescription) {
        return new InterviewResponse(
                List.of("Расскажите о вашем ключевом опыте по вакансии: " + jobDescription),
                List.of("Повторите основные теоретические концепции")
        );
    }

    private void saveInterviewLog(String request, InterviewResponse response) {
        interviewLogRepository.ifPresentOrElse(repository -> {
            try {
                InterviewLog interviewLog = new InterviewLog();
                interviewLog.setRequest(request);
                interviewLog.setResponse(objectMapper.writeValueAsString(response));
                repository.save(interviewLog);
                log.info("Лог интервью успешно сохранен");
            } catch (Exception e) {
                log.error("Ошибка при сохранении лога интервью: {}", e.getMessage());
            }
        }, () -> log.debug("Логирование в БД отключено (режим разработки)"));
    }
}