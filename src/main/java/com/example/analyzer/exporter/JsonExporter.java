package com.example.analyzer.exporter;

import com.example.analyzer.model.AnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Exports analysis results to JSON format
 */
public class JsonExporter {
    private static final Logger logger = LoggerFactory.getLogger(JsonExporter.class);

    private final ObjectMapper objectMapper;

    public JsonExporter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Export analysis result to JSON file
     */
    public void export(AnalysisResult result, String outputPath) throws IOException {
        Path path = Paths.get(outputPath);

        // Create parent directories if they don't exist
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        objectMapper.writeValue(path.toFile(), result);
        logger.info("Exported analysis result to: {}", path.toAbsolutePath());
    }

    /**
     * Export analysis result to JSON string
     */
    public String exportToString(AnalysisResult result) throws IOException {
        return objectMapper.writeValueAsString(result);
    }

    /**
     * Export with custom formatting options
     */
    public void export(AnalysisResult result, String outputPath, boolean prettyPrint) throws IOException {
        ObjectMapper mapper = this.objectMapper.copy();
        if (prettyPrint) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        } else {
            mapper.disable(SerializationFeature.INDENT_OUTPUT);
        }

        Path path = Paths.get(outputPath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        mapper.writeValue(path.toFile(), result);
        logger.info("Exported analysis result to: {}", path.toAbsolutePath());
    }

    /**
     * Load analysis result from JSON file
     */
    public AnalysisResult load(String inputPath) throws IOException {
        Path path = Paths.get(inputPath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + inputPath);
        }

        AnalysisResult result = objectMapper.readValue(path.toFile(), AnalysisResult.class);
        result.buildIndexes();  // Rebuild internal indexes
        logger.info("Loaded analysis result from: {}", path.toAbsolutePath());
        return result;
    }
}
