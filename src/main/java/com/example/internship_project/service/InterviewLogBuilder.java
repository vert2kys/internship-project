package com.example.internship_project.service;

import com.example.internship_project.dto.InterviewResponse;
import com.example.internship_project.entity.InterviewLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewLogBuilder {

    private final ObjectMapper objectMapper;
    private String request;
    private InterviewResponse response;

    public InterviewLogBuilder request(String request) {
        this.request = request;
        return this;
    }

    public InterviewLogBuilder response(InterviewResponse response) {
        this.response = response;
        return this;
    }

    public InterviewLog build() {
        if (request == null || response == null) {
            throw new IllegalStateException("Для построения InterviewLog требуется request и response");
        }

        try {
            InterviewLog interviewLog = new InterviewLog();
            interviewLog.setRequest(request);
            interviewLog.setResponse(objectMapper.writeValueAsString(response));
            return interviewLog;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать InterviewResponse в JSON", e);
        }
    }
}
