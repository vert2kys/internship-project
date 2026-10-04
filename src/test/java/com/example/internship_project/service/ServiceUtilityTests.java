package com.example.internship_project.service;

import com.example.internship_project.dto.InterviewResponse;
import com.example.internship_project.entity.InterviewLog;
import com.example.internship_project.repository.InterviewLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceUtilityTests {

    @Mock
    private InterviewLogRepository interviewLogRepository;

    @Test
    void promptBuilderService_buildInterviewPrompt_containsJobDescriptionAndJsonSchema() {
        PromptBuilderService service = new PromptBuilderService();

        String prompt = service.buildInterviewPrompt("Java Backend Developer");

        assertTrue(prompt.contains("Java Backend Developer"));
        assertTrue(prompt.contains("\"questions\""));
        assertTrue(prompt.contains("\"recommendations\""));
    }

    @Test
    void interviewResponseParser_parse_validJson_returnsInterviewResponse() throws Exception {
        InterviewResponseParser parser = new InterviewResponseParser(new ObjectMapper());
        String raw = """
                {
                  "questions": ["Q1", "Q2"],
                  "recommendations": ["R1"]
                }
                """;

        InterviewResponse response = parser.parse(raw);

        assertEquals(List.of("Q1", "Q2"), response.getQuestions());
        assertEquals(List.of("R1"), response.getRecommendations());
    }

    @Test
    void interviewResponseParser_parse_invalidJson_throwsIllegalArgumentException() {
        InterviewResponseParser parser = new InterviewResponseParser(new ObjectMapper());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> parser.parse("{not-json}"));

        assertTrue(ex.getMessage().contains("невалидный JSON"));
    }

    @Test
    void interviewResponseParser_cleanJsonResponse_removesMarkdownFence() {
        InterviewResponseParser parser = new InterviewResponseParser(new ObjectMapper());

        String cleaned = parser.cleanJsonResponse("```json\n{\"questions\":[\"Q\"]}\n```");

        assertFalse(cleaned.contains("```"));
        assertTrue(cleaned.contains("\"questions\""));
    }

    @Test
    void interviewFallbackResponseFactory_create_returnsDefaultQuestionsAndRecommendations() {
        InterviewFallbackResponseFactory factory = new InterviewFallbackResponseFactory();

        InterviewResponse response = factory.create("Java Developer");

        assertEquals(1, response.getQuestions().size());
        assertTrue(response.getQuestions().get(0).contains("Java Developer"));
        assertEquals(1, response.getRecommendations().size());
    }

    @Test
    void interviewLogBuilder_build_withValidData_returnsInterviewLog() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InterviewLogBuilder builder = new InterviewLogBuilder(mapper);
        InterviewResponse response = new InterviewResponse(List.of("Q1"), List.of("R1"));

        InterviewLog log = builder.request("Java Developer").response(response).build();

        assertEquals("Java Developer", log.getRequest());
        assertNotNull(log.getResponse());
        assertTrue(log.getResponse().contains("Q1"));
    }

    @Test
    void interviewLogBuilder_build_withoutRequestOrResponse_throwsIllegalStateException() {
        InterviewLogBuilder builder = new InterviewLogBuilder(new ObjectMapper());

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void interviewLogService_findByRequest_whenEntityExists_returnsInterviewResponse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InterviewLog log = new InterviewLog();
        log.setId(1L);
        log.setRequest("Java Developer");
        log.setResponse(mapper.writeValueAsString(new InterviewResponse(List.of("Q1"), List.of("R1"))));
        log.setCreatedAt(LocalDateTime.now());

        when(interviewLogRepository.findTopByRequestIgnoreCaseOrderByCreatedAtDesc("Java Developer"))
                .thenReturn(Optional.of(log));

        InterviewLogService service = new InterviewLogService(interviewLogRepository, new InterviewLogBuilder(mapper), mapper);

        Optional<InterviewResponse> result = service.findByRequest("Java Developer");

        assertTrue(result.isPresent());
        assertEquals(List.of("Q1"), result.get().getQuestions());
    }

    @Test
    void interviewLogService_save_persistsEntity() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InterviewLogBuilder builder = new InterviewLogBuilder(mapper);
        InterviewLogService service = new InterviewLogService(interviewLogRepository, builder, mapper);
        InterviewResponse response = new InterviewResponse(List.of("Q1"), List.of("R1"));

        service.save("Java Developer", response);

        ArgumentCaptor<InterviewLog> captor = ArgumentCaptor.forClass(InterviewLog.class);
        verify(interviewLogRepository, times(1)).save(captor.capture());
        assertEquals("Java Developer", captor.getValue().getRequest());
        assertTrue(captor.getValue().getResponse().contains("Q1"));
    }
}
