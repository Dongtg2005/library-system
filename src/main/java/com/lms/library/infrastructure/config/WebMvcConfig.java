package com.lms.library.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /uploads/** URL paths to the local /app/uploads/ directory
        // In Docker, the volume is at /app/uploads/
        String path = "/app/uploads/";
        if (!new File(path).exists()) {
            path = "uploads/"; // local development fallback
        }
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
    }
}
