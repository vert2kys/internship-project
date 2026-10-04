package com.example.internship_project.service;

import com.example.internship_project.dto.InterviewResponse;
import com.example.internship_project.entity.InterviewLog;
import com.example.internship_project.repository.InterviewLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewLogService {

    private final InterviewLogRepository interviewLogRepository;
    private final InterviewLogBuilder interviewLogBuilder;
    private final ObjectMapper objectMapper;

    public Optional<InterviewResponse> findByRequest(String request) {
        String normalizedRequest = normalizeRequest(request);
        if (normalizedRequest.isBlank()) {
            return Optional.empty();
        }

        return interviewLogRepository
                .findTopByRequestIgnoreCaseOrderByCreatedAtDesc(normalizedRequest)
                .map(this::toInterviewResponse);
    }

    public void save(String request, InterviewResponse response) {
        try {
            InterviewLog interviewLog = interviewLogBuilder
                    .request(request)
                    .response(response)
                    .build();

            interviewLogRepository.save(interviewLog);
            log.info("Лог интервью успешно сохранен");
        } catch (Exception e) {
            log.error("Ошибка при сохранении лога интервью: {}", e.getMessage());
        }
    }

    private InterviewResponse toInterviewResponse(InterviewLog interviewLog) {
        try {
            return objectMapper.readValue(interviewLog.getResponse(), InterviewResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось десериализовать InterviewResponse из сохраненного лога", e);
        }
    }

    private String normalizeRequest(String request) {
        return request == null ? "" : request.trim();
    }
}
