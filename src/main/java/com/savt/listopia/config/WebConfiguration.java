package com.savt.listopia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("https://*.ensargok.com", "https://*.cinemium.net", "http://localhost:5173/", "http://127.0.0.1:5173/", "https://*.listopia.pages.dev/")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
