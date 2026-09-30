package com.example.internship_project.service;

import com.example.internship_project.dto.InterviewResponse;
import com.example.internship_project.entity.InterviewLog;
import com.example.internship_project.repository.InterviewLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewLogService {

    private final InterviewLogRepository interviewLogRepository;
    private final InterviewLogBuilder interviewLogBuilder;

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
}
