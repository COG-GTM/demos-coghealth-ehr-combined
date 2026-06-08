package com.medchart.ehr.service;

import com.medchart.ehr.config.EmbeddingProperties;
import com.medchart.ehr.domain.patient.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmbeddingService {

    private final WebClient webClient;
    private final EmbeddingProperties properties;

    public EmbeddingService(EmbeddingProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getApiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Get embedding vector for a text string by calling the configured embedding API.
     */
    @SuppressWarnings("unchecked")
    public float[] getEmbedding(String text) {
        Map<String, Object> requestBody = Map.of(
                "input", text,
                "model", properties.getModel()
        );

        Map<String, Object> response = webClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        List<Double> embeddingList = (List<Double>) data.get(0).get("embedding");

        float[] embedding = new float[embeddingList.size()];
        for (int i = 0; i < embeddingList.size(); i++) {
            embedding[i] = embeddingList.get(i).floatValue();
        }
        return embedding;
    }

    /**
     * Convert a Patient entity into a text string suitable for embedding.
     */
    public String toSearchableText(Patient patient) {
        StringBuilder sb = new StringBuilder();
        sb.append(patient.getFirstName()).append(" ");
        if (patient.getMiddleName() != null) {
            sb.append(patient.getMiddleName()).append(" ");
        }
        sb.append(patient.getLastName()).append(" ");
        sb.append(patient.getMrn());
        if (patient.getDateOfBirth() != null) {
            sb.append(" ").append(patient.getDateOfBirth().toString());
        }
        if (patient.getEmail() != null) {
            sb.append(" ").append(patient.getEmail());
        }
        return sb.toString().trim();
    }

    /**
     * Convert a float array to the PostgreSQL vector literal format: "[0.1,0.2,...]"
     */
    public String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
