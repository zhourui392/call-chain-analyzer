package com.example.analyzer.core;

import com.example.analyzer.model.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scans project directories to discover services and their source code
 */
public class ProjectScanner {
    private static final Logger logger = LoggerFactory.getLogger(ProjectScanner.class);

    /**
     * Scan a single service directory
     */
    public ServiceInfo scanService(String servicePath) {
        Path path = Paths.get(servicePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Service path does not exist: " + servicePath);
        }

        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setId(UUID.randomUUID().toString());
        serviceInfo.setRootPath(path.toAbsolutePath().toString());

        // Try to parse pom.xml
        File pomFile = new File(path.toFile(), "pom.xml");
        if (pomFile.exists()) {
            parsePomXml(pomFile, serviceInfo);
        } else {
            // Fallback: use directory name
            serviceInfo.setName(path.getFileName().toString());
            serviceInfo.setArtifactId(path.getFileName().toString());
        }

        // Detect base package from source structure
        String basePackage = detectBasePackage(path);
        serviceInfo.setBasePackage(basePackage);

        logger.info("Scanned service: {} at {}", serviceInfo.getName(), servicePath);
        return serviceInfo;
    }

    /**
     * Scan multiple service directories
     */
    public List<ServiceInfo> scanServices(List<String> servicePaths) {
        List<ServiceInfo> services = new ArrayList<>();
        for (String path : servicePaths) {
            try {
                ServiceInfo service = scanService(path);
                services.add(service);
            } catch (Exception e) {
                logger.error("Failed to scan service at {}: {}", path, e.getMessage());
            }
        }
        return services;
    }

    /**
     * Discover service directories under a root directory.
     * A service directory matches one of the following rules:
     *  - Contains a pom.xml
     *  - Contains standard source directory: src/main/java
     * This will not include the root directory itself, only its sub-directories.
     *
     * @param rootDir   the parent directory that contains services
     * @param recursive whether to scan recursively
     * @return list of absolute service directory paths
     */
    public List<String> discoverServiceDirs(String rootDir, boolean recursive) {
        Path rootPath = Paths.get(rootDir);
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Root directory does not exist or is not a directory: " + rootDir);
        }

        logger.info("Discovering services under: {} (recursive={})", rootPath.toAbsolutePath(), recursive);

        List<String> result = new ArrayList<>();
        try {
            if (recursive) {
                try (Stream<Path> paths = Files.walk(rootPath)) {
                    paths
                            .filter(Files::isDirectory)
                            .filter(p -> !p.equals(rootPath))
                            .filter(this::isServiceDir)
                            .forEach(p -> result.add(p.toAbsolutePath().toString()));
                }
            } else {
                try (Stream<Path> paths = Files.list(rootPath)) {
                    paths
                            .filter(Files::isDirectory)
                            .filter(this::isServiceDir)
                            .forEach(p -> result.add(p.toAbsolutePath().toString()));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to discover service directories under {}: {}", rootDir, e.getMessage());
        }

        logger.info("Discovered {} service directories", result.size());
        return result;
    }

    private boolean isServiceDir(Path dir) {
        try {
            Path pom = dir.resolve("pom.xml");
            Path srcMainJava = dir.resolve(Paths.get("src", "main", "java"));

            // Skip if no pom.xml and no src/main/java
            if (!Files.exists(pom) && !Files.exists(srcMainJava)) {
                return false;
            }

            // If pom.xml exists, check if it's a parent POM (packaging=pom)
            if (Files.exists(pom)) {
                if (isParentPom(pom.toFile())) {
                    logger.debug("Skipping parent POM directory: {}", dir);
                    return false;
                }
                return true;
            }

            // If has src/main/java but no pom.xml, still consider it a service
            if (Files.exists(srcMainJava) && Files.isDirectory(srcMainJava)) {
                return true;
            }
        } catch (Exception e) {
            logger.debug("Skip invalid path {}: {}", dir, e.getMessage());
        }
        return false;
    }

    /**
     * Check if a pom.xml is a parent POM (packaging=pom)
     */
    private boolean isParentPom(File pomFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pomFile);
            doc.getDocumentElement().normalize();

            NodeList packagingNodes = doc.getElementsByTagName("packaging");
            if (packagingNodes.getLength() > 0) {
                Element packagingElement = (Element) packagingNodes.item(0);
                String packaging = packagingElement.getTextContent().trim();
                return "pom".equalsIgnoreCase(packaging);
            }

            // Default packaging is jar if not specified
            return false;
        } catch (Exception e) {
            logger.debug("Failed to check POM packaging: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parse Maven pom.xml to extract service information
     */
    private void parsePomXml(File pomFile, ServiceInfo serviceInfo) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pomFile);
            doc.getDocumentElement().normalize();

            // Get groupId
            NodeList groupIdNodes = doc.getElementsByTagName("groupId");
            if (groupIdNodes.getLength() > 0) {
                Element groupIdElement = (Element) groupIdNodes.item(0);
                serviceInfo.setGroupId(groupIdElement.getTextContent().trim());
            }

            // Get artifactId
            NodeList artifactIdNodes = doc.getElementsByTagName("artifactId");
            if (artifactIdNodes.getLength() > 0) {
                Element artifactIdElement = (Element) artifactIdNodes.item(0);
                String artifactId = artifactIdElement.getTextContent().trim();
                serviceInfo.setArtifactId(artifactId);
                if (serviceInfo.getName() == null) {
                    serviceInfo.setName(artifactId);
                }
            }

            // Get version
            NodeList versionNodes = doc.getElementsByTagName("version");
            if (versionNodes.getLength() > 0) {
                Element versionElement = (Element) versionNodes.item(0);
                serviceInfo.setVersion(versionElement.getTextContent().trim());
            }

            // Get name if available
            NodeList nameNodes = doc.getElementsByTagName("name");
            if (nameNodes.getLength() > 0) {
                Element nameElement = (Element) nameNodes.item(0);
                serviceInfo.setName(nameElement.getTextContent().trim());
            }

        } catch (Exception e) {
            logger.warn("Failed to parse pom.xml: {}", e.getMessage());
        }
    }

    /**
     * Detect base package from source directory structure
     */
    private String detectBasePackage(Path servicePath) {
        Path srcMainJava = servicePath.resolve("src/main/java");
        if (!Files.exists(srcMainJava)) {
            logger.warn("Standard Maven structure not found at {}", servicePath);
            return "";
        }

        try (Stream<Path> paths = Files.walk(srcMainJava, 10)) {
            // Find the first .java file
            List<Path> javaFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .limit(10)
                    .collect(Collectors.toList());

            if (!javaFiles.isEmpty()) {
                Path javaFile = javaFiles.get(0);
                Path relativePath = srcMainJava.relativize(javaFile.getParent());
                return relativePath.toString().replace(File.separator, ".");
            }
        } catch (Exception e) {
            logger.warn("Failed to detect base package: {}", e.getMessage());
        }

        return "";
    }

    /**
     * Find all Java source files in a service
     */
    public List<Path> findJavaFiles(ServiceInfo serviceInfo) {
        Path srcMainJava = Paths.get(serviceInfo.getRootPath(), "src/main/java");
        if (!Files.exists(srcMainJava)) {
            logger.warn("Source directory not found: {}", srcMainJava);
            return new ArrayList<>();
        }

        try (Stream<Path> paths = Files.walk(srcMainJava)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to find Java files in {}: {}", srcMainJava, e.getMessage());
            return new ArrayList<>();
        }
    }
}
