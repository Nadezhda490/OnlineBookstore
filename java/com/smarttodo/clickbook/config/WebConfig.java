package com.smarttodo.clickbook.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Основной обработчик для статических файлов
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // Обработчик для /images/** перенаправляет в /static/images/
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");

        // Другие ресурсы
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        // Доступ к статическим ресурсам для админа
        registry.addResourceHandler("/admin/**")
                .addResourceLocations("classpath:/static/");
    }
}