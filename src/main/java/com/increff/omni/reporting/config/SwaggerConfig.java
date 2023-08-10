package com.increff.omni.reporting.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.annotations.OpenAPI30;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.ZonedDateTime;

/**
 * This Spring configuration file enables Swagger and Swagger UI for REST API
 */

@Configuration
//@EnableWebMvc
//@EnableSwagger2
//@Profile({"dev","qa"})
//@OpenAPIDefinition
//@OpenAPI30
public class SwaggerConfig implements WebMvcConfigurer {

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

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .packagesToScan("com.increff")
                .group("springshop-public")
                .pathsToMatch("/**")
                .build();
    }
    // Add configuration for Swagger


    @Bean
    public OpenAPI springShopOpenAPI(@Value("${server.servlet.context-path}")String contextPath) {
        return new OpenAPI()
                .addServersItem(new Server().url(contextPath))
                .info(new Info().title("SpringShop API")
                        .description("Spring shop sample application")
                        .version("3.0.0"));
    }

}
