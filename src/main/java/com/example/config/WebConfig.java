package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@EnableWebMvc
@ComponentScan("com.example.web")
public class WebConfig implements WebMvcConfigurer {

    @Value("${APP_ENV:prod}")
    String appEnv;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry r) {
        r.addResourceHandler("/static/**")
         .addResourceLocations("/" + appEnv + "/", "/static/");
    }

    // @Value の ${...} を解決するため（static 必須）。
    // ファイル選択は Bean 生成より前に決まるので、APP_ENV はここで直接読む。
    // 既定ではファイルより Environment（システムプロパティ、環境変数）が優先される。
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setFileEncoding("UTF-8");
        configurer.setLocations(new ClassPathResource("application-" + resolveAppEnv() + ".properties"));
        return configurer;
    }

    static String resolveAppEnv() {
        String env = System.getProperty("APP_ENV");
        if (env == null || env.isBlank()) {
            env = System.getenv("APP_ENV");
        }
        if (env == null || env.isBlank()) {
            env = "prod";
        }
        return env;
    }

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver vr = new InternalResourceViewResolver();
        vr.setPrefix("/WEB-INF/views/");
        vr.setSuffix(".jsp");
        return vr;
    }
}
