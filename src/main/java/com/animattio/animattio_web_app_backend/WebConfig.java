package com.animattio.animattio_web_app_backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer { //  this interface allows to override methods to customize configurations like CORS
    @Override
    public void addCorsMappings(CorsRegistry registry) { // Configures which domains, HTTP methods, and headers are allowed to access backend
        registry.addMapping("/**") // Applies the CORS configuration to all endpoints
                .allowedOrigins("https://frontend-animattio-39d2470d8e0c.herokuapp.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
//                .allowCredentials(true);
    }
}

