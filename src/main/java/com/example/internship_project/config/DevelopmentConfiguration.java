package com.example.internship_project.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "false")
public class DevelopmentConfiguration {
    // Конфигурация для режима разработки без БД
    // Логирование будет пропускаться при недоступности БД
}

