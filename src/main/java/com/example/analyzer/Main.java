package com.example.analyzer;

import com.example.analyzer.core.CallChainEngine;
import com.example.analyzer.exporter.JsonExporter;
import com.example.analyzer.model.AnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main entry point for Call Chain Analyzer
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                printUsage();
                System.exit(1);
            }

            AnalyzerConfig config = parseArguments(args);

            if (config.servicePaths.isEmpty()) {
                System.err.println("Error: No service paths provided");
                printUsage();
                System.exit(1);
            }

            logger.info("=== Call Chain Analyzer ===");
            logger.info("Analyzing services: {}", config.servicePaths);
            logger.info("Output file: {}", config.outputPath);

            // Run analysis
            CallChainEngine engine = new CallChainEngine();
            AnalysisResult result = engine.analyze(config.servicePaths);

            // Export results
            JsonExporter exporter = new JsonExporter();
            exporter.export(result, config.outputPath, config.prettyPrint);

            logger.info("=== Analysis Complete ===");
            logger.info("Services analyzed: {}", result.getMetadata().getTotalServices());
            logger.info("Classes found: {}", result.getMetadata().getTotalClasses());
            logger.info("Methods found: {}", result.getMetadata().getTotalMethods());
            logger.info("Method calls: {}", result.getMethodCalls().size());
            logger.info("Call chains: {}", result.getCallChains().size());
            logger.info("Results saved to: {}", config.outputPath);

            System.exit(0);

        } catch (Exception e) {
            logger.error("Analysis failed: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static AnalyzerConfig parseArguments(String[] args) {
        AnalyzerConfig config = new AnalyzerConfig();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--service":
                case "-s":
                    if (i + 1 < args.length) {
                        config.servicePaths.add(args[++i]);
                    }
                    break;
                case "--services":
                    if (i + 1 < args.length) {
                        String[] paths = args[++i].split(",");
                        config.servicePaths.addAll(Arrays.asList(paths));
                    }
                    break;
                case "--output":
                case "-o":
                    if (i + 1 < args.length) {
                        config.outputPath = args[++i];
                    }
                    break;
                case "--pretty":
                    config.prettyPrint = true;
                    break;
                case "--help":
                case "-h":
                    printUsage();
                    System.exit(0);
                    break;
                default:
                    // Treat as service path if no flag
                    if (!arg.startsWith("-")) {
                        config.servicePaths.add(arg);
                    }
            }
        }

        return config;
    }

    private static void printUsage() {
        System.out.println("Call Chain Analyzer - Static analysis tool for Spring Boot + Dubbo microservices");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar call-chain-analyzer.jar [OPTIONS] <service-path>...");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -s, --service <path>       Path to a single service directory");
        System.out.println("  --services <paths>         Comma-separated list of service paths");
        System.out.println("  -o, --output <file>        Output JSON file path (default: analysis-result.json)");
        System.out.println("  --pretty                   Pretty-print JSON output");
        System.out.println("  -h, --help                 Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  # Analyze single service");
        System.out.println("  java -jar analyzer.jar --service ./user-service --output result.json");
        System.out.println();
        System.out.println("  # Analyze multiple services");
        System.out.println("  java -jar analyzer.jar --services ./user-service,./order-service --output chains.json");
        System.out.println();
        System.out.println("  # Analyze with pretty-printed output");
        System.out.println("  java -jar analyzer.jar --service ./user-service --output result.json --pretty");
    }

    private static class AnalyzerConfig {
        List<String> servicePaths = new ArrayList<>();
        String outputPath = "analysis-result.json";
        boolean prettyPrint = true;  // Default to pretty print
    }
}
