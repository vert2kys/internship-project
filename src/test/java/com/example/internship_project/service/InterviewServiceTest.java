package com.example.internship_project.service;

import com.example.internship_project.dto.InterviewRequest;
import com.example.internship_project.dto.InterviewResponse;
import com.example.internship_project.repository.InterviewLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private InterviewLogRepository logRepository;

    @InjectMocks
    private InterviewService interviewService;

    private InterviewRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new InterviewRequest();
        validRequest.setJobDescription("Java Developer with Spring Boot experience");
    }

    @Test
    @DisplayName("Успешная генерация вопросов и сохранение лога")
    void generateQuestions_Success() {
        String mockJsonResponse = """
                {
                  "questions": ["Что такое Dependency Injection?", "Как работает HashMap?"],
                  "recommendations": ["Повторите Core Java", "Изучите Spring IoC"]
                }
                """;

        when(geminiService.askGemini(anyString())).thenReturn(mockJsonResponse);

        InterviewResponse response = interviewService.generateQuestions(validRequest);

        assertNotNull(response);
        assertEquals(2, response.getQuestions().size());
        assertEquals("Что такое Dependency Injection?", response.getQuestions().get(0));

        verify(geminiService, times(1)).askGemini(anyString());
        verify(logRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Возврат fallback-ответа при пустом ответе от Gemini API")
    void generateQuestions_ApiError_ReturnsFallback() {
        when(geminiService.askGemini(anyString())).thenReturn(null);

        InterviewResponse response = interviewService.generateQuestions(validRequest);

        assertNotNull(response);
        assertFalse(response.getQuestions().isEmpty());
        assertTrue(response.getQuestions().get(0).contains("Расскажите о вашем ключевом опыте"));

        verify(logRepository, times(1)).save(any());
    }
}