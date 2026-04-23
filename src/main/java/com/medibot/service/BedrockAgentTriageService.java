package com.medibot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

@Service
public class BedrockAgentTriageService {

    @Value("${bedrock.region:ap-south-1}")
    private String bedrockRegion;

    @Value("${bedrock.modelId:amazon.nova-lite-v1:0}")
    private String modelId;

    private final ObjectMapper mapper = new ObjectMapper();

    public TriageResult analyze(String patientName, String symptoms) {
        try {
            BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .region(Region.of(bedrockRegion))
                .build();

            String prompt = "You are a medical triage assistant. Analyze the symptoms and respond ONLY with this JSON:\n" +
                "{\n" +
                "  \"triageLevel\": \"EMERGENCY or URGENT or NORMAL\",\n" +
                "  \"summary\": \"one sentence assessment\",\n" +
                "  \"department\": \"department name\",\n" +
                "  \"waitTime\": \"wait time\"\n" +
                "}\n\n" +
                "Rules:\n" +
                "EMERGENCY: chest pain, stroke, breathing difficulty → waitTime: Immediate\n" +
                "URGENT: high fever, severe pain, fractures → waitTime: 15-30 minutes\n" +
                "NORMAL: mild cold, minor aches, checkup → waitTime: 1-2 hours\n\n" +
                "Departments: Emergency, Cardiology, Neurology, Orthopedics, Pulmonology, General Medicine, Pediatrics\n\n" +
                "Patient symptoms: " + symptoms + "\n\n" +
                "Respond ONLY with JSON. No extra text.";

            // Amazon Nova request format
            String requestBody = mapper.writeValueAsString(
                java.util.Map.of(
                    "messages", java.util.List.of(
                        java.util.Map.of(
                            "role", "user",
                            "content", java.util.List.of(
                                java.util.Map.of("text", prompt)
                            )
                        )
                    ),
                    "inferenceConfig", java.util.Map.of(
                        "maxTokens", 400,
                        "temperature", 0.3
                    )
                )
            );

            InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId(modelId)
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(requestBody))
                .build();

            InvokeModelResponse response = client.invokeModel(request);
            String responseBody = response.body().asUtf8String();

            // Amazon Nova response format: output.message.content[0].text
            JsonNode root    = mapper.readTree(responseBody);
            String content   = root.path("output")
                                   .path("message")
                                   .path("content")
                                   .get(0)
                                   .path("text")
                                   .asText();

            int start = content.indexOf("{");
            int end   = content.lastIndexOf("}") + 1;
            if (start < 0 || end <= start) throw new RuntimeException("No JSON in response");

            JsonNode result = mapper.readTree(content.substring(start, end));

            return new TriageResult(
                result.path("triageLevel").asText("NORMAL"),
                result.path("summary").asText("Analysis completed."),
                result.path("department").asText("General Medicine"),
                result.path("waitTime").asText("1-2 hours")
            );

        } catch (Exception e) {
            System.err.println("Nova error: " + e.getMessage());
            return new TriageResult("NORMAL",
                "AI service temporarily unavailable. Default triage applied.",
                "General Medicine", "1-2 hours");
        }
    }

    public static class TriageResult {
        public final String triageLevel;
        public final String summary;
        public final String department;
        public final String waitTime;

        public TriageResult(String triageLevel, String summary,
                            String department, String waitTime) {
            this.triageLevel = triageLevel;
            this.summary     = summary;
            this.department  = department;
            this.waitTime    = waitTime;
        }
    }
}
