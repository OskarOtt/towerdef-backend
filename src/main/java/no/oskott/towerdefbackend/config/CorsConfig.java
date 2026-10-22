package no.oskott.towerdefbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    WebMvcConfigurer corsConfigurer(TowerdefProperties props) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(props.security().allowedOrigins().toArray(String[]::new))
                        .allowedMethods("GET", "POST")
                        .allowedHeaders("Content-Type")
                        .maxAge(3600);
            }
        };
    }
}
