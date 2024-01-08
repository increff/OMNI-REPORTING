//package com.increff.omni.reporting.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartResolver;
//import org.springframework.web.multipart.support.StandardServletMultipartResolver;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
//import java.time.ZonedDateTime;
//
///**
// * This Spring configuration file enables Swagger and Swagger UI for REST API
// */
//
//@Configuration
//@EnableWebMvc
//@EnableSwagger2
//@Profile({"dev","qa"})
//public class SwaggerConfig extends WebMvcConfigurerAdapter{
//
//    @Bean
//    public Docket api() {
//        return new Docket(DocumentationType.SWAGGER_2)//
//                .directModelSubstitute(ZonedDateTime.class, String.class)
//                .useDefaultResponseMessages(false)//
//                .select()
//                .apis(RequestHandlerSelectors
//                        .withClassAnnotation(RestController.class))//
//                .paths(PathSelectors.regex("/.*"))
//                .build();
//    }
//
//    // Add configuration for Swagger
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }
//
//    @Bean
//    public MultipartResolver multipartResolver() {
//        return new StandardServletMultipartResolver();
//    }
//
//}
