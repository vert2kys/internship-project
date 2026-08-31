package com.example.internship_project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterviewRequest {
    @NotBlank(message = "jobDescription не должен быть пустым")
    private String jobDescription;
}