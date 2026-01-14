package com.trimminflow.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {

    public static void main(String[] args) {
        loadEnv();
        SpringApplication.run(DemoApplication.class, args);
    }

    private static void loadEnv() {
        // Try to find .env file in current directory
        java.io.File envFile = new java.io.File(".env");
        if (envFile.exists()) {
            try (java.util.stream.Stream<String> stream = java.nio.file.Files.lines(envFile.toPath())) {
                stream.forEach(line -> {
                    // Simple parsing for key=value
                    if (line != null && !line.trim().isEmpty() && !line.trim().startsWith("#")) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            // Handle potential quotes
                            if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                                value = value.substring(1, value.length() - 1);
                            }
                            System.setProperty(key, value);
                        }
                    }
                });
                System.out.println("Loaded environment variables from .env");
            } catch (java.io.IOException e) {
                System.err.println("Could not read .env file: " + e.getMessage());
            }
        }
    }

}
