package com.example.internship_project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiServiceTest {

    private GeminiService geminiService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient.Builder builder = RestClient.builder().requestFactory(requestFactory);
        mockServer = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();

        geminiService = new GeminiService();
        ReflectionTestUtils.setField(geminiService, "restClient", restClient);
        ReflectionTestUtils.setField(geminiService, "apiUrl", "https://example.test/generateContent");
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-key");
    }

    @Test
    void askGeminiReturnsFirstCandidateText() {
        mockServer.expect(requestTo("https://example.test/generateContent?key=test-key"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
                        {
                          "contents": [
                            {
                              "parts": [
                                {"text": "Explain dependency injection"}
                              ]
                            }
                          ]
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "candidates": [
                            {
                              "content": {
                                "parts": [
                                  {"text": "Dependency injection supplies dependencies from outside a class."}
                                ]
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        String result = geminiService.askGemini("Explain dependency injection");

        assertThat(result).isEqualTo("Dependency injection supplies dependencies from outside a class.");
        mockServer.verify();
    }

    @Test
    void askGeminiReturnsEmptyStringWhenResponseHasNoCandidates() {
        mockServer.expect(requestTo("https://example.test/generateContent?key=test-key"))
                .andRespond(withSuccess("{\"candidates\": []}", MediaType.APPLICATION_JSON));

        String result = geminiService.askGemini("No answer");

        assertThat(result).isEmpty();
        mockServer.verify();
    }
}
