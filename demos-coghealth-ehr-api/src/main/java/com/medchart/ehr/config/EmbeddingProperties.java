package com.medchart.ehr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "medchart.embedding")
public class EmbeddingProperties {
    private String apiUrl = "https://api.openai.com/v1/embeddings";
    private String apiKey;
    private String model = "text-embedding-3-small";
    private int dimensions = 1536;
}
