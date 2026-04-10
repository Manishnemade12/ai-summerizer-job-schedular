package com.smartcache;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartCacheApplication {

    public static void main(String[] args) {
        // Load .env file into system properties (same as godotenv in Go)
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(entry ->
                    System.setProperty(entry.getKey(), entry.getValue())
            );
        } catch (Exception e) {
            System.out.println("No .env file found, using environment variables");
        }

        SpringApplication.run(SmartCacheApplication.class, args);
    }
}
