package com.tongji.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "spring.elasticsearch")
public class EsProperties {
    private List<String> uris;    // Supports multiple ES nodes: spring.elasticsearch.uris

    // Since xpack.security.enabled: false in config file, no username/password needed
    private String username;      // spring.elasticsearch.username (optional)
    private String password;      // spring.elasticsearch.password (optional)

    // RAG index name from Spring AI configuration
    @Value("${spring.ai.vectorstore.elasticsearch.index-name:}")
    private String index;         // e.g. intellectlight-ai-index

    // Legacy support: returns the first URI as host
    public String getHost() {
        return (uris == null || uris.isEmpty()) ? null : uris.getFirst();
    }
}
